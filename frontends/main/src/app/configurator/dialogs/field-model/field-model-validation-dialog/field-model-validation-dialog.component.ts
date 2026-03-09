import {FieldModel} from '@core/model/field-model';
import {ProjectLanguage} from '@core/model/project-language';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSelectModule} from '@angular/material/select';
import {MatSnackBar} from '@angular/material/snack-bar';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface FieldModelValidationDialogData {
	fieldModel: FieldModel;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-field-model-validation-dialog',
	standalone: true,
	templateUrl: './field-model-validation-dialog.component.html',
	styleUrls: ['./field-model-validation-dialog.component.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule,
		MatIconModule, MatCheckboxModule, MatTabsModule, MatSelectModule]
})
export class FieldModelValidationDialogComponent extends BaseDialogComponent<FieldModelValidationDialogData> implements OnInit {
	form: FormGroup;
	languageForms = new Map<string, FormGroup>();
	availableLanguages: ProjectLanguage[] = [];
	fieldType: string;

	constructor(
		private fb: FormBuilder,
		dialogRef: MatDialogRef<FieldModelValidationDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: FieldModelValidationDialogData,
		snackBar: MatSnackBar
	) {
		super(dialogRef, data, snackBar);
		this.fieldType = data.fieldModel.type || 'STRING';
	}

	ngOnInit(): void {
		this.loadProjectLanguages();
		this.initializeForm();
	}

	loadProjectLanguages(): void {
		this.availableLanguages = this.data.languages || [];
		if(this.availableLanguages.length === 0) {
			this.availableLanguages = [{languageCode: 'en', isDefault: true}];
		}
		this.initializeLanguageForms();
	}

	initializeForm(): void {
		const fm = this.data.fieldModel;

		this.form = this.fb.group({
			maxLength: [fm.maxLength],
			matcher: [fm.matcher || ''],

			maxIntegerDigits: [fm.maxIntegerDigits],
			maxDecimalDigits: [fm.maxDecimalDigits],
			minValue: [fm.minValue],
			maxValue: [fm.maxValue],

			minYear: [fm.minYear],
			allowDateInFuture: [fm.allowDateInFuture || false],
			withYears: [fm.withYears || false],
			withMonths: [fm.withMonths || false],
			withDays: [fm.withDays || false],
			withHours: [fm.withHours || false],
			withMinutes: [fm.withMinutes || false],
			withSeconds: [fm.withSeconds || false],

			yearsMandatory: [fm.yearsMandatory || false],
			monthsMandatory: [fm.monthsMandatory || false],
			daysMandatory: [fm.daysMandatory || false],
			hoursMandatory: [fm.hoursMandatory || false],
			minutesMandatory: [fm.minutesMandatory || false],
			secondsMandatory: [fm.secondsMandatory || false]
		});
	}

	initializeLanguageForms(): void {
		this.availableLanguages.forEach((lang: ProjectLanguage) => {
			if(lang.languageCode) {
				const fm = this.data.fieldModel;
				const langForm = this.fb.group({
					matcherMessage: [fm.matcherMessage?.[lang.languageCode] || '']
				});
				this.languageForms.set(lang.languageCode, langForm);
			}
		});
	}

	hasValidation(): boolean {
		return this.fieldType !== 'AUTO_COMPLETION'
		  && this.fieldType !== 'SELECT'
		  && this.fieldType !== 'RADIO'
		  && this.fieldType !== 'CHECKBOX'
		  && this.fieldType !== 'CHECKBOX_GROUP';
	}

	showStringValidation(): boolean {
		return this.fieldType === 'STRING';
	}

	showTextAreaValidation(): boolean {
		return this.fieldType === 'TEXTAREA' || this.fieldType === 'FILE';
	}

	showDateValidation(): boolean {
		return this.fieldType === 'DATE';
	}

	showDateSelectValidation(): boolean {
		return this.fieldType === 'DATE_SELECT';
	}

	showNumberValidation(): boolean {
		return this.fieldType === 'NUMBER';
	}

	getLanguageLabel(code: string, isDefault: boolean): string {
		const name = this.getLanguageName(code);
		return isDefault ? `${name} ★` : name;
	}

	getLanguageName(code: string): string {
		try {
			const displayNames = new Intl.DisplayNames(['en'], {type: 'language'});
			return displayNames.of(code) || code.toUpperCase();
		}
		catch (error) {
			console.error(error);
			return code.toUpperCase();
		}
	}

	onSave(): void {
		if(this.form.invalid) {
			this.showError();
			return;
		}

		const formValue = this.form.getRawValue();

		if((this.showStringValidation() || this.showNumberValidation()) && formValue.matcher) {
			try {
				new RegExp(formValue.matcher);
			}
			catch (error) {
				const errorMessage = error instanceof Error ? error.message : 'Invalid regex syntax';
				this.showError(`Invalid regular expression: ${errorMessage}`);
				return;
			}
		}

		const result: any = {};

		if(this.showStringValidation() || this.showNumberValidation()) {
			const matcherMessage: Record<string, string> = {};
			this.languageForms.forEach((langForm: FormGroup, langCode: string) => {
				const langValue = langForm.value;
				if(langValue.matcherMessage) {
					matcherMessage[langCode] = langValue.matcherMessage;
				}
			});
			result.matcherMessage = matcherMessage;
		}

		if(this.showStringValidation()) {
			result.maxLength = formValue.maxLength;
			result.matcher = formValue.matcher;
		}

		if(this.showTextAreaValidation()) {
			result.maxLength = formValue.maxLength;
		}

		if(this.showDateValidation()) {
			result.minYear = formValue.minYear;
			result.allowDateInFuture = formValue.allowDateInFuture;
			result.withYears = formValue.withYears;
			result.withMonths = formValue.withMonths;
			result.withDays = formValue.withDays;
			result.withHours = formValue.withHours;
			result.withMinutes = formValue.withMinutes;
			result.withSeconds = formValue.withSeconds;
		}

		if(this.showDateSelectValidation()) {
			result.minYear = formValue.minYear;
			result.allowDateInFuture = formValue.allowDateInFuture;
			result.withYears = formValue.withYears;
			result.withMonths = formValue.withMonths;
			result.withDays = formValue.withDays;
			result.withHours = formValue.withHours;
			result.withMinutes = formValue.withMinutes;
			result.withSeconds = formValue.withSeconds;
			result.yearsMandatory = formValue.yearsMandatory;
			result.monthsMandatory = formValue.monthsMandatory;
			result.daysMandatory = formValue.daysMandatory;
			result.hoursMandatory = formValue.hoursMandatory;
			result.minutesMandatory = formValue.minutesMandatory;
			result.secondsMandatory = formValue.secondsMandatory;
		}

		if(this.showNumberValidation()) {
			result.maxIntegerDigits = formValue.maxIntegerDigits;
			result.maxDecimalDigits = formValue.maxDecimalDigits;
			result.minValue = formValue.minValue;
			result.maxValue = formValue.maxValue;
			result.matcher = formValue.matcher;
		}

		this.dialogRef.close(result);
	}
}
