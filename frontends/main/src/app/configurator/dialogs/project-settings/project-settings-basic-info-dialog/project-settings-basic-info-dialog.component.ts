import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {ProjectLanguage} from '@core/model/project-language';
import {LanguageService} from '../../../services/language.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';
import {MatSnackBar} from '@angular/material/snack-bar';

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
export class ProjectSettingsBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<ProjectSettingsBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: BasicInfoDialogData,
		private snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef);
	}

	ngOnInit(): void {
		this.loadProjectLanguages(this.data.languages);
		this.initializeForm();
	}

	protected initializeLanguageForms(): void {
		this.availableLanguages.forEach(lang => {
			if(!lang.languageCode) {
				return;
			}
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [this.data.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [this.data.longname?.[lang.languageCode] || ''],
				description: [this.data.description?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		this.form = this.fb.group({
			code: [this.data.code, [Validators.required, Validators.pattern(/^[A-Z0-9_]+$/)]],
			url: [this.data.url, [Validators.pattern(/^https?:\/\/.+/)]],
			color: [this.data.color || '#5bd4d4']
		});
	}

	onColorInput(event: Event): void {
		const input = event.target as HTMLInputElement;
		const value = input.value;

		if(/^#[0-9A-F]{6}$/i.test(value)) {
			this.form.patchValue({color: value});
		}
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const {shortname, longname, description} = this.collectTranslations();
		this.dialogRef.close({
			code: this.form.value.code,
			shortname,
			longname,
			description,
			...this.form.value
		});
	}
}
