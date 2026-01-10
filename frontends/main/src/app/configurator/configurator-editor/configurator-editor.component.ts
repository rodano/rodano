import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ConfiguratorService} from '@core/services/configurator.service';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {ProjectConfigVersion} from '@core/model/project-config-version';
import {ConfiguratorDetailComponent} from '../configurator-detail/configurator-detail.component';
import {ConfiguratorTreeComponent} from '../configurator-tree/configurator-tree.component';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmationDialogComponent} from '../../confirmation-dialog/confirmation-dialog.component';
import {SnapshotsListDialogComponent} from '../snapshots/snapshots-list-dialog/snapshots-list-dialog.component';
import {CreateSnapshotDialogComponent} from '../snapshots/create-snapshot-dialog/create-snapshot-dialog.component';

@Component({
	selector: 'app-configurator-editor',
	standalone: true,
	templateUrl: './configurator-editor.component.html',
	styleUrls: ['./configurator-editor.component.css'],
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatProgressSpinnerModule,
		MatSnackBarModule,
		ConfiguratorTreeComponent,
		ConfiguratorDetailComponent,
		MatTooltipModule
	]
})
export class ConfiguratorEditorComponent implements OnInit {
	projectId = '';
	project: ConfiguratorProject | null = null;
	workingProject: ConfiguratorProject | null = null;
	draftVersion: ProjectConfigVersion | null = null;
	loading = true;
	saving = false;
	selectedNode: string | null = null;
	modifiedFields = new Set<string>();

	canRollback = false;
	canRollForward = false;

	constructor(
		private route: ActivatedRoute,
		private router: Router,
		private configuratorService: ConfiguratorService,
		private snackBar: MatSnackBar,
		private dialog: MatDialog
	) {}

	ngOnInit(): void {
		this.projectId = this.route.snapshot.paramMap.get('projectId') || '';
		if(this.projectId) {
			this.initializeProject();
		}
	}

