import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {
	FieldModelBasicInfoDialogComponent, FieldModelBasicInfoDialogData
} from '../../dialogs/field-model/field-model-basic-info-dialog/field-model-basic-info-dialog.component';
import {FieldModel} from '@core/model/field-model';
import {ProjectLanguage} from '@core/model/project-language';
import {
	FieldModelValidationDialogComponent
} from '../../dialogs/field-model/field-model-validation-dialog/field-model-validation-dialog.component';
import {
	FieldModelCalculatedValueDialogComponent
} from '../../dialogs/field-model/field-model-calculated-value-dialog/field-model-calculated-value-dialog.component';
import {
	FieldModelExportDialogComponent
} from '../../dialogs/field-model/field-model-export-dialog/field-model-export-dialog.component';
import {
	FieldModelPossibleValuesDialogComponent
} from '../../dialogs/field-model/field-model-possible-values-dialog/field-model-possible-values-dialog.component';
import {
	FieldModelHelpDialogComponent
} from '../../dialogs/field-model/field-model-help-dialog/field-model-help-dialog.component';
import {
	FieldModelResourcesDialogComponent
} from '../../dialogs/field-model/field-model-resources-dialog/field-model-resources-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class FieldModelDialogService {
	constructor(
		private dialog: MatDialog
	) {}

	openCreateDialog(
		projectId: string,
		datasetModelId: string,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(FieldModelBasicInfoDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				projectId,
				datasetModelId,
				fieldModel: null,
				languages
			} as FieldModelBasicInfoDialogData
		});

		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(
		fieldModel: FieldModel,
		projectId: string,
		datasetModelId: string,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(FieldModelBasicInfoDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				projectId,
				datasetModelId,
				fieldModel,
				languages
			} as FieldModelBasicInfoDialogData
		});

		return dialogRef.afterClosed();
	}

	openValidationDialog(
		fieldModel: FieldModel,
		projectId: string,
		languages: ProjectLanguage[]
	): Observable<any> {
		const dialogRef = this.dialog.open(FieldModelValidationDialogComponent, {
			width: '500px',
			data: {
				fieldModel,
				languages
			},
			disableClose: true
		});

		return dialogRef.afterClosed();
	}

	openCalculatedValueDialog(
		fieldModel: FieldModel,
		projectId: string
	): Observable<any> {
		const dialogRef = this.dialog.open(FieldModelCalculatedValueDialogComponent, {
			width: '500px',
			data: {
				fieldModel,
				projectId
			},
			disableClose: true
		});

		return dialogRef.afterClosed();
	}

	openExportDialog(fieldModel: FieldModel): Observable<any> {
		const dialogRef = this.dialog.open(FieldModelExportDialogComponent, {
			width: '500px',
			data: {
				fieldModel
			},
			disableClose: true
		});

		return dialogRef.afterClosed();
	}

	openHelpDialog(
		fieldModel: FieldModel,
		languages: ProjectLanguage[]
	): Observable<any> {
		const dialogRef = this.dialog.open(FieldModelHelpDialogComponent, {
			width: '500px',
			data: {
				fieldModel,
				languages
			},
			disableClose: true
		});

		return dialogRef.afterClosed();
	}

	openPossibleValuesDialog(
		fieldModel: FieldModel,
		projectId: string,
		languages: ProjectLanguage[]
	): Observable<any> {
		const dialogRef = this.dialog.open(FieldModelPossibleValuesDialogComponent, {
			width: '500px',
			data: {
				fieldModel,
				languages
			},
			disableClose: true
		});

		return dialogRef.afterClosed();
	}

	openResourcesDialog(
		fieldModel: FieldModel,
		availableValidators: {id: string; name: string}[],
		availableWorkflows: {id: string; name: string}[]
	): Observable<any> {
		const dialogRef = this.dialog.open(FieldModelResourcesDialogComponent, {
			width: '500px',
			data: {
				fieldModel,
				availableValidators,
				availableWorkflows
			},
			disableClose: true
		});

		return dialogRef.afterClosed();
	}
}
