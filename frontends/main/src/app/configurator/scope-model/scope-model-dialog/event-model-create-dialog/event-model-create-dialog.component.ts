import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {HttpErrorResponse} from '@angular/common/http';
import {ProjectLanguage} from '@core/model/project-language';
import {EventModel} from '@core/model/event-model';
import {EventModelService} from '../../../services/event-model.service';

export interface EventModelBasicInfoDialogData {
	projectId: string;
	scopeModelId: string;
	eventModel: EventModel | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-event-model-basic-info-dialog',
	standalone: true,
	imports: [
		CommonModule,
		MatDialogModule,
		MatFormFieldModule,
		MatInputModule,
		MatButtonModule,
		MatCheckboxModule,
		ReactiveFormsModule,
		MatTabsModule
	],
	templateUrl: './event-model-create-dialog.component.html',
	styleUrls: ['../shared-scope-model-dialog-styles.css']
})
export class EventModelCreateDialogComponent implements OnInit {
	form: FormGroup;
	languageForms = new Map<string, FormGroup>();
	isEditMode: boolean;
	saving = false;

	allEventModels: EventModel[] = [];
	availableLanguages: ProjectLanguage[] = [];

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<EventModelCreateDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: EventModelBasicInfoDialogData,
		private eventModelService: EventModelService,
		private snackBar: MatSnackBar
	) {
		this.isEditMode = !!data.eventModel;

		this.form = this.fb.group({
			id: ['', [Validators.required]],
			mandatory: [false],
			inceptive: [false]
		});
	}

	ngOnInit(): void {
		this.loadProjectLanguages();
		this.loadAllEventModels();
	}

	loadProjectLanguages(): void {
		this.availableLanguages = this.data.languages || [];

		if(this.availableLanguages.length === 0) {
			this.availableLanguages = [{languageCode: 'en', isDefault: true}];
		}

		this.initializeLanguageForms();
	}

	loadAllEventModels(): void {
		this.eventModelService.getEventModels(this.data.projectId).subscribe({
			next: (eventModels: EventModel[]) => {
				this.allEventModels = eventModels;
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error loading event models:', error);
			}
		});
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

		if(this.data.eventModel) {
			this.populateForm();
		}
	}

	populateForm(): void {
		if(!this.data.eventModel) {
			return;
		}

		const eventModel = this.data.eventModel;

		this.form.patchValue({
			id: eventModel.id,
			mandatory: eventModel.mandatory,
			inceptive: eventModel.inceptive
		});

		this.languageForms.forEach((langForm: FormGroup, langCode: string) => {
			langForm.patchValue({
				shortname: eventModel.shortname?.[langCode] || '',
				longname: eventModel.longname?.[langCode] || '',
				description: eventModel.description?.[langCode] || ''
			});
		});
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const formValue = this.form.getRawValue();
		const code = formValue.id.toUpperCase();

		if(!this.isEditMode && this.isCodeDuplicate(code)) {
			this.snackBar.open(`An event model with code "${code}" already exists`, 'Close', {duration: 3000});
			return;
		}

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
			id: code,
			shortname,
			longname,
			description,
			mandatory: formValue.mandatory,
			inceptive: formValue.inceptive
		};

		this.dialogRef.close(result);
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

	onCodeInput(event: Event): void {
		const input = event.target as HTMLInputElement;
		const uppercaseValue = input.value.toUpperCase();
		input.value = uppercaseValue;
		this.form.patchValue({id: uppercaseValue}, {emitEvent: false});
	}

	isCodeDuplicate(code: string): boolean {
		const currentEventModelId = this.data.eventModel?.eventModelId;
		return this.allEventModels.some(em =>
			em.id.toUpperCase() === code.toUpperCase() && em.eventModelId !== currentEventModelId);
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
