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
import {HttpErrorResponse} from '@angular/common/http';
import {MatSnackBar} from '@angular/material/snack-bar';
import {EventModelService} from '../../../services/api/event-model.service';

export interface EventModelBasicInfoDialogData {
	projectId: string;
	scopeModelId: string;
	eventModel: EventModel | null;
	languages: ProjectLanguage[];
	eventGroups: {id: string; name: string; code: string}[];
}

@Component({
	selector: 'app-event-model-basic-info-dialog',
	standalone: true,
	templateUrl: './event-model-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
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
	isEditMode: boolean;
	saving = false;
	allEventModels: EventModel[] = [];

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<EventModelBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: EventModelBasicInfoDialogData,
		private eventModelService: EventModelService,
		private snackBar: MatSnackBar
	) {
		this.isEditMode = !!data.eventModel;
		this.eventGroups = data.eventGroups || [];
	}

	ngOnInit(): void {
		this.loadProjectLanguages();
		this.loadAllEventModels();
		this.initializeForm();
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

	initializeForm(): void {
		const em = this.data.eventModel;

		this.form = this.fb.group({
			id: [
				em?.id || '',
				[Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]
			],
			number: [em?.number || null],
			eventGroupId: [em?.eventGroupId || null],
			mandatory: [em?.mandatory || false],
			inceptive: [em?.inceptive || false],
			maxOccurrence: [em?.maxOccurrence || null],
			preventAdd: [em?.preventAdd || false]
		});
	}

	initializeLanguageForms(): void {
		this.availableLanguages.forEach((lang: ProjectLanguage) => {
			if(lang.languageCode) {
				const em = this.data.eventModel;
				const langForm = this.fb.group({
					shortname: [
						em?.shortname?.[lang.languageCode] || '',
						lang.isDefault ? Validators.required : []
					],
					longname: [em?.longname?.[lang.languageCode] || ''],
					description: [em?.description?.[lang.languageCode] || '']
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
			console.error(error);
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

	onIdInput(event: Event): void {
		const input = event.target as HTMLInputElement;
		const uppercaseValue = input.value.toUpperCase();
		input.value = uppercaseValue;
		this.form.patchValue({id: uppercaseValue}, {emitEvent: false});
	}

	isCodeDuplicate(code: string): boolean {
		const currentEventModelId = this.data.eventModel?.eventModelId;
		return this.allEventModels.some(em =>
			em.id.toUpperCase() === code.toUpperCase() && em.eventModelId !== currentEventModelId
		);
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const formValue = this.form.getRawValue();
		const code = formValue.id.toUpperCase();

		if(this.isCodeDuplicate(code)) {
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
			number: formValue.number,
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
