import {EventGroup} from '@core/model/event-group';
import {ProjectLanguage} from '@core/model/project-language';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatInputModule} from '@angular/material/input';
import {MatTabsModule} from '@angular/material/tabs';

export interface EventGroupDialogData {
	projectId: string;
	scopeModelId: string;
	eventGroup: EventGroup | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-event-group-create-dialog',
	standalone: true,
	imports: [
		CommonModule,
		MatDialogModule,
		MatButtonModule,
		MatIconModule,
		ReactiveFormsModule,
		MatInputModule,
		MatTabsModule
	],
	templateUrl: './event-group-dialog.component.html',
	styleUrls: ['../../dialog-shared.css']
})
export class EventGroupDialogComponent implements OnInit {
	form: FormGroup;
	languageForms = new Map<string, FormGroup>();
	isEditMode = false;

	availableLanguages: ProjectLanguage[] = [];

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<EventGroupDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: EventGroupDialogData
	) {
		this.isEditMode = !!data.eventGroup;

		this.form = this.fb.group({
			id: ['', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]]
		});
	}

	ngOnInit(): void {
		this.loadProjectLanguages();
	}

	loadProjectLanguages(): void {
		this.availableLanguages = this.data.languages || [];

		if(this.availableLanguages.length === 0) {
			this.availableLanguages = [{languageCode: 'en', isDefault: true}];
		}

		this.initializeLanguageForms();
	}

	initializeLanguageForms(): void {
		this.availableLanguages.forEach((lang: ProjectLanguage) => {
			if(lang.languageCode) {
				const langForm = this.fb.group({
					shortname: ['', lang.isDefault ? Validators.required : []],
					longname: [''],
					description: ['']
				});
				this.languageForms.set(lang.languageCode, langForm);
			}
		});

		if(this.data.eventGroup) {
			this.populateForm();
		}
	}

	populateForm(): void {
		if(!this.data.eventGroup) {
			return;
		}

		const eventGroup = this.data.eventGroup;

		this.form.patchValue({
			id: eventGroup.id
		});

		this.languageForms.forEach((langForm: FormGroup, langCode: string) => {
			langForm.patchValue({
				shortname: eventGroup.shortname?.[langCode] || '',
				longname: eventGroup.longname?.[langCode] || '',
				description: eventGroup.description?.[langCode] || ''
			});
		});
	}

	onIdInput(event: Event): void {
		const input = event.target as HTMLInputElement;
		const uppercaseValue = input.value.toUpperCase();
		input.value = uppercaseValue;
		this.form.patchValue({id: uppercaseValue}, {emitEvent: false});
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

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			return;
		}

		const formValue = this.form.getRawValue();

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

		const result = {
			id: formValue.id,
			shortname,
			longname,
			description
		};

		this.dialogRef.close(result);
	}

	onCancel(): void {
		this.dialogRef.close(null);
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

	getLanguageLabel(code: string, isDefault: boolean): string {
		const name = this.getLanguageName(code);
		return isDefault ? `${name} ★` : name;
	}
}
