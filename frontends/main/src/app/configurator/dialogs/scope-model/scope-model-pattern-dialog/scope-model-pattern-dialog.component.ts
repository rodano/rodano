import {ScopeModel} from '@core/model/scope-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';

export interface ScopeModelPatternDialogData {
	scopeModel: ScopeModel;
}

interface PatternOption {
	pattern: string;
	description: string;
}

@Component({
	selector: 'app-scope-model-pattern-dialog',
	standalone: true,
	imports: [
		CommonModule,
		MatDialogModule,
		MatButtonModule,
		MatIconModule,
		ReactiveFormsModule
	],
	templateUrl: './scope-model-pattern-dialog.component.html',
	styleUrls: ['../../shared-dialog-styles.css']
})
export class ScopeModelPatternDialogComponent implements OnInit {
	form: FormGroup;

	patternOptions: PatternOption[] = [
		{pattern: '${parent}', description: 'Parent scope code'},
		{pattern: '${model}', description: 'Scope model code'},
		{pattern: '${siblingsNumber:DECIMAL}', description: 'Sibling number (decimal)'},
		{pattern: '${sameScopeModelNumber:DECIMAL}', description: 'Same scope model number (decimal)'},
		{pattern: '${checksum}', description: 'Checksum'}
	];

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<ScopeModelPatternDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: ScopeModelPatternDialogData
	) {
		this.form = this.fb.group({
			scopeFormat: ['']
		});
	}

	ngOnInit(): void {
		if(this.data.scopeModel) {
			this.form.patchValue({
				scopeFormat: this.data.scopeModel.scopeFormat || ''
			});
		}
	}

	insertPattern(pattern: string): void {
		const scopeFormatControl = this.form.get('scopeFormat');
		const currentValue = scopeFormatControl?.value || '';
		scopeFormatControl?.setValue(currentValue + pattern);
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

			const normalizedScopeFormat = normalize(formValue.scopeFormat);
			const normalizedOriginalScopeFormat = normalize(this.data.scopeModel.scopeFormat);
			if(normalizedScopeFormat !== normalizedOriginalScopeFormat) {
				result.scopeFormat = formValue.scopeFormat;
			}

			this.dialogRef.close(result);
		}
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}
}
