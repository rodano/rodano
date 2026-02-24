import {Injectable} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {ProjectLanguage} from '@core/model/project-language';
import {Observable} from 'rxjs';
import {Feature} from '@core/model/feature';
import {FeatureDialogComponent, FeatureDialogData} from '../../dialogs/feature/feature-dialog/feature-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class FeatureDialogService {
	constructor(
		private dialog: MatDialog
	) {}

	openCreateDialog(projectId: string, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<FeatureDialogComponent> = this.dialog.open(
			FeatureDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {projectId, feature: null, languages} as FeatureDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(projectId: string, feature: Feature, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<FeatureDialogComponent> = this.dialog.open(
			FeatureDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {
					projectId,
					feature: JSON.parse(JSON.stringify(feature)),
					languages
				} as FeatureDialogData
			}
		);
		return dialogRef.afterClosed();
	}
}
