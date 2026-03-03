import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {HttpErrorResponse} from '@angular/common/http';
import {forkJoin, of} from 'rxjs';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {Profile} from '@core/model/profile';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';
import {ProfileDialogService} from '../../services/dialogs/profile-dialog.service';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {LanguageService} from '../../services/language.service';
import {ProfileDetailComponent} from '../profile-detail/profile-detail.component';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {BaseListComponent} from '../../shared/base-list.component';
import {ListHeaderComponent} from '../../shared/list-header/list-header.component';
import {ModifiedDirective} from '../../shared/modified.directive';

@Component({
	selector: 'app-profile-list',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule,
		MatSnackBarModule, ProfileDetailComponent, EmptyStateComponent, ListHeaderComponent, ModifiedDirective],
	templateUrl: './profile-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class ProfileListComponent
	extends BaseListComponent<Profile>
	implements OnInit, OnChanges, OnDestroy {
	@Input() override projectId = '';
	@Input() override project: ConfiguratorProject | null = null;
	@Input() override selectedNode: string | null = null;
	@Output() profilesChanged = new EventEmitter<boolean>();
	@Output() profileContextChanged = new EventEmitter<{
		profiles: any[];
		selectedProfileId: string | null;
	}>();

	constructor(
		public profileManager: ProfileManagerService,
		public override languageService: LanguageService,
		private workflowManager: WorkflowManagerService,
		private profileDialogService: ProfileDialogService,
		snackBar: MatSnackBar
	) {
		super(profileManager, languageService, snackBar);
	}

	getEntityId(p: Profile): string {return p.profileId;}
	getNodePrefix(): string {return 'profile';}
	getListNodeName(): string {return 'profiles';}

	get profiles(): Profile[] {return this.manager.getAll();}
	get selectedProfile(): Profile | null {return this.selected as Profile | null;}
	get modifiedProfileIds(): Set<string> {return this.manager.getModifiedIds();}
	get originalProfiles(): Profile[] {return this.manager.getOriginals();}

	loadProfiles(): void {this.load();}
	load(): void {
		this.loading = true;
		forkJoin({
			profiles: this.profileManager.load(this.projectId),
			workflows: this.workflowManager.isLoaded()
				? of(null)
				: this.workflowManager.load(this.projectId)
		}).subscribe({
			next: ({profiles}) => this.afterLoad(profiles),
			error: (e: HttpErrorResponse) => this.handleLoadError(e, 'profiles')
		});
	}

	emitChangedEvent(hasModifications: boolean): void {
		this.profilesChanged.emit(hasModifications);
	}

	emitContextEvent(): void {
		this.profileContextChanged.emit({
			profiles: [...this.profiles],
			selectedProfileId: this.selected?.profileId || null
		});
	}

	onCreate(): void {
		this.profileDialogService.openCreateDialog(this.projectId, this.projectLanguages)
			.subscribe((result: Profile | null) => {
				if(result) {
					this.profileManager.create(this.projectId, result).subscribe({
						next: () => this.afterCreate('Profile'),
						error: e => {
							console.error(e);
							this.snackBar.open('Failed to create profile', 'Close', {duration: 3000});
						}
					});
				}
			});
	}

	onUpdated(updated: Profile): void {
		this.profileManager.update(updated);
		this.selected = this.profileManager.getById(updated.profileId) || null;
		this.emitModificationChange();
	}

	onDeleted(profileId: string): void {
		const profile = this.profiles.find(p => p.profileId === profileId);
		if(!profile) {
			return;
		}
		this.profileManager.delete(this.projectId, profileId).subscribe({
			next: () => this.afterDelete(profile, 'Profile'),
			error: (e: HttpErrorResponse) => {
				console.error(e);
				this.snackBar.open('Failed to delete profile', 'Close', {duration: 3000});
			}
		});
	}

	onSelectProfile(p: Profile): void {this.onSelect(p);}
	onCreateProfile(): void {this.onCreate();}
	onProfileUpdated(p: Profile): void {this.onUpdated(p);}
	onProfileDeleted(id: string): void {this.onDeleted(id);}

	getWorkflowLabel(id: string): string {
		return this.languageService.getLabelById(id, i => this.workflowManager.getById(i));
	}
}
