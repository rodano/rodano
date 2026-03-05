import {DatasetModel} from '@core/model/dataset-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatCheckbox} from '@angular/material/checkbox';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface DatasetModelExportDialogData {
	datasetModel: DatasetModel;
}

@Component({
	selector: 'app-dataset-model-export-dialog',
	standalone: true,
	templateUrl: './dataset-model-export-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatCheckbox]
})
export class DatasetModelExportDialogComponent extends BaseDialogComponent<DatasetModelExportDialogData> implements OnInit {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		dialogRef: MatDialogRef<DatasetModelExportDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: DatasetModelExportDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		if(this.data.datasetModel) {
			const isExportable = this.data.datasetModel.exportable ?? false;

			this.form = this.fb.group({
				exportable: [isExportable],
				exportOrder: [this.data.datasetModel.exportOrder]
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

	onSave(): void {
		if(this.form.valid) {
			const formValue = this.form.getRawValue();
			const result: any = {};

			const normalize = (value: any) => value === '' || value === null || value === undefined ? null : value;

			const normalizedExportable = normalize(formValue.exportable);
			const normalizedOriginalExportable = normalize(this.data.datasetModel.exportable);
			if(normalizedExportable !== normalizedOriginalExportable) {
				result.exportable = formValue.exportable;
			}

			if(!formValue.exportable) {
				result.exportOrder = null;
			}
			else {
				const normalizedExportOrder = normalize(formValue.exportOrder);
				const normalizedOriginalExportOrder = normalize(this.data.datasetModel.exportOrder);
				if(normalizedExportOrder !== normalizedOriginalExportOrder) {
					result.exportOrder = formValue.exportOrder;
				}
			}

			this.dialogRef.close(result);
		}
	}
}
