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
import {MatSnackBar} from '@angular/material/snack-bar';
import {EventModelManagerService} from '../../../services/manager/event-model-manager.service';
import {LanguageService} from '../../../services/language.service';
import {EventGroup} from '@core/model/event-group';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';

export interface EventModelBasicInfoDialogData {
	projectId: string;
	scopeModelId: string;
	eventModel: EventModel | null;
	languages: ProjectLanguage[];
	eventGroups: EventGroup[];
}

@Component({
	selector: 'app-event-model-basic-info-dialog',
	standalone: true,
	templateUrl: './event-model-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule,
		MatIconModule, MatCheckboxModule, MatTabsModule, MatSelectModule]
})
export class EventModelBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	eventGroups: EventGroup[] = [];
	isEditMode: boolean;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<EventModelBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: EventModelBasicInfoDialogData,
		private eventModelManager: EventModelManagerService,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
		this.isEditMode = !!data.eventModel;
		this.eventGroups = data.eventGroups || [];
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
			const em = this.data.eventModel;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [em?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [em?.longname?.[lang.languageCode] || ''],
				description: [em?.description?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		const em = this.data.eventModel;
		this.form = this.fb.group({
			id: [em?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			number: [em?.number || null],
			eventGroupId: [em?.eventGroupId || null],
			mandatory: [em?.mandatory || false],
			inceptive: [em?.inceptive || false],
			maxOccurrence: [em?.maxOccurrence || null],
			preventAdd: [em?.preventAdd || false]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentEventModelId = this.data.eventModel?.eventModelId;
		return this.eventModelManager.getAll().some(em =>
			em.id.toUpperCase() === code.toUpperCase() && em.eventModelId !== currentEventModelId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.showError();
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.showError(`An event model with code "${code}" already exists`);
			return;
		}

		const {shortname, longname, description} = this.collectTranslations();
		this.dialogRef.close({
			...this.form.value,
			id: code,
			shortname,
			longname,
			description
		});
	}
}
