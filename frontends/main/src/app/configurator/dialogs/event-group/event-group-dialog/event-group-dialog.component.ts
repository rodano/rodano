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
import {LanguageService} from '../../../services/language.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';
import {EventGroupManagerService} from '../../../services/manager/event-group-manager.service';
import {MatSnackBar} from '@angular/material/snack-bar';

export interface EventGroupDialogData {
	projectId: string;
	scopeModelId: string;
	eventGroup: EventGroup | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-event-group-create-dialog',
	standalone: true,
	templateUrl: './event-group-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, ReactiveFormsModule, MatInputModule,
		MatTabsModule]
})
export class EventGroupDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode = false;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<EventGroupDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: EventGroupDialogData,
		private eventGroupManager: EventGroupManagerService,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
		this.isEditMode = !!data.eventGroup;
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
			const eg = this.data.eventGroup;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [eg?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [eg?.longname?.[lang.languageCode] || ''],
				description: [eg?.description?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		const eg = this.data.eventGroup;
		this.form = this.fb.group({
			id: [eg?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentEventGroupId = this.data.eventGroup?.eventGroupId;
		return this.eventGroupManager.getAll().some(eg =>
			eg.id.toUpperCase() === code.toUpperCase() && eg.eventGroupId !== currentEventGroupId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.showError();
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.showError(`An event group with code "${code}" already exists`);
		}

		const {shortname, longname, description} = this.collectTranslations();
		this.dialogRef.close({
			id: code,
			shortname,
			longname,
			description
		});
	}
}
