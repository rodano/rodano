import {Component, Inject, Optional } from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ConfiguratorService} from '../../../services/api/configurator.service';
import {CreateProjectRequest} from '@core/model/create-project-request';
import {MatTabsModule} from '@angular/material/tabs';
import ISO6391 from 'iso-639-1';
import {MatSelectChange, MatSelectModule} from '@angular/material/select';

@Component({
	selector: 'app-create-project-dialog',
	standalone: true,
	templateUrl: './create-project-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatIconModule,
		MatProgressSpinnerModule,
		MatTooltipModule,
		MatTabsModule,
		MatSelectModule
	]
})
export class CreateProjectDialogComponent {
	projectForm: FormGroup;
	loading = false;
	cloneMode = false;

	selectedLanguages: {code: string; name: string; isDefault: boolean}[] = [];

	languageForms = new Map<string, FormGroup>();

	constructor(
		private fb: FormBuilder,
		private configuratorService: ConfiguratorService,
		private dialogRef: MatDialogRef<CreateProjectDialogComponent>,
		@Inject(MAT_DIALOG_DATA) @Optional() private data: {cloneMode?: boolean} | null
	) {
		this.projectForm = this.fb.group({
			code: ['', [Validators.required, Validators.pattern(/^[A-Z0-9_]+$/)]],
			url: ['', Validators.pattern(/^https?:\/\/.+/)],
			color: ['#5bd4d4']
		});
		this.cloneMode = data?.cloneMode ?? false;
	}

	onIdInput(event: Event): void {
		const input = event.target as HTMLInputElement;
		const uppercaseValue = input.value.toUpperCase();
		input.value = uppercaseValue;
		this.projectForm.patchValue({code: uppercaseValue}, {emitEvent: false});
	}

	onSubmit(): void {
		if(!this.isFormValid()) {
			this.projectForm.markAllAsTouched();
			this.languageForms.forEach(form => form.markAllAsTouched());
			return;
		}

		const formValue = this.projectForm.value;

		const shortname: Record<string, string> = {};
		const longname: Record<string, string> = {};
		const description: Record<string, string> = {};

		this.languageForms.forEach((form, langCode) => {
			const langValue = form.value;
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

		const languages = this.selectedLanguages.map(lang => ({
			languageCode: lang.code,
			isDefault: lang.isDefault
		}));

		const request: CreateProjectRequest = {
			code: formValue.code,
			shortname: shortname,
			longname: longname,
			description: Object.keys(description).length > 0 ? description : undefined,
			url: formValue.url || undefined,
			color: formValue.color,
			languages: languages
		};

		if(this.cloneMode) {
			this.dialogRef.close(request);
			return;
		}

		this.loading = true;
		this.configuratorService.createProject(request).subscribe({
			next: project => {
				this.loading = false;
				this.dialogRef.close(project);
			},
			error: error => {
				this.loading = false;
				console.error('Error creating project:', error);
				if(error.error?.message?.includes('code already exists')) {
					this.projectForm.get('code')?.setErrors({duplicate: true});
				}
			}
		});
	}

	onCancel(): void {
		this.dialogRef.close();
	}

	onColorInput(event: Event): void {
		const input = event.target as HTMLInputElement;
		let value = input.value.trim();

		if(value && !value.startsWith('#')) {
			value = `#${value}`;
		}

		if(/^#[0-9A-Fa-f]{6}$/.test(value)) {
			this.projectForm.patchValue({color: value});
		}
		else if(value === '#' || value === '') {
			input.value = value;
		}
	}

	removeLanguage(code: string): void {
		const langToRemove = this.selectedLanguages.find(l => l.code === code);
		if(!langToRemove) {
			return;
		}

		if(langToRemove.isDefault && this.selectedLanguages.length > 1) {
			const remaining = this.selectedLanguages.find(l => l.code !== code);
			if(remaining) {
				remaining.isDefault = true;
			}
		}

		this.selectedLanguages = this.selectedLanguages.filter(l => l.code !== code);
		this.languageForms.delete(code);
	}

	setDefaultLanguage(code: string): void {
		this.selectedLanguages.forEach(lang => lang.isDefault = false);

		const lang = this.selectedLanguages.find(l => l.code === code);
		if(lang) {
			lang.isDefault = true;
		}
	}

	getAvailableLanguages(): {code: string; name: string}[] {
		const allCodes = ISO6391.getAllCodes();
		const alreadySelected = this.selectedLanguages.map(l => l.code);

		return allCodes
			.filter(code => !alreadySelected.includes(code))
			.map(code => ({
				code: code,
				name: this.getLanguageName(code)
			}))
			.sort((a, b) => a.name.localeCompare(b.name));
	}

	onLanguageSelected(event: MatSelectChange): void {
		const code = event.value;
		if(code) {
			this.addLanguage(code);
		}
	}

	private addLanguage(code: string): void {
		if(this.selectedLanguages.some(l => l.code === code)) {
			return;
		}

		if(!ISO6391.validate(code)) {
			console.error(`Invalid language code: ${code}`);
			return;
		}

		const isDefault = this.selectedLanguages.length === 0;

		this.selectedLanguages.push({
			code: code,
			name: this.getLanguageName(code),
			isDefault
		});

		this.languageForms.set(code, this.fb.group({
			shortname: ['', Validators.required],
			longname: [''],
			description: ['']
		}));
	}

	getLanguageName(code: string): string {
		try {
			const displayNames = new Intl.DisplayNames(['en'], {type: 'language'});
			return displayNames.of(code) || code.toUpperCase();
		}
		catch (e) {
			console.error(e);
			return code.toUpperCase();
		}
	}

	getLanguageLabel(code: string, isDefault: boolean): string {
		const lang = this.selectedLanguages.find(l => l.code === code);
		return lang ? `${lang.name}${isDefault ? ' (Default)' : ''}` : code;
	}

	isFormValid(): boolean {
		if(this.projectForm.invalid) {
			return false;
		}

		if(this.selectedLanguages.length === 0) {
			return false;
		}

		const defaultCount = this.selectedLanguages.filter(l => l.isDefault).length;
		if(defaultCount !== 1) {
			return false;
		}

		for(const [, form] of this.languageForms) {
			if(form.invalid) {
				return false;
			}
		}

		return true;
	}

	getErrorMessage(fieldName: string): string {
		const field = this.projectForm.get(fieldName);
		if(field?.hasError('required')) {
			return `${this.getFieldLabel(fieldName)} is required`;
		}
		if(field?.hasError('pattern')) {
			if(fieldName === 'code') {
				return 'Only uppercase letters, numbers, and underscores allowed';
			}
			if(fieldName === 'url') {
				return 'Must be a valid URL starting with http:// or https://';
			}
		}
		if(field?.hasError('duplicate')) {
			return 'This project code already exists';
		}
		return '';
	}

	private getFieldLabel(fieldName: string): string {
		const labels: Record<string, string> = {
			code: 'Project code',
			shortnameEn: 'Short name',
			longnameEn: 'Long name',
			url: 'URL'
		};
		return labels[fieldName] || fieldName;
	}
}
