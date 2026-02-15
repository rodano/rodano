import {ProjectLanguage} from '@core/model/project-language';
import {Validator} from '@core/model/validator';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatSnackBar} from '@angular/material/snack-bar';
import {HttpErrorResponse} from '@angular/common/http';
import {ValidatorService} from '../../../services/api/validator.service';

export interface ValidatorBasicInfoDialogData {
	projectId: string;
	validator: Validator | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-validator-basic-info-dialog',
	standalone: true,
	templateUrl: './validator-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatTabsModule,
		MatCheckboxModule
	]
})
export class ValidatorBasicInfoDialogComponent implements OnInit {
	form: FormGroup;
	languageForms = new Map<string, FormGroup>();
	availableLanguages: ProjectLanguage[] = [];
	isEditMode: boolean;
	allValidators: Validator[] = [];

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<ValidatorBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: ValidatorBasicInfoDialogData,
		private validatorService: ValidatorService,
		private snackBar: MatSnackBar
	) {
		this.isEditMode = !!data.validator;
	}

	ngOnInit(): void {
		this.loadProjectLanguages();
		this.loadAllValidators();
		this.initializeForm();
	}

	loadProjectLanguages(): void {
		this.availableLanguages = this.data.languages || [];

		if(this.availableLanguages.length === 0) {
			this.availableLanguages = [{languageCode: 'en', isDefault: true}];
		}

		this.initializeLanguageForms();
	}

	loadAllValidators(): void {
		this.validatorService.getValidators(this.data.projectId).subscribe({
			next: (validators: Validator[]) => {
				this.allValidators = validators;
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error loading validators:', error);
			}
		});
	}

	initializeForm(): void {
		const v = this.data.validator;

		this.form = this.fb.group({
			id: [
				v?.id || '',
				[Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]
			],
			required: [v?.required ?? false],
			script: [v?.script ?? false]
		});
	}

	initializeLanguageForms(): void {
		this.availableLanguages.forEach((lang: ProjectLanguage) => {
			if(lang.languageCode) {
				const v = this.data.validator;
				const langForm = this.fb.group({
					shortname: [
						v?.shortname?.[lang.languageCode] || '',
						lang.isDefault ? Validators.required : []
					],
					longname: [v?.longname?.[lang.languageCode] || ''],
					description: [v?.description?.[lang.languageCode] || ''],
					message: [v?.message?.[lang.languageCode] || '']
				});
				this.languageForms.set(lang.languageCode, langForm);
			}
		});
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

	areLanguageFormsValid(): boolean {
		let allValid = true;
		this.languageForms.forEach((langForm: FormGroup) => {
			if(langForm.invalid) {
				allValid = false;
			}
		});
		return allValid;
	}

	onIdInput(event: Event): void {
		const input = event.target as HTMLInputElement;
		const uppercaseValue = input.value.toUpperCase();
		input.value = uppercaseValue;
		this.form.patchValue({id: uppercaseValue}, {emitEvent: false});
	}

	isCodeDuplicate(code: string): boolean {
		const currentValidatorId = this.data.validator?.validatorId;
		return this.allValidators.some(v =>
			v.id.toUpperCase() === code.toUpperCase() && v.validatorId !== currentValidatorId
		);
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const formValue = this.form.getRawValue();
		const code = formValue.id.toUpperCase();

		if(this.isCodeDuplicate(code)) {
			this.snackBar.open(`A validator with code "${code}" already exists`, 'Close', {duration: 3000});
			return;
		}

		const shortname: Record<string, string> = {};
		const longname: Record<string, string> = {};
		const description: Record<string, string> = {};
		const invalidMessage: Record<string, string> = {};

		this.languageForms.forEach((langForm: FormGroup, langCode: string) => {
			const langValue = langForm.value;
			if(langValue.shortname) {
				shortname[langCode] = langValue.shortname;
			}
			if(langValue.longname) {
				longname[langCode] = langValue.longname;
			}
			if(langValue.description) {
				description[langCode] = langValue.description;
			}
			if(langValue.message) {
				invalidMessage[langCode] = langValue.message;
			}
		});

		const result = {
			id: code,
			shortname,
			longname,
			description,
			message: invalidMessage,
			required: formValue.required,
			script: formValue.script
		};

		this.dialogRef.close(result);
	}
}
