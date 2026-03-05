import {Component, Inject, OnInit} from '@angular/core';
import {ScopeModel} from '@core/model/scope-model';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {CommonModule} from '@angular/common';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatSelectModule} from '@angular/material/select';
import {MatTabsModule} from '@angular/material/tabs';
import {MatInputModule} from '@angular/material/input';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ProjectLanguage} from '@core/model/project-language';
import {ScopeModelManagerService} from '../../../services/manager/scope-model-manager.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';
import {LanguageService} from '../../../services/language.service';

interface ScopeModelBasicInfoDialogData {
	projectId: string;
	scopeModel: ScopeModel | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-scope-model-basic-info-dialog',
	standalone: true,
	templateUrl: './scope-model-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule,
		MatCheckboxModule, MatSelectModule, MatTabsModule]
})
export class ScopeModelBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode: boolean;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<ScopeModelBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: ScopeModelBasicInfoDialogData,
		private scopeModelManager: ScopeModelManagerService,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
		this.isEditMode = !!data.scopeModel;
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
			const sm = this.data.scopeModel;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [sm?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [sm?.longname?.[lang.languageCode] || ''],
				description: [sm?.description?.[lang.languageCode] || ''],
				pluralShortname: [sm?.pluralShortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []]
			}));
		});
	}

	initializeForm(): void {
		const sm = this.data.scopeModel;
		this.form = this.fb.group({
			id: [sm?.id || '', [Validators.required]],
			virtual: [sm?.virtual ?? false],
			expectedNumber: [sm?.expectedNumber ?? null, [Validators.min(0)]],
			maxNumber: [sm?.maxNumber ?? null, [Validators.min(0)]]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentScopeModelId = this.data.scopeModel?.scopeModelId;
		return this.scopeModelManager.getAll().some(sm =>
			sm.id.toUpperCase() === code.toUpperCase() && sm.scopeModelId !== currentScopeModelId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.showError();
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(!this.isEditMode && this.isCodeDuplicate(code)) {
			this.showError(`A scope model with code "${code}" already exists`);
			return;
		}

		const {shortname, longname, description} = this.collectTranslations();
		const pluralShortname: Record<string, string> = {};

		this.languageForms.forEach((langForm, langCode) => {
			const v = langForm.value;
			if(v.pluralShortname) {
				pluralShortname[langCode] = v.pluralShortname;
			}
		});

		this.dialogRef.close({
			...this.form.value,
			id: code,
			shortname,
			longname,
			description,
			pluralShortname
		});
	}
}
