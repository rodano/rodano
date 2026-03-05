import {FieldModel} from '@core/model/field-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatSnackBar} from '@angular/material/snack-bar';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface FieldModelExportDialogData {
	fieldModel: FieldModel;
}

@Component({
	selector: 'app-field-model-export-dialog',
	standalone: true,
	templateUrl: './field-model-export-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule,
		MatIconModule, MatCheckboxModule]
})
export class FieldModelExportDialogComponent extends BaseDialogComponent<FieldModelExportDialogData> implements OnInit {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		dialogRef: MatDialogRef<FieldModelExportDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: FieldModelExportDialogData,
		snackBar: MatSnackBar
	) {
		super(dialogRef, data, snackBar);
	}

	ngOnInit(): void {
		this.initializeForm();
	}

	initializeForm(): void {
		const fm = this.data.fieldModel;

		this.form = this.fb.group({
			exportable: [fm.exportable || false],
			exportOrder: [fm.exportOrder, [Validators.min(0)]],
			searchable: [fm.searchable || false]
		});

		this.form.get('exportable')?.valueChanges.subscribe((exportable: boolean) => {
			if(!exportable) {
				this.form.get('exportOrder')?.setValue(null, {emitEvent: false});
			}
		});
	}

	onSave(): void {
		if(this.form.invalid) {
			this.showError();
			return;
		}

		const formValue = this.form.getRawValue();

		const result = {
			exportable: formValue.exportable,
			exportOrder: formValue.exportable ? formValue.exportOrder : null,
			searchable: formValue.searchable
		};

		this.dialogRef.close(result);
	}
}
