import {ProjectLanguage} from '@core/model/project-language';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Profile} from '@core/model/profile';
import {ProfileManagerService} from '../../../services/manager/profile-manager.service';
import {MatSelectModule} from '@angular/material/select';
import {Workflow} from '@core/model/workflow';
import {LanguageService} from '../../../services/language.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';

export interface ProfileBasicInfoDialogData {
	projectId: string;
	profile: Profile | null;
	languages: ProjectLanguage[];
	availableWorkflows: Workflow[];
}

@Component({
	selector: 'app-profile-basic-info-dialog',
	standalone: true,
	templateUrl: './profile-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatTabsModule, MatSelectModule]
})
export class ProfileBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode: boolean;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<ProfileBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: ProfileBasicInfoDialogData,
		private profileManager: ProfileManagerService,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
		this.isEditMode = !!data.profile;
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
			const p = this.data.profile;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [p?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [p?.longname?.[lang.languageCode] || ''],
				description: [p?.description?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		const p = this.data.profile;
		this.form = this.fb.group({
			id: [p?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			workflowOfInterestId: [p?.workflowOfInterestId ?? null],
			order: [p?.order ?? null]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentProfileId = this.data.profile?.profileId;
		return this.profileManager.getAll().some(p =>
			p.id.toUpperCase() === code.toUpperCase() && p.profileId !== currentProfileId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.showError();
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.showError(`A profile with code "${code}" already exists`);
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
