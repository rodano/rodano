import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {ProjectLanguage} from '@core/model/project-language';
import {forkJoin, of, Subscription} from 'rxjs';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {LanguageService} from '../../services/language.service';
import {HttpErrorResponse} from '@angular/common/http';
import {Profile} from '@core/model/profile';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';
import {ProfileDialogService} from '../../services/dialogs/profile-dialog.service';
import {ProfileDetailComponent} from '../profile-detail/profile-detail.component';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';

@Component({
	selector: 'app-profile-list',
	standalone: true,
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatProgressSpinnerModule,
		MatSnackBarModule,
		ProfileDetailComponent,
		EmptyStateComponent
	],
	templateUrl: './profile-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class ProfileListComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string | null>();
	@Output() profilesChanged = new EventEmitter<{modificationCount: number}>();
	@Output() profileContextChanged = new EventEmitter<{
		profiles: any[];
		selectedProfileId: string | null;
	}>();

	selectedProfile: Profile | null = null;
	viewMode = 'detail';
	loading = false;

	projectLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';
	private languageSubscription: Subscription;

	constructor(
		public profileManager: ProfileManagerService,
		public languageService: LanguageService,
		private workflowManager: WorkflowManagerService,
		private profileDialogService: ProfileDialogService,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadProfiles();

		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnChanges(changes: any): void {
		if(changes['selectedNode'] && this.profiles.length > 0) {
			const nodeId = this.selectedNode;
			if(nodeId?.startsWith('profile-')) {
				const profileId = nodeId.replace('profile-', '');
				const profile = this.profiles.find(p => p.profileId === profileId);
				if(profile) {
					this.selectedProfile = profile;
				}
			}
			else if(nodeId === 'profiles') {
				this.selectedProfile = null;
			}
		}
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	get viewLevel(): number {
		if(!this.selectedProfile) {
			return 0;
		}
		return 2;
	}

	get profiles(): Profile[] {
		return this.profileManager.getAll();
	}

	get modifiedProfileIds(): Set<string> {
		return this.profileManager.getModifiedIds();
	}

	get originalProfiles(): Profile[] {
		return this.profileManager.getOriginals();
	}

	get totalModificationCount(): number {
		return this.profileManager.getModificationCount();
	}

	loadProfiles(): void {
		this.loading = true;
		forkJoin({
			profiles: this.profileManager.load(this.projectId),
			workflows: this.workflowManager.isLoaded()
				? of(null)
				: this.workflowManager.load(this.projectId)
		}).subscribe({
			next: ({profiles}) => {
				if(this.selectedProfile) {
					this.selectedProfile = profiles.find(
						p => p.profileId === this.selectedProfile!.profileId
					) || null;
				}
				this.loading = false;
				this.emitContext();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error loading profiles:', error);
				this.snackBar.open('Failed to load profiles', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	onSelectProfile(profile: Profile): void {
		if(this.selectedProfile?.profileId === profile.profileId) {
			this.clearSelection();
		}
		else {
			this.selectProfile(profile);
		}
		this.emitContext();
	}

	clearSelection(): void {
		this.selectedProfile = null;
		this.viewMode = 'detail';
		this.nodeSelected.emit('profiles');
	}

	private selectProfile(profile: Profile): void {
		const previousProfileId = this.selectedProfile?.profileId;
		this.selectedProfile = profile;

		if(previousProfileId !== profile.profileId) {
			this.viewMode = 'detail';
		}
		this.emitContext();
		this.nodeSelected.emit(`profile-${profile.profileId}`);
	}

	isSelected(profile: Profile): boolean {
		return this.selectedProfile?.profileId === profile.profileId;
	}

	onCreateProfile(): void {
		this.profileDialogService.openCreateDialog(
			this.projectId,
			this.projectLanguages
		).subscribe((result: Profile | null) => {
			if(result) {
				this.profileManager.create(this.projectId, result).subscribe({
					next: () => {
						this.snackBar.open('Profile created', 'Close', {duration: 2000});
						this.loadProfiles();
						this.emitModificationChange();
					},
					error: error => {
						console.error('Error creating profile', error);
						this.snackBar.open('Failed to create profile', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onProfileUpdated(updatedProfile: Profile): void {
		this.selectedProfile = this.profileManager.getById(updatedProfile.profileId) || null;
		this.emitModificationChange();
	}

	onProfileDeleted(profileId: string): void {
		const profile = this.profiles.find(p => p.profileId === profileId);
		if(!profile) {
			return;
		}
		this.performDelete(profile);
	}

	private performDelete(profile: Profile): void {
		this.profileManager.delete(this.projectId, profile.profileId).subscribe({
			next: () => {
				this.snackBar.open('Profile deleted', 'Close', {duration: 2000});

				if(this.selectedProfile?.profileId === profile.profileId) {
					this.clearSelection();
				}

				this.loadProfiles();
				this.emitModificationChange();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting profile', error);
				this.snackBar.open('Failed to delete profile', 'Close', {duration: 3000});
			}
		});
	}

	private emitModificationChange(): void {
		this.profilesChanged.emit({modificationCount: this.totalModificationCount});
	}

	private emitContext(): void {
		this.profileContextChanged.emit({
			profiles: [...this.profiles],
			selectedProfileId: this.selectedProfile?.profileId || null
		});
	}

	isModified(profileId: string): boolean {
		return this.profileManager.isModified(profileId);
	}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}
}
