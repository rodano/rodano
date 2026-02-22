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
import {WorkflowManagerService} from '../../../services/manager/workflow-manager.service';
import {Workflow} from '@core/model/workflow';
import {LanguageService} from '../../../services/language.service';

export interface ProfileBasicInfoDialogData {
	projectId: string;
	profile: Profile | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-profile-basic-info-dialog',
	standalone: true,
	templateUrl: './profile-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatTabsModule,
		MatSelectModule
	]
})
export class ProfileBasicInfoDialogComponent implements OnInit {
	form: FormGroup;
	languageForms = new Map<string, FormGroup>();
	availableLanguages: ProjectLanguage[] = [];
	isEditMode: boolean;

	availableWorkflows: Workflow[] = [];

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<ProfileBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: ProfileBasicInfoDialogData,
		private profileManager: ProfileManagerService,
		private workflowManager: WorkflowManagerService,
		private languageService: LanguageService,
		private snackBar: MatSnackBar
	) {
		this.isEditMode = !!data.profile;
	}

	ngOnInit(): void {
		this.loadProjectLanguages();
		this.initializeForm();
		this.availableWorkflows = this.workflowManager.getAll();
	}

	loadProjectLanguages(): void {
		this.availableLanguages = this.data.languages || [];

		if(this.availableLanguages.length === 0) {
			this.availableLanguages = [{languageCode: 'en', isDefault: true}];
		}

		this.initializeLanguageForms();
	}

	initializeForm(): void {
		const p = this.data.profile;

		this.form = this.fb.group({
			id: [
				p?.id || '',
				[Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]
			],
			workflowOfInterestId: [p?.workflowOfInterestId ?? null],
			order: [p?.order ?? null]
		});
	}

	initializeLanguageForms(): void {
		this.availableLanguages.forEach((lang: ProjectLanguage) => {
			if(lang.languageCode) {
				const p = this.data.profile;
				const langForm = this.fb.group({
					shortname: [
						p?.shortname?.[lang.languageCode] || '',
						lang.isDefault ? Validators.required : []
					],
					longname: [p?.longname?.[lang.languageCode] || ''],
					description: [p?.description?.[lang.languageCode] || '']
				});
				this.languageForms.set(lang.languageCode, langForm);
			}
		});
	}

	getLanguageLabel(code: string, isDefault: boolean): string {
		const name = this.getLanguageName(code);
		return isDefault ? `${name} ☆` : name;
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
		const currentProfileId = this.data.profile?.profileId;
		return this.profileManager.getAll().some(p =>
			p.id.toUpperCase() === code.toUpperCase() && p.profileId !== currentProfileId
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
			this.snackBar.open(`A profile with code "${code}" already exists`, 'Close', {duration: 3000});
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
			workflowOfInterestId: formValue.workflowOfInterestId,
			order: formValue.order
		};

		this.dialogRef.close(result);
	}

	getWorkflowLabel(workflow: Workflow): string {
		const name = this.languageService.getDefaultTranslation(workflow.shortname) || workflow.id;
		return `${name} (${workflow.id})`;
	}
}
