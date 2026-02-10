import {ProjectLanguage} from '@core/model/project-language';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import ISO6391 from 'iso-639-1';
import {MatSelectModule} from '@angular/material/select';

export interface LanguagesDialogData {
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-edit-languages-dialog',
	standalone: true,
	templateUrl: './project-settings-languages-dialog.component.html',
	styleUrls: ['./project-settings-languages-dialog.component.css'],
	imports: [
		CommonModule,
		MatDialogModule,
		MatIconModule,
		MatTooltipModule,
		MatSelectModule
	]
})
export class ProjectSettingsLanguagesDialogComponent implements OnInit {
	selectedLanguages: {code: string; name: string; isDefault: boolean}[] = [];

	constructor(
		private dialogRef: MatDialogRef<ProjectSettingsLanguagesDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: LanguagesDialogData
	) {}

	ngOnInit(): void {
		if(this.data.languages && this.data.languages.length > 0) {
			this.selectedLanguages = this.data.languages.map(lang => ({
				code: lang.languageCode || '',
				name: this.getLanguageName(lang.languageCode),
				isDefault: lang.isDefault || false
			}));
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

	onLanguageSelected(event: Event): void {
		const select = event.target as HTMLSelectElement;
		const code = select.value;

		if(code) {
			this.addLanguage(code);
			select.value = '';
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

		const isFirstLanguage = this.selectedLanguages.length === 0;

		this.selectedLanguages.push({
			code: code,
			name: this.getLanguageName(code),
			isDefault: isFirstLanguage
		});
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
	}

	setDefaultLanguage(code: string): void {
		this.selectedLanguages.forEach(lang => lang.isDefault = false);

		const lang = this.selectedLanguages.find(l => l.code === code);
		if(lang) {
			lang.isDefault = true;
		}
	}

	getLanguageName(code: string | undefined): string {
		if(!code) {
			return 'Unknown';
		}
		try {
			const displayNames = new Intl.DisplayNames(['en'], {type: 'language'});
			return displayNames.of(code) || code.toUpperCase();
		}
		catch (e) {
			console.error(e);
			return code.toUpperCase();
		}
	}

	isValid(): boolean {
		if(this.selectedLanguages.length === 0) {
			return false;
		}

		const defaultCount = this.selectedLanguages.filter(l => l.isDefault).length;
		return defaultCount === 1;
	}

	onSave(): void {
		if(!this.isValid()) {
			return;
		}

		const result: ProjectLanguage[] = this.selectedLanguages.map(lang => ({
			languageCode: lang.code,
			isDefault: lang.isDefault
		}));

		this.dialogRef.close(result);
	}

	onCancel(): void {
		this.dialogRef.close();
	}
}
