import {Injectable} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {ProjectLanguage} from '@core/model/project-language';
import {Observable} from 'rxjs';
import {Validator} from '@core/model/validator';
import {
	ValidatorBasicInfoDialogComponent, ValidatorBasicInfoDialogData
} from '../../dialogs/validator/validator-basic-info-dialog/validator-basic-info-dialog.component';
import {
	ValidatorWorkflowConfiguration, ValidatorWorkflowDialogComponent
} from '../../dialogs/validator/validator-workflow-dialog/validator-workflow-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class ValidatorDialogService {
	constructor(
		private dialog: MatDialog
	) {}

	openCreateDialog(
		projectId: string,
		languages: ProjectLanguage[]
	): Observable<any> {
		const dialogRef: MatDialogRef<ValidatorBasicInfoDialogComponent> = this.dialog.open(
			ValidatorBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {
					projectId,
					validator: null,
					languages
				} as ValidatorBasicInfoDialogData
			}
		);

		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(
		projectId: string,
		validator: Validator,
		languages: ProjectLanguage[]
	): Observable<any> {
		const clonedValidator = JSON.parse(JSON.stringify(validator));

		const dialogRef: MatDialogRef<ValidatorBasicInfoDialogComponent> = this.dialog.open(
			ValidatorBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {
					projectId,
					validator: clonedValidator,
					languages
				} as ValidatorBasicInfoDialogData
			}
		);

		return dialogRef.afterClosed();
	}

	openWorkflowDialog(
		validator: Validator,
		currentConfiguration: ValidatorWorkflowConfiguration | null
	): Observable<any> {
		const dialogRef: MatDialogRef<ValidatorWorkflowDialogComponent> = this.dialog.open(
			ValidatorWorkflowDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {
					validator,
					availableWorkflows: [], //TODO: Pass actual workflows
					currentConfiguration
				}
			}
		);

		return dialogRef.afterClosed();
	}
}
