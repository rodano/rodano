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

export interface FieldModelExportDialogData {
	fieldModel: FieldModel;
}

@Component({
	selector: 'app-field-model-export-dialog',
	standalone: true,
	templateUrl: './field-model-export-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatFormFieldModule,
		MatInputModule,
		MatButtonModule,
		MatIconModule,
		MatCheckboxModule
	]
})
export class FieldModelExportDialogComponent implements OnInit {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<FieldModelExportDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: FieldModelExportDialogData,
		private snackBar: MatSnackBar
	) {}

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

	onCancel(): void {
		this.dialogRef.close(null);
	}

	onSave(): void {
		if(this.form.invalid) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
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
