import {DatasetModel} from '@core/model/dataset-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatCheckbox} from '@angular/material/checkbox';

export interface DatasetModelExportDialogData {
	datasetModel: DatasetModel;
}

@Component({
	selector: 'app-dataset-model-export-dialog',
	standalone: true,
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatCheckbox
	],
	templateUrl: './dataset-model-export-dialog.component.html',
	styleUrls: ['../../shared-dialog-styles.css']
})
export class DatasetModelExportDialogComponent implements OnInit {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		public dialogRef: MatDialogRef<DatasetModelExportDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: DatasetModelExportDialogData
	) {
		this.form = this.fb.group({
			exportable: [false],
			exportOrder: [{value: null, disabled: true}]
		});
	}

	ngOnInit(): void {
		if(this.data.datasetModel) {
			const isExportable = this.data.datasetModel.exportable ?? false;

			this.form.patchValue({
				exportable: isExportable,
				exportOrder: this.data.datasetModel.exportOrder
			});

			if(isExportable) {
				this.form.get('exportOrder')?.enable();
			}
			else {
				this.form.get('exportOrder')?.disable();
			}
		}

		this.form.get('exportable')?.valueChanges.subscribe((isExportable: boolean) => {
			if(isExportable) {
				this.form.get('exportOrder')?.enable();
			}
			else {
				this.form.get('exportOrder')?.disable();
				this.form.get('exportOrder')?.setValue(null);
			}
		});
	}

	onCancel(): void {
		this.dialogRef.close();
	}

	onSave(): void {
		if(this.form.valid) {
			const formValue = this.form.getRawValue();
			const result: any = {};

			const normalize = (value: any) => {
				if(value === '' || value === null || value === undefined) {
					return null;
				}
				return value;
			};

			const normalizedExportable = normalize(formValue.exportable);
			const normalizedOriginalExportable = normalize(this.data.datasetModel.exportable);
			if(normalizedExportable !== normalizedOriginalExportable) {
				result.exportable = formValue.exportable;
			}

			const normalizedExportOrder = normalize(formValue.exportOrder);
			const normalizedOriginalExportOrder = normalize(this.data.datasetModel.exportOrder);
			if(normalizedExportOrder !== normalizedOriginalExportOrder) {
				result.exportOrder = formValue.exportOrder;
			}

			this.dialogRef.close(result);
		}
	}
}
