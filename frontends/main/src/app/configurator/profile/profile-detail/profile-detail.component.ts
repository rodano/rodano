import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {Subscription} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {Profile} from '@core/model/profile';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';
import {ProfileDialogService} from '../../services/dialogs/profile-dialog.service';
import {ProjectLanguage} from '@core/model/project-language';

@Component({
	selector: 'app-profile-detail',
	standalone: true,
	templateUrl: './profile-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule]
})
export class ProfileDetailComponent implements OnInit, OnDestroy {
	@Input() profile!: Profile;
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() allProfiles: Profile[] = [];
	@Output() profileUpdated = new EventEmitter<Profile>();
	@Output() profileDeleted = new EventEmitter<string>();
	@Output() closed = new EventEmitter<void>();

	selectedLanguage = '';
	projectLanguages: ProjectLanguage[];
	private languageSubscription: Subscription;

	constructor(
		public profileManager: ProfileManagerService,
		private workflowManager: WorkflowManagerService,
		private languageService: LanguageService,
		private profileDialogService: ProfileDialogService,
		private dialog: MatDialog,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	onEditBasicInfo(): void {
		this.profileDialogService.openBasicInfoDialog(
			this.projectId,
			this.profile,
			this.projectLanguages
		).subscribe((result: any) => {
			if(result) {
				const updatedProfile: Profile = {...this.profile, ...result};
				this.profileManager.update(updatedProfile);
				this.profileUpdated.emit(updatedProfile);
				this.showStagedMessage();
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Profile',
				message: `Are you sure you want to delete "${this.getTranslatedValue(this.profile.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.profileDeleted.emit(this.profile.profileId);
			}
		});
	}

	onClose(): void {
		this.closed.emit();
	}

	private showStagedMessage(): void {
		this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
	}

	isFieldModified(fieldName: string): boolean {
		return this.profileManager.isFieldModified(this.profile.profileId, fieldName);
	}

	getTranslatedName(translations: Record<string, string> | undefined): string {
		return this.getTranslatedValue(translations);
	}

	getTranslatedValue(translations: Record<string, string> | undefined, languageCode?: string): string {
		if(!translations) {
			return '';
		}
		return translations[languageCode || this.selectedLanguage] || '';
	}

	getLanguageName(code: string | undefined): string {
		if(!code) {
			return 'Unknown';
		}
		try {
			return new Intl.DisplayNames(['en'], {type: 'language'}).of(code) || code.toUpperCase();
		}
		catch (error) {
			console.error(error);
			return code.toUpperCase();
		}
	}

	getWorkflowDisplayName(): string {
		if(!this.profile.workflowOfInterestId) {
			return 'Not set';
		}
		const workflow = this.workflowManager.getById(this.profile.workflowOfInterestId);
		return workflow
			? `${this.languageService.getDefaultTranslation(workflow.shortname) || workflow.id} (${workflow.id})`
			: this.profile.workflowOfInterestId;
	}
}
