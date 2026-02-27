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
import {PrivacyPolicy} from '@core/model/privacy-policy';
import {PrivacyPolicyManagerService} from '../../../services/manager/privacy-policy-manager.service';
import {WysiwygEditorComponent} from '../../../shared/wysiwyg-editor/wysiwyg-editor.component';

export interface PrivacyPolicyBasicInfoDialogData {
	projectId: string;
	privacyPolicy: PrivacyPolicy | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-privacy-policy-basic-info-dialog',
	standalone: true,
	templateUrl: './privacy-policy-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatTabsModule,
		MatSelectModule,
		WysiwygEditorComponent
	]
})
export class PrivacyPolicyBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode: boolean;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<PrivacyPolicyBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: PrivacyPolicyBasicInfoDialogData,
		private privacyPolicyManager: PrivacyPolicyManagerService,
		private snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef);
		this.isEditMode = !!data.privacyPolicy;
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
			const pp = this.data.privacyPolicy;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [pp?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [pp?.longname?.[lang.languageCode] || ''],
				description: [pp?.description?.[lang.languageCode] || ''],
				content: [pp?.content?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []]
			}));
		});
	}

	initializeForm(): void {
		const pp = this.data.privacyPolicy;
		this.form = this.fb.group({
			id: [pp?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentPrivacyPolicyId = this.data.privacyPolicy?.policyId;
		return this.privacyPolicyManager.getAll().some(pp =>
			pp.id.toUpperCase() === code.toUpperCase() && pp.policyId !== currentPrivacyPolicyId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.snackBar.open(`A privacy policy with code "${code}" already exists`, 'Close', {duration: 3000});
			return;
		}

		const {shortname, longname, description} = this.collectTranslations();
		const content: Record<string, string> = {};

		this.languageForms.forEach((langForm, langCode) => {
			const v = langForm.value;
			if(v.content) {
				content[langCode] = v.content;
			}
		});

		this.dialogRef.close({
			id: code,
			shortname,
			longname,
			description,
			content
		});
	}
}