	initializeProject(): void {
		this.loading = true;

		localStorage.setItem('configProjectId', this.projectId);

		this.configuratorService.getProject(this.projectId).subscribe({
			next: project => {
				this.project = project;
				this.workingProject = {...project};
				this.loadDraftVersion();
			},
			error: error => {
				console.error('Error selecting project:', error);
				this.snackBar.open('Failed to load project', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	loadDraftVersion(): void {
		this.configuratorService.getOrCreateDraft(this.projectId).subscribe({
			next: draft => {
				this.draftVersion = draft;
				this.loading = false;
				this.loadSnapshotState();
			},
			error: error => {
				console.error('Error loading draft version:', error);
				this.snackBar.open('Failed to load draft', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	loadSnapshotState(): void {
		if(!this.draftVersion) {
			return;
		}

		this.configuratorService.getSnapshots(this.projectId, this.draftVersion.pk!).subscribe({
			next: snapshots => {
				this.canRollback = snapshots.currentIndex !== undefined && snapshots.currentIndex > 0;
				this.canRollForward = snapshots.currentIndex !== undefined && snapshots.snapshots !== undefined && snapshots.currentIndex < snapshots.snapshots.length - 1;
			},
			error: () => {
				this.canRollback = false;
				this.canRollForward = false;
			}
		});
	}

	onNodeSelected(nodeId: string): void {
		this.selectedNode = nodeId;
	}

	onFieldsUpdated(updates: Partial<ConfiguratorProject>): void {
		this.workingProject = {...this.workingProject!, ...updates};

		Object.keys(updates).forEach(key => {
			const originalValue = this.project![key as keyof ConfiguratorProject];
			const newValue = updates[key as keyof ConfiguratorProject];

			if(newValue !== null && originalValue !== null && typeof newValue === 'object' && typeof originalValue === 'object') {
				if(JSON.stringify(originalValue) !== JSON.stringify(newValue)) {
					this.modifiedFields.add(key);
				}
				else {
					this.modifiedFields.delete(key);
				}
			}
			else {
				if(originalValue !== newValue) {
					this.modifiedFields.add(key);
				}
				else {
					this.modifiedFields.delete(key);
				}
			}
		});
	}

	onSaveDraft(): void {
		if(!this.workingProject) {
			return;
		}

		this.saving = true;
		this.configuratorService.updateProject(this.projectId, this.workingProject).subscribe({
			next: updatedProject => {
				this.project = updatedProject;
				this.workingProject = {...updatedProject};
				this.modifiedFields.clear();
				this.saving = false;
				this.snackBar.open('Draft saved', 'Close', {duration: 2000});
			},
			error: error => {
				console.error('Error saving draft:', error);
				this.snackBar.open('Failed to save draft', 'Close', {duration: 3000});
				this.saving = false;
			}
		});
	}

	onDiscardChanges(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			maxWidth: '90vw',
			data: {
				title: 'Discard Changes',
				message: 'Are you sure you want to discard all unsaved changes? This action cannot be undone.',
				confirmText: 'Discard',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});

		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.workingProject = {...this.project!};
				this.modifiedFields.clear();
				this.snackBar.open('Changes discarded', 'Close', {duration: 2000});
			}
		});
	}

	onPublish(): void {
		//TODO: Open publish dialog
		console.log('Publish clicked');
	}

	onBack(): void {
		localStorage.removeItem('configProjectId');
		this.router.navigate(['/configurator']);
	}

	getTranslatedName(translations: Record<string, string> | undefined): string {
		if(!translations) {
			return '';
		}
		return translations['en'] || Object.values(translations)[0] || '';
	}

	get hasModifications(): boolean {
		return this.modifiedFields.size > 0;
	}

	onCreateSnapshot(): void {
		const dialogRef = this.dialog.open(CreateSnapshotDialogComponent, {
			width: '500px'
		});

		dialogRef.afterClosed().subscribe(summary => {
			if(summary && this.draftVersion) {
				this.configuratorService.createSnapshot(
					this.projectId,
					this.draftVersion.pk!,
					summary
				).subscribe({
					next: () => {
						this.snackBar.open('Snapshot created', 'Close', {duration: 2000});
						this.loadSnapshotState();
					},
					error: error => {
						console.error('Error creating snapshot:', error);
						this.snackBar.open('Failed to create snapshot', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onViewSnapshots(): void {
		if(!this.draftVersion) {
			return;
		}

		this.configuratorService.getSnapshots(this.projectId, this.draftVersion.pk!).subscribe({
			next: snapshots => {
				const dialogRef = this.dialog.open(SnapshotsListDialogComponent, {
					width: '600px',
					data: {snapshots}
				});

				dialogRef.afterClosed().subscribe(result => {
					if(result?.action === 'restore') {
						this.restoreSnapshot(result.index);
					}
				});
			},
			error: error => {
				console.error('Error loading snapshots:', error);
				this.snackBar.open('Failed to load snapshots', 'Close', {duration: 3000});
			}
		});
	}

	private restoreSnapshot(targetIndex: number): void {
		if(!this.draftVersion) {
			return;
		}

		this.configuratorService.getSnapshots(this.projectId, this.draftVersion.pk!).subscribe({
			next: snapshots => {
				const currentIndex = snapshots.currentIndex;

				if(currentIndex === undefined) {
					this.snackBar.open('Invalid snapshot state', 'Close', {duration: 3000});
					return;
				}

				if(targetIndex < currentIndex) {
					const stepsBack = currentIndex - targetIndex;
					this.performRollback(stepsBack);
				}
				else if(targetIndex > currentIndex) {
					const stepsForward = targetIndex - currentIndex;
					this.performRollForward(stepsForward);
				}
			}
		});
	}

	private performRollback(steps: number): void {
		if(steps <= 0 || !this.draftVersion) {
			return;
		}

		this.configuratorService.rollbackSnapshot(this.projectId, this.draftVersion.pk!).subscribe({
			next: updatedProject => {
				this.project = updatedProject;
				this.workingProject = {...updatedProject};
				this.modifiedFields.clear();

				if(steps > 1) {
					this.performRollback(steps - 1);
				}
				else {
					this.snackBar.open('Restored to snapshot', 'Close', {duration: 2000});
					this.loadSnapshotState();
				}
			},
			error: error => {
				console.error('Error rolling back:', error);
				this.snackBar.open('Failed to restore snapshot', 'Close', {duration: 3000});
			}
		});
	}

	private performRollForward(steps: number): void {
		if(steps <= 0 || !this.draftVersion) {
			return;
		}

		this.configuratorService.rollForwardSnapshot(this.projectId, this.draftVersion.pk!).subscribe({
			next: updatedProject => {
				this.project = updatedProject;
				this.workingProject = {...updatedProject};
				this.modifiedFields.clear();

				if(steps > 1) {
					this.performRollForward(steps - 1);
				}
				else {
					this.snackBar.open('Restored to snapshot', 'Close', {duration: 2000});
					this.loadSnapshotState();
				}
			},
			error: error => {
				console.error('Error rolling forward:', error);
				this.snackBar.open('Failed to restore snapshot', 'Close', {duration: 3000});
			}
		});
	}
}
