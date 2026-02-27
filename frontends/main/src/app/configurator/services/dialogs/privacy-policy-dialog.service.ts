import {Injectable} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {ProjectLanguage} from '@core/model/project-language';
import {Observable} from 'rxjs';
import {PrivacyPolicy} from '@core/model/privacy-policy';
import {
	PrivacyPolicyBasicInfoDialogComponent,
	PrivacyPolicyBasicInfoDialogData
} from '../../dialogs/privacy-policy/privacy-policy-basic-info-dialog/privacy-policy-basic-info-dialog.component';
import {
	PrivacyPolicyResourcesDialogComponent
} from '../../dialogs/privacy-policy/privacy-policy-resources-dialog/privacy-policy-resources-dialog.component';
import {ProfileManagerService} from '../manager/profile-manager.service';

@Injectable({
	providedIn: 'root'
})
export class PrivacyPolicyDialogService {
	constructor(
		private profileManager: ProfileManagerService,
		private dialog: MatDialog
	) {}

	openCreateDialog(projectId: string, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<PrivacyPolicyBasicInfoDialogComponent> = this.dialog.open(
			PrivacyPolicyBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {projectId, privacyPolicy: null, languages} as PrivacyPolicyBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(projectId: string, privacyPolicy: PrivacyPolicy, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<PrivacyPolicyBasicInfoDialogComponent> = this.dialog.open(
			PrivacyPolicyBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {projectId, privacyPolicy: JSON.parse(JSON.stringify(privacyPolicy)), languages} as PrivacyPolicyBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openResourcesDialog(
		projectId: string,
		privacyPolicy: PrivacyPolicy
	): Observable<any> {
		const dialogRef = this.dialog.open(PrivacyPolicyResourcesDialogComponent, {
			width: '500px',
			data: {
				privacyPolicy: JSON.parse(JSON.stringify(privacyPolicy)),
				availableProfiles: this.profileManager.getAll()
			}
		});
		return dialogRef.afterClosed();
	}
}
