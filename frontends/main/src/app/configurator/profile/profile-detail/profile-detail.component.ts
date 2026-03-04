import {Component, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {Profile} from '@core/model/profile';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';
import {ProfileDialogService} from '../../services/dialogs/profile-dialog.service';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {BaseManagerDetailComponent} from '../../shared/base-manager-detail.component';

@Component({
	selector: 'app-profile-detail',
	standalone: true,
	templateUrl: './profile-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent]
})
export class ProfileDetailComponent extends BaseManagerDetailComponent<Profile, ProfileManagerService> {
	@Input() override entity!: Profile;
	@Input() override allEntities: Profile[] = [];
	@Output() profileUpdated = this.entityUpdated;
	@Output() profileDeleted = this.entityDeleted;

	@Input() set profile(v: Profile) {this.entity = v;}
	get profile(): Profile {return this.entity;}

	@Input() set allProfiles(v: Profile[]) {this.allEntities = v;}

	constructor(
		profileManager: ProfileManagerService,
		languageService: LanguageService,
		private workflowManager: WorkflowManagerService,
		private profileDialogService: ProfileDialogService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(profileManager, languageService, snackBar);
	}

	protected getEntityId(): string {return this.entity.profileId;}

	onEditBasicInfo(): void {
		this.profileDialogService.openBasicInfoDialog(
			this.projectId,
			this.entity,
			this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Profile',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.entity.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.entityDeleted.emit(this.entity.profileId);
			}
		});
	}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}
}
