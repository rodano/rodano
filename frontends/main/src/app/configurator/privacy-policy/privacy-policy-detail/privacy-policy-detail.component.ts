import {Component, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {PrivacyPolicy} from '@core/model/privacy-policy';
import {PrivacyPolicyManagerService} from '../../services/manager/privacy-policy-manager.service';
import {PrivacyPolicyDialogService} from '../../services/dialogs/privacy-policy-dialog.service';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';
import {BaseManagerDetailComponent} from '../../shared/base-manager-detail.component';
import {SettingItemComponent} from '../../shared/setting-item/setting-item.component';

@Component({
	selector: 'app-privacy-policy-detail',
	standalone: true,
	templateUrl: './privacy-policy-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent]
})
export class PrivacyPolicyDetailComponent extends BaseManagerDetailComponent<PrivacyPolicy, PrivacyPolicyManagerService> {
	@Input() override entity!: PrivacyPolicy;
	@Input() override allEntities: PrivacyPolicy[] = [];
	@Output() privacyPolicyUpdated = this.entityUpdated;
	@Output() privacyPolicyDeleted = this.entityDeleted;

	@Input() set privacyPolicy(v: PrivacyPolicy) {this.entity = v;}
	get privacyPolicy(): PrivacyPolicy {return this.entity;}

	@Input() set allPrivacyPolicies(v: PrivacyPolicy[]) {this.allEntities = v;}

	constructor(
		privacyPolicyManager: PrivacyPolicyManagerService,
		languageService: LanguageService,
		private profileManager: ProfileManagerService,
		private privacyPolicyDialogService: PrivacyPolicyDialogService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(privacyPolicyManager, languageService, snackBar);
	}

	protected getEntityId(): string {return this.entity.policyId;}

	onEditBasicInfo(): void {
		this.privacyPolicyDialogService.openBasicInfoDialog(
			this.projectId,
			this.entity,
			this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditResources(): void {
		this.privacyPolicyDialogService.openResourcesDialog(
			this.projectId,
			this.entity
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
				title: 'Delete Privacy Policy',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.entity.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.entityDeleted.emit(this.entity.policyId);
			}
		});
	}

	getProfileLabel(profileId: string): string {
		return this.languageService.getLabelById(profileId, id => this.profileManager.getById(id));
	}
}
