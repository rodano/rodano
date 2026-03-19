import {Injectable} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {ProjectLanguage} from '@core/model/project-language';
import {Observable} from 'rxjs';
import {CronDialogComponent, CronDialogData} from '../../dialogs/cron/cron-dialog/cron-dialog.component';
import {Cron} from '@core/model/cron';

@Injectable({
	providedIn: 'root'
})
export class CronDialogService {
	constructor(
		private dialog: MatDialog
	) {}

	openCreateDialog(projectId: string, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<CronDialogComponent> = this.dialog.open(
			CronDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {projectId, cron: null, languages} as CronDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(projectId: string, cron: Cron, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<CronDialogComponent> = this.dialog.open(
			CronDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {
					projectId,
					cron: JSON.parse(JSON.stringify(cron)),
					languages
				} as CronDialogData
			}
		);
		return dialogRef.afterClosed();
	}
}
