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
import {LanguageService} from '../../../services/language.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';

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
export class FieldModelBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
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
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<FieldModelBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: FieldModelBasicInfoDialogData,
		private fieldModelManager: FieldModelManagerService,
		private snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef);
		this.isEditMode = !!data.fieldModel;
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
			const fm = this.data.fieldModel;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [fm?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [fm?.longname?.[lang.languageCode] || ''],
				description: [fm?.description?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		const fm = this.data.fieldModel;
		this.form = this.fb.group({
			id: [fm?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			type: [fm?.type || null, Validators.required],
			dataType: [fm?.dataType || null, Validators.required],
			readOnly: [fm?.readOnly || false]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentFieldModelId = this.data.fieldModel?.fieldModelId;
		return this.fieldModelManager.getAll().some(fm =>
			fm.id.toUpperCase() === code.toUpperCase() && fm.fieldModelId !== currentFieldModelId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.snackBar.open(`A field model with code "${code}" already exists`, 'Close', {duration: 3000});
			return;
		}

		const {shortname, longname, description} = this.collectTranslations();
		const typesWithPossibleValues = ['AUTO_COMPLETION', 'SELECT', 'RADIO', 'CHECKBOX_GROUP'];

		const result: any = {
			id: code,
			shortname,
			longname,
			description,
			...this.form.value
		};

		if(!typesWithPossibleValues.includes(this.form.value.type)) {
			result.possibleValues = [];
		}

		this.dialogRef.close(result);
	}
}
