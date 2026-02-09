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
import {EventModel} from '@core/model/event-model';
import {ProjectLanguage} from '@core/model/project-language';

interface EventModelBasicInfoDialogData {
	eventModel: EventModel;
	languages: ProjectLanguage[];
	eventGroups: {id: string; name: string; code: string}[];
}

@Component({
	selector: 'app-event-model-basic-info-dialog',
	standalone: true,
	templateUrl: './event-model-basic-info-dialog.component.html',
	styleUrls: ['../shared-scope-model-dialog-styles.css'],
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
export class EventModelBasicInfoDialogComponent implements OnInit {
	form: FormGroup;
	languageForms = new Map<string, FormGroup>();
	availableLanguages: ProjectLanguage[] = [];
	eventGroups: {id: string; name: string; code: string}[];

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<EventModelBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: EventModelBasicInfoDialogData
	) {
		this.eventGroups = data.eventGroups || [];
	}

	ngOnInit(): void {
		this.loadProjectLanguages();

		this.form = this.fb.group({
			id: [this.data.eventModel.id, Validators.required],
			number: [this.data.eventModel.number],
			icon: [this.data.eventModel.icon],
			eventGroupId: [this.data.eventModel.eventGroupId],
			mandatory: [this.data.eventModel.mandatory || false],
			inceptive: [this.data.eventModel.inceptive || false],
			maxOccurrence: [this.data.eventModel.maxOccurrence || null],
			preventAdd: [this.data.eventModel.preventAdd || false]
		});
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
					shortname: [this.data.eventModel.shortname?.[lang.languageCode] || ''],
					longname: [this.data.eventModel.longname?.[lang.languageCode] || ''],
					description: [this.data.eventModel.description?.[lang.languageCode] || '']
				});
				this.languageForms.set(lang.languageCode, langForm);
			}
		});
	}

	getLanguageLabel(code: string, isDefault: boolean): string {
		const name = this.getLanguageName(code);
		return isDefault ? `${name} ★` : name;
	}

	getLanguageName(code: string): string {
		try {
			const displayNames = new Intl.DisplayNames(['en'], {type: 'language'});
			return displayNames.of(code) || code.toUpperCase();
		}
		catch (error) {
			console.log(error);
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

	onCancel(): void {
		this.dialogRef.close(null);
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
			number: formValue.number,
			icon: formValue.icon,
			eventGroupId: formValue.eventGroupId,
			shortname,
			longname,
			description,
			mandatory: formValue.mandatory,
			inceptive: formValue.inceptive,
			maxOccurrence: formValue.maxOccurrence,
			preventAdd: formValue.preventAdd
		};

		this.dialogRef.close(result);
	}
}
