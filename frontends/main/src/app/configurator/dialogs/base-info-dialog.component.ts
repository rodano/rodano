import {Directive, Inject} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {ProjectLanguage} from '@core/model/project-language';
import {LanguageService} from '../services/language.service';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {BaseDialogComponent} from './base-dialog.component';

@Directive()
export abstract class BaseInfoDialogComponent extends BaseDialogComponent {
	languageForms = new Map<string, FormGroup>();
	availableLanguages: ProjectLanguage[] = [];

	protected constructor(
		protected fb: FormBuilder,
		protected languageService: LanguageService,
		dialogRef: MatDialogRef<any>,
		@Inject(MAT_DIALOG_DATA) data: any,
		snackBar?: MatSnackBar
	) {
		super(dialogRef, data, snackBar);
	}

	protected loadProjectLanguages(languages: ProjectLanguage[]): void {
		this.availableLanguages = languages?.length
			? languages
			: [{languageCode: 'en', isDefault: true}];
		this.initializeLanguageForms();
	}

	protected abstract initializeLanguageForms(): void;

	getLanguageLabel(code: string, isDefault: boolean): string {
		const name = this.languageService.getLanguageName(code);
		return isDefault ? `${name} ☆` : name;
	}

	areLanguageFormsValid(): boolean {
		return Array.from(this.languageForms.values()).every(f => f.valid);
	}

	protected collectTranslations(): {
		shortname: Record<string, string>;
		longname: Record<string, string>;
		description: Record<string, string>;
	} {
		const shortname: Record<string, string> = {};
		const longname: Record<string, string> = {};
		const description: Record<string, string> = {};

		this.languageForms.forEach((langForm, langCode) => {
			const v = langForm.value;
			if(v.shortname) {
				shortname[langCode] = v.shortname;
			}
			if(v.longname) {
				longname[langCode] = v.longname;
			}
			if(v.description) {
				description[langCode] = v.description;
			}
		});

		return {shortname, longname, description};
	}

	onCodeInput(event: Event, form: FormGroup, controlName = 'id'): void {
		const input = event.target as HTMLInputElement;
		const uppercased = input.value.toUpperCase();
		input.value = uppercased;
		form.patchValue({[controlName]: uppercased}, {emitEvent: false});
	}
}
