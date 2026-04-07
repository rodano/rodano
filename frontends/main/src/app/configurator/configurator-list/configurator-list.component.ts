import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatDialog, MatDialogModule} from '@angular/material/dialog';
import {MatMenuModule} from '@angular/material/menu';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {ConfiguratorService} from '../services/api/configurator.service';
import {Router} from '@angular/router';
import {CreateProjectDialogComponent} from '../dialogs/project/create-project-dialog/create-project-dialog.component';
import {ConfirmationDialogComponent} from '../../confirmation-dialog/confirmation-dialog.component';
import {SnapshotsListDialogComponent} from '../dialogs/base/snapshots-list-dialog/snapshots-list-dialog.component';
import {LanguageService} from '../services/language.service';

@Component({
	selector: 'app-configurator-list',
	standalone: true,
	templateUrl: './configurator-list.component.html',
	styleUrls: ['./configurator-list.component.css'],
	imports: [
		CommonModule,
		FormsModule,
		MatButtonModule,
		MatIconModule,
		MatDialogModule,
		MatMenuModule,
		MatSnackBarModule,
		MatProgressSpinnerModule,
		MatTooltipModule
	]
})
export class ConfiguratorListComponent implements OnInit {
	projects: ConfiguratorProject[] = [];
	filteredProjects: ConfiguratorProject[] = [];
	searchTerm = '';
	loading = false;

	filters = {
		showActive: true,
		showClosed: true,
		showArchived: false,

		showDraft: true,
		showPublished: true,
		showConfigArchived: false,

		createdAfter: null as string | null,
		createdBefore: null as string | null
	};

	sortBy: 'nameAsc' | 'nameDesc' | 'newestFirst' | 'oldestFirst' = 'newestFirst';

	constructor(
		private configuratorService: ConfiguratorService,
		private languageService: LanguageService,
		private dialog: MatDialog,
		private snackBar: MatSnackBar,
		private router: Router
	) {}

	ngOnInit(): void {
		this.loadProjects();
	}

