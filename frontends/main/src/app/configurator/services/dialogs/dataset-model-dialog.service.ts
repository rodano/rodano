import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Injectable} from '@angular/core';
import {ProjectLanguage} from '@core/model/project-language';
import {DatasetModel} from '@core/model/dataset-model';
import {
	DatasetModelBasicInfoDialogComponent, DatasetModelBasicInfoDialogData
} from '../../dialogs/dataset-model/dataset-model-basic-info-dialog/dataset-model-basic-info-dialog.component';
import {Observable} from 'rxjs';
import {
	DatasetModelFamilyDialogComponent,
	DatasetModelFamilyDialogData
} from '../../dialogs/dataset-model/dataset-model-family-dialog/dataset-model-family-dialog.component';
import {
	DatasetModelExportDialogComponent,
	DatasetModelExportDialogData
} from '../../dialogs/dataset-model/dataset-model-export-dialog/dataset-model-export-dialog.component';
import {
	DatasetModelLabelPatternsDialogComponent,
	DatasetModelLabelPatternsDialogData
} from '../../dialogs/dataset-model/dataset-model-label-patterns-dialog/dataset-model-label-patterns-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class DatasetModelDialogService {
	constructor(
		private dialog: MatDialog
	) {}

	openCreateDialog(
		projectId: string,
		languages: ProjectLanguage[]
	): Observable<any> {
		const dialogRef: MatDialogRef<DatasetModelBasicInfoDialogComponent> = this.dialog.open(
			DatasetModelBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {
					projectId,
					datasetModel: null,
					languages
				} as DatasetModelBasicInfoDialogData
			}
		);

		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(
		projectId: string,
		datasetModel: DatasetModel,
		languages: ProjectLanguage[]
	): Observable<any> {
		const clonedDatasetModel = JSON.parse(JSON.stringify(datasetModel));

		const dialogRef: MatDialogRef<DatasetModelBasicInfoDialogComponent> = this.dialog.open(
			DatasetModelBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {
					projectId,
					datasetModel: clonedDatasetModel,
					languages
				} as DatasetModelBasicInfoDialogData
			}
		);

		return dialogRef.afterClosed();
	}

	openFamilyDialog(datasetModel: DatasetModel): Observable<any> {
		const clonedDatasetModel = JSON.parse(JSON.stringify(datasetModel));

		const dialogData: DatasetModelFamilyDialogData = {
			datasetModel: clonedDatasetModel
		};

		const dialogRef = this.dialog.open(DatasetModelFamilyDialogComponent, {
			width: '500px',
			disableClose: true,
			data: dialogData
		});

		return dialogRef.afterClosed();
	}

	openExportDialog(datasetModel: DatasetModel): Observable<any> {
		const clonedDatasetModel = JSON.parse(JSON.stringify(datasetModel));

		const dialogData: DatasetModelExportDialogData = {
			datasetModel: clonedDatasetModel
		};

		const dialogRef = this.dialog.open(DatasetModelExportDialogComponent, {
			width: '500px',
			disableClose: true,
			data: dialogData
		});

		return dialogRef.afterClosed();
	}

	openLabelPatternsDialog(datasetModel: DatasetModel): Observable<any> {
		const clonedDatasetModel = JSON.parse(JSON.stringify(datasetModel));

		const dialogData: DatasetModelLabelPatternsDialogData = {
			datasetModel: clonedDatasetModel
		};

		const dialogRef = this.dialog.open(DatasetModelLabelPatternsDialogComponent, {
			width: '500px',
			disableClose: true,
			data: dialogData
		});

		return dialogRef.afterClosed();
	}
}
