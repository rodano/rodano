import {ProjectLanguage} from '@core/model/project-language';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSelectModule} from '@angular/material/select';
import {MatSnackBar} from '@angular/material/snack-bar';
import {FieldModel} from '@core/model/field-model';
import {FieldModelManagerService} from '../../../services/manager/field-model-manager.service';

export interface FieldModelBasicInfoDialogData {
	projectId: string;
	datasetModelId: string;
	fieldModel: FieldModel | null;
	languages: ProjectLanguage[];
}

interface TypeOption {
	value: string;
	label: string;
}

interface DataTypeOption {
	value: string;
	label: string;
}

@Component({
	selector: 'app-field-model-basic-info-dialog',
	standalone: true,
	templateUrl: './field-model-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatFormFieldModule,
		MatInputModule,
		MatButtonModule,
		MatIconModule,
		MatCheckboxModule,
		MatTabsModule,
		MatSelectModule
	]
})
export class FieldModelBasicInfoDialogComponent implements OnInit {
	form: FormGroup;
	languageForms = new Map<string, FormGroup>();
	availableLanguages: ProjectLanguage[] = [];
	isEditMode: boolean;

	typeOptions: TypeOption[] = [
		{value: 'STRING', label: 'String'},
		{value: 'AUTO_COMPLETION', label: 'Autocompleted String'},
		{value: 'DATE', label: 'Date'},
		{value: 'DATE_SELECT', label: 'Date with Selection'},
		{value: 'NUMBER', label: 'Number'},
		{value: 'SELECT', label: 'Combobox'},
		{value: 'RADIO', label: 'Radio'},
		{value: 'CHECKBOX', label: 'Checkbox'},
		{value: 'CHECKBOX_GROUP', label: 'Checkbox Group'},
		{value: 'TEXTAREA', label: 'Text Area'},
		{value: 'FILE', label: 'File'}
	];

	dataTypeOptions: DataTypeOption[] = [
		{value: 'STRING', label: 'String'},
		{value: 'DATE', label: 'Date'},
		{value: 'NUMBER', label: 'Number'},
		{value: 'BOOLEAN', label: 'Boolean'},
		{value: 'BLOB', label: 'Blob'}
	];

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<FieldModelBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: FieldModelBasicInfoDialogData,
		private fieldModelManager: FieldModelManagerService,
		private snackBar: MatSnackBar
	) {
		this.isEditMode = !!data.fieldModel;
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
			id: [
				fm?.id || '',
				[Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]
			],
			type: [fm?.type || null, Validators.required],
			dataType: [fm?.dataType || null, Validators.required],
			readOnly: [fm?.readOnly || false]
		});
	}

	initializeLanguageForms(): void {
		this.availableLanguages.forEach((lang: ProjectLanguage) => {
			if(lang.languageCode) {
				const fm = this.data.fieldModel;
				const langForm = this.fb.group({
					shortname: [
						fm?.shortname?.[lang.languageCode] || '',
						lang.isDefault ? Validators.required : []
					],
					longname: [fm?.longname?.[lang.languageCode] || ''],
					description: [fm?.description?.[lang.languageCode] || '']
				});
				this.languageForms.set(lang.languageCode, langForm);
			}
		});
	}

	getLanguageLabel(code: string, isDefault: boolean): string {
		const name = this.getLanguageName(code);
		return isDefault ? `${name} ☆` : name;
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
		const currentFieldModelId = this.data.fieldModel?.fieldModelId;
		return this.fieldModelManager.getAll().some(fm =>
			fm.id.toUpperCase() === code.toUpperCase() && fm.fieldModelId !== currentFieldModelId
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
			this.snackBar.open(`A field model with code "${code}" already exists`, 'Close', {duration: 3000});
			return;
		}

		const shortname: Record<string, string> = {};
		const longname: Record<string, string> = {};
		const description: Record<string, string> = {};

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
		});

		const typesWithPossibleValues = ['AUTO_COMPLETION', 'SELECT', 'RADIO', 'CHECKBOX_GROUP'];

		const result: any = {
			id: code,
			shortname,
			longname,
			description,
			type: formValue.type,
			dataType: formValue.dataType,
			readOnly: formValue.readOnly
		};

		if(!typesWithPossibleValues.includes(formValue.type)) {
			result.possibleValues = [];
		}

		this.dialogRef.close(result);
	}
}
