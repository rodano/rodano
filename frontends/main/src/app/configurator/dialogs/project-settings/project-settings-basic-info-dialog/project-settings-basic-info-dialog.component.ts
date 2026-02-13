import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {ProjectLanguage} from '@core/model/project-language';

export interface BasicInfoDialogData {
	code: string;
	shortname: Record<string, string>;
	longname: Record<string, string>;
	description: Record<string, string>;
	url: string | null;
	color: string;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-edit-basic-info-dialog',
	standalone: true,
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatTabsModule
	],
	templateUrl: './project-settings-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css']
})
export class ProjectSettingsBasicInfoDialogComponent implements OnInit {
	form: FormGroup;
	languageForms = new Map<string, FormGroup>();
	availableLanguages: ProjectLanguage[] = [];

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<ProjectSettingsBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: BasicInfoDialogData
	) {
		this.form = this.fb.group({
			code: [data.code, [Validators.required, Validators.pattern(/^[A-Z0-9_]+$/)]],
			url: [data.url, [Validators.pattern(/^https?:\/\/.+/)]],
			color: [data.color || '#5bd4d4']
		});
	}

	ngOnInit(): void {
		this.availableLanguages = this.data.languages?.length > 0
			? this.data.languages.filter(lang => lang && lang.languageCode)
			: [{languageCode: 'en', isDefault: true}];

		this.availableLanguages.forEach(lang => {
			if(!lang.languageCode) {
				return;
			}

			const languageForm = this.fb.group({
				shortname: [this.data.shortname?.[lang.languageCode] || ''],
				longname: [this.data.longname?.[lang.languageCode] || ''],
				description: [this.data.description?.[lang.languageCode] || '']
			});
			this.languageForms.set(lang.languageCode, languageForm);
		});
	}

	getLanguageLabel(languageCode: string, isDefault: boolean): string {
		try {
			const displayNames = new Intl.DisplayNames([navigator.language, 'en'], {type: 'language'});
			const languageName = displayNames.of(languageCode) || languageCode.toUpperCase();

			return isDefault ? `${languageName} (Default)` : languageName;
		}
		catch (error) {
			console.error('Error getting language label', error);
			return isDefault ? `${languageCode.toUpperCase()} (Default)` : languageCode.toUpperCase();
		}
	}

	onCancel(): void {
		this.dialogRef.close();
	}

	onSave(): void {
		if(this.form.invalid) {
			return;
		}

		const formValue = this.form.value;

		const shortname: Record<string, string> = {};
		const longname: Record<string, string> = {};
		const description: Record<string, string> = {};

		this.languageForms.forEach((langForm, languageCode) => {
			const langValue = langForm.value;
			if(langValue.shortname) {
				shortname[languageCode] = langValue.shortname;
			}
			if(langValue.longname) {
				longname[languageCode] = langValue.longname;
			}
			if(langValue.description) {
				description[languageCode] = langValue.description;
			}
		});

		const result = {
			code: formValue.code,
			shortname,
			longname,
			description,
			url: formValue.url,
			color: formValue.color
		};

		this.dialogRef.close(result);
	}

	onColorInput(event: Event): void {
		const input = event.target as HTMLInputElement;
		const value = input.value;

		if(/^#[0-9A-F]{6}$/i.test(value)) {
			this.form.patchValue({color: value});
		}
	}

	onCodeInput(event: Event): void {
		const input = event.target as HTMLInputElement;
		input.value = input.value.toUpperCase();
	}
}
