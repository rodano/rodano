import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {DatasetModel} from '@core/model/dataset-model';
import {MatCheckbox} from '@angular/material/checkbox';

export interface DatasetModelFamilyDialogData {
	datasetModel: DatasetModel;
}

@Component({
	selector: 'app-dataset-model-family-dialog',
	standalone: true,
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatCheckbox
	],
	templateUrl: './dataset-model-family-dialog.component.html',
	styleUrls: ['../../dialog-shared.css']
})
export class DatasetModelFamilyDialogComponent implements OnInit {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		public dialogRef: MatDialogRef<DatasetModelFamilyDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: DatasetModelFamilyDialogData
	) {
		this.form = this.fb.group({
			family: [''],
			master: [false]
		});
	}

	ngOnInit(): void {
		if(this.data.datasetModel) {
			this.form.patchValue({
				family: this.data.datasetModel.family || '',
				master: this.data.datasetModel.master ?? false
			});
		}
	}

	onCancel(): void {
		this.dialogRef.close();
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