	loadProjects(): void {
		this.loading = true;
		this.configuratorService.getAllProjects().subscribe({
			next: projects => {
				this.projects = projects;
				this.applyFilters();
				this.loading = false;
			},
			error: error => {
				console.error('Error loading projects:', error);
				this.snackBar.open('Failed to load projects', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	applyFilters(): void {
		let filtered = [...this.projects];

		filtered = filtered.filter(project =>
			project.projectId !== '00000000-0000-0000-0000-000000000000'
		);

		if(this.searchTerm.trim()) {
			const term = this.searchTerm.toLowerCase();
			filtered = filtered.filter(project =>
				project.code?.toLowerCase().includes(term)
				|| this.getTranslatedName(project.shortname).toLowerCase().includes(term)
				|| this.getTranslatedName(project.longname).toLowerCase().includes(term)
			);
		}

		filtered = filtered.filter(project => {
			if(project.status === 'ACTIVE' && !this.filters.showActive) {
				return false;
			}
			if(project.status === 'CLOSED' && !this.filters.showClosed) {
				return false;
			}
			if(project.status === 'ARCHIVED' && !this.filters.showArchived) {
				return false;
			}
			return true;
		});

		filtered = filtered.filter(project => {
			const configStatus = project.draftConfigVersionStatus || project.activeConfigVersionStatus || 'DRAFT';

			if(configStatus === 'DRAFT' && !this.filters.showDraft) {
				return false;
			}
			if(configStatus === 'PUBLISHED' && !this.filters.showPublished) {
				return false;
			}
			if(configStatus === 'ARCHIVED' && !this.filters.showConfigArchived) {
				return false;
			}
			return true;
		});

		filtered = filtered.filter(p => {
			if(this.filters.createdAfter && p.created) {
				const createdDate = new Date(p.created);
				if(createdDate < new Date(this.filters.createdAfter)) {
					return false;
				}
			}
			if(this.filters.createdBefore && p.created) {
				const createdDate = new Date(p.created);
				if(createdDate > new Date(this.filters.createdBefore)) {
					return false;
				}
			}
			return true;
		});

		filtered.sort((a, b) => {
			switch(this.sortBy) {
				case 'nameAsc':
					return this.getTranslatedName(a.shortname).localeCompare(this.getTranslatedName(b.shortname));
				case 'nameDesc':
					return this.getTranslatedName(b.shortname).localeCompare(this.getTranslatedName(a.shortname));
				case 'newestFirst':
					return this.getTimestamp(b.created) - this.getTimestamp(a.created);
				case 'oldestFirst':
					return this.getTimestamp(a.created) - this.getTimestamp(b.created);
				default:
					return 0;
			}
		});

		this.filteredProjects = filtered;
	}

	clearFilters(): void {
		this.searchTerm = '';
		this.filters = {
			showActive: true,
			showClosed: true,
			showArchived: false,
			showDraft: true,
			showPublished: true,
			showConfigArchived: false,
			createdAfter: null,
			createdBefore: null
		};
		this.sortBy = 'newestFirst';
		this.applyFilters();
	}

	hasActiveFilters(): boolean {
		return this.filters.showConfigArchived || !this.filters.showPublished || !this.filters.showDraft || this.filters.showArchived || !this.filters.showClosed || !this.filters.showActive || this.searchTerm.trim() !== '' || this.filters.createdAfter !== null || this.filters.createdBefore !== null;
	}

	navigateToProjects(): void {
		this.router.navigate(['/projects']);
	}

	openCreateDialog(): void {
		const dialogRef = this.dialog.open(CreateProjectDialogComponent, {
			width: '600px',
			disableClose: true,
			autoFocus: false
		});

		dialogRef.afterClosed().subscribe(result => {
			if(result) {
				this.router.navigate(['/configurator/projects', result.projectId, 'edit']);
			}
		});
	}

	editProject(project: ConfiguratorProject): void {
		this.router.navigate(['configurator/projects', project.projectId, 'edit']);
	}

	viewVersions(project: ConfiguratorProject): void {
		const projectId = project.projectId;
		const draftVersionId = project.draftConfigVersionId;

		if(!projectId || !draftVersionId) {
			this.snackBar.open('No draft version available', 'Close', {duration: 3000});
			return;
		}

		this.configuratorService.getSnapshots(projectId, draftVersionId).subscribe({
			next: snapshots => {
				const dialogRef = this.dialog.open(SnapshotsListDialogComponent, {
					width: '600px',
					data: {snapshots}
				});

				dialogRef.afterClosed().subscribe(result => {
					if(result?.action === 'restore') {
						this.snackBar.open('Please open the editor to restore snapshots', 'Close', {duration: 3000});
					}
				});
			},
			error: error => {
				console.error('Error loading snapshots:', error);
				this.snackBar.open('Failed to load snapshots', 'Close', {duration: 3000});
			}
		});
	}

	copyProject(project: ConfiguratorProject): void {
		const dialogRef = this.dialog.open(CreateProjectDialogComponent, {
			width: '500px',
			disableClose: true,
			autoFocus: false,
			data: {cloneMode: true}
		});

		dialogRef.afterClosed().subscribe(result => {
			if(!result) {
				return;
			}

			this.configuratorService.cloneProject(project.projectId!, result).subscribe({
				next: newProject => {
					this.snackBar.open('Project cloned successfully', 'Close', {duration: 3000});
					this.router.navigate(['/configurator/projects', newProject.projectId, 'edit']);
				},
				error: () => this.snackBar.open('Failed to clone project', 'Close', {duration: 3000})
			});
		});
	}

	archiveProject(project: ConfiguratorProject): void {
		const projectId = project.projectId;
		if(!projectId || !project.draftConfigVersionId) {
			return;
		}

		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			maxWidth: '90vw',
			data: {
				title: 'Archive Draft',
				message: `Are you sure you want to archive the draft for "${this.getTranslatedName(project.shortname)}"?`,
				confirmText: 'Archive',
				cancelText: 'Cancel',
				type: 'warning'
			}
		});

		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.configuratorService.archiveDraft(projectId, project.draftConfigVersionId!).subscribe({
					next: () => {
						this.loadProjects();
						this.snackBar.open('Draft archived successfully', 'Close', {duration: 3000});
					},
					error: error => {
						console.error('Error archiving draft:', error);
						this.snackBar.open('Failed to archive draft', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	restoreProject(project: ConfiguratorProject): void {
		const projectId = project.projectId;
		if(!projectId || !project.draftConfigVersionId) {
			return;
		}

		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			maxWidth: '90vw',
			data: {
				title: 'Restore Draft',
				message: `Are you sure you want to restore the draft for "${this.getTranslatedName(project.shortname)}"?`,
				confirmText: 'Restore',
				cancelText: 'Cancel',
				type: 'info'
			}
		});

		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.configuratorService.restoreDraft(projectId, project.draftConfigVersionId!).subscribe({
					next: () => {
						this.loadProjects();
						this.snackBar.open('Draft restored successfully', 'Close', {duration: 3000});
					},
					error: error => {
						console.error('Error restoring draft:', error);
						this.snackBar.open('Failed to restore draft', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	getTranslatedName(translations: Record<string, string> | undefined): string {
		return this.languageService.getDefaultTranslation(translations) || '';
	}

	private getTimestamp(date: Date | undefined): number {
		return date ? new Date(date).getTime() : 0;
	}
}
