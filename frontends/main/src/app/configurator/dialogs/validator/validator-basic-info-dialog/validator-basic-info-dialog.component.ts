import {ProjectLanguage} from '@core/model/project-language';
import {Validator} from '@core/model/validator';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ValidatorManagerService} from '../../../services/manager/validator-manager.service';
import {LanguageService} from '../../../services/language.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';

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
export class ValidatorBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode: boolean;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<ValidatorBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: ValidatorBasicInfoDialogData,
		private validatorManager: ValidatorManagerService,
		private snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef);
		this.isEditMode = !!data.validator;
	}

	ngOnInit(): void {
		this.loadProjectLanguages(this.data.languages);
		this.initializeForm();
	}

	initializeLanguageForms(): void {
		this.availableLanguages.forEach(lang => {
			if(!lang.languageCode) {
				return;
			}
			const v = this.data.validator;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [v?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [v?.longname?.[lang.languageCode] || ''],
				description: [v?.description?.[lang.languageCode] || ''],
				message: [v?.message?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		const v = this.data.validator;
		this.form = this.fb.group({
			id: [v?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			required: [v?.required ?? false],
			script: [v?.script ?? false]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentValidatorId = this.data.validator?.validatorId;
		return this.validatorManager.getAll().some(v =>
			v.id.toUpperCase() === code.toUpperCase() && v.validatorId !== currentValidatorId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.snackBar.open(`A validator with code "${code}" already exists`, 'Close', {duration: 3000});
			return;
		}

		const {shortname, longname, description} = this.collectTranslations();
		const message: Record<string, string> = {};

		this.languageForms.forEach((langForm, langCode) => {
			const v = langForm.value;
			if(v.message) {
				message[langCode] = v.message;
			}
		});

		this.dialogRef.close({
			id: code,
			shortname,
			longname,
			description,
			message,
			...this.form.value
		});
	}
}
