import {ProjectLanguage} from '@core/model/project-language';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSelectModule} from '@angular/material/select';
import {LanguageService} from '../../../services/language.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';
import {FormModel} from '@core/model/form-model';
import {FormModelManagerService} from '../../../services/manager/form-model-manager.service';
import {MatCheckboxModule} from '@angular/material/checkbox';

export interface FormModelBasicInfoDialogData {
	projectId: string;
	formModel: FormModel | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-form-model-basic-info-dialog',
	standalone: true,
	templateUrl: './form-model-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatTabsModule, MatSelectModule, MatCheckboxModule]
})
export class FormModelBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode: boolean;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<FormModelBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: FormModelBasicInfoDialogData,
		private formModelManager: FormModelManagerService,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
		this.isEditMode = !!data.formModel;
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
			const fm = this.data.formModel;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [fm?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [fm?.longname?.[lang.languageCode] || ''],
				description: [fm?.description?.[lang.languageCode] || ''],
				printButtonLabel: [fm?.printButtonLabel?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		const fm = this.data.formModel;
		this.form = this.fb.group({
			id: [fm?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			optional: [fm?.optional ?? false]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentFormModelId = this.data.formModel?.formModelId;
		return this.formModelManager.getAll().some(fm =>
			fm.id.toUpperCase() === code.toUpperCase() && fm.formModelId !== currentFormModelId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.showError();
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.showError(`A form model with code "${code}" already exists`);
			return;
		}

		const {shortname, longname, description} = this.collectTranslations();
		const printButtonLabel: Record<string, string> = {};

		this.languageForms.forEach((langForm, langCode) => {
			const v = langForm.value;
			if(v.printButtonLabel) {
				printButtonLabel[langCode] = v.printButtonLabel;
			}
		});

		this.dialogRef.close({
			...this.form.value,
			id: code,
			shortname,
			longname,
			description,
			printButtonLabel
		});
	}
}
