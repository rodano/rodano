import {Injectable} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {LanguageService} from '../language.service';
import {ProjectLanguage} from '@core/model/project-language';
import {Observable} from 'rxjs';
import {ProfileManagerService} from '../manager/profile-manager.service';
import {
	ProfileBasicInfoDialogComponent,
	ProfileBasicInfoDialogData
} from '../../dialogs/profile/profile-basic-info-dialog/profile-basic-info-dialog.component';
import {Profile} from '@core/model/profile';

@Injectable({
	providedIn: 'root'
})
export class ProfileDialogService {
	constructor(
		private dialog: MatDialog,
		private profileManager: ProfileManagerService,
		private languageService: LanguageService
	) {}

	openCreateDialog(projectId: string, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<ProfileBasicInfoDialogComponent> = this.dialog.open(
			ProfileBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {projectId, profile: null, languages} as ProfileBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(projectId: string, profile: Profile, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<ProfileBasicInfoDialogComponent> = this.dialog.open(
			ProfileBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {projectId, profile: JSON.parse(JSON.stringify(profile)), languages} as ProfileBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}
}
