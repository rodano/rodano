import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {DatasetModel} from '@core/model/dataset-model';
import {MatCheckbox} from '@angular/material/checkbox';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface DatasetModelFamilyDialogData {
	datasetModel: DatasetModel;
}

@Component({
	selector: 'app-dataset-model-family-dialog',
	standalone: true,
	templateUrl: './dataset-model-family-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatCheckbox]
})
export class DatasetModelFamilyDialogComponent extends BaseDialogComponent<DatasetModelFamilyDialogData> implements OnInit {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		dialogRef: MatDialogRef<DatasetModelFamilyDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: DatasetModelFamilyDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		if(this.data.datasetModel) {
			this.form = this.fb.group({
				family: [this.data.datasetModel.family || ''],
				master: [this.data.datasetModel.master ?? false]
			});
		}
	}

	onSave(): void {
		if(this.form.valid) {
			const formValue = this.form.value;
			const result: any = {};

			const normalize = (value: any) => {
				if(value === '' || value === null || value === undefined) {
					return null;
				}
				return value;
			};

			const normalizedFamily = normalize(formValue.family);
			const normalizedOriginalFamily = normalize(this.data.datasetModel.family);
			if(normalizedFamily !== normalizedOriginalFamily) {
				result.family = formValue.family;
			}

			const normalizedMaster = normalize(formValue.master);
			const normalizedOriginalMaster = normalize(this.data.datasetModel.master);
			if(normalizedMaster !== normalizedOriginalMaster) {
				result.master = formValue.master;
			}

			this.dialogRef.close(result);
		}
	}
}
