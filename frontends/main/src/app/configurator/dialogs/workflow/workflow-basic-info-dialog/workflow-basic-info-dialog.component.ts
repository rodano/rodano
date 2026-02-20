import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {CommonModule} from '@angular/common';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatSelectModule} from '@angular/material/select';
import {MatTabsModule} from '@angular/material/tabs';
import {MatInputModule} from '@angular/material/input';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ProjectLanguage} from '@core/model/project-language';
import {Workflow} from '@core/model/workflow';
import {WorkflowManagerService} from '../../../services/manager/workflow-manager.service';

interface DialogData {
	projectId: string;
	workflow: Workflow | null;
	languages: ProjectLanguage[];
	workflowActions: {id: string; name: string; code: string};
}

@Component({
	selector: 'app-workflow-basic-info-dialog',
	standalone: true,
	templateUrl: './workflow-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatFormFieldModule,
		MatInputModule,
		MatButtonModule,
		MatCheckboxModule,
		MatSelectModule,
		MatTabsModule
	]
})
export class WorkflowBasicInfoDialogComponent implements OnInit {
	form: FormGroup;
	languageForms = new Map<string, FormGroup>();
	availableLanguages: ProjectLanguage[] = [];
	workflowActions: {id: string; name: string; code: string};
	isEditMode: boolean;

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<WorkflowBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: DialogData,
		private workflowManager: WorkflowManagerService,
		private snackBar: MatSnackBar
	) {
		this.isEditMode = !!data.workflow;
		this.workflowActions = data.workflowActions || [];
	}

	ngOnInit(): void {
		this.loadProjectLanguages();
		this.initializeForm();
	}

	loadProjectLanguages(): void {
		this.availableLanguages = this.data.languages || [];

		if(this.availableLanguages.length === 0) {
			this.availableLanguages = [{languageCode: 'en', isDefault: true}];
		}

		this.initializeLanguageForms();
	}

	initializeForm(): void {
		const wf = this.data.workflow;

		this.form = this.fb.group({
			id: [
				wf?.id || '',
				[Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]
			],
			order: [wf?.order || null]
		});
	}

	initializeLanguageForms(): void {
		this.availableLanguages.forEach((lang: ProjectLanguage) => {
			if(lang.languageCode) {
				const wf = this.data.workflow;
				const langForm = this.fb.group({
					shortname: [
						wf?.shortname?.[lang.languageCode] || '',
						lang.isDefault ? Validators.required : []
					],
					longname: [wf?.longname?.[lang.languageCode] || ''],
					description: [wf?.description?.[lang.languageCode] || ''],
					message: [wf?.message?.[lang.languageCode] || '']
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

	onCodeInput(event: Event): void {
		const input = event.target as HTMLInputElement;
		const uppercaseValue = input.value.toUpperCase();
		input.value = uppercaseValue;
		this.form.patchValue({id: uppercaseValue}, {emitEvent: false});
	}

	isCodeDuplicate(code: string): boolean {
		const currentWorkflowId = this.data.workflow?.workflowId;
		return this.workflowManager.getAll().some(wf =>
			wf.id.toUpperCase() === code.toUpperCase() && wf.workflowId !== currentWorkflowId
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
			this.snackBar.open(`A workflow with code "${code}" already exists`, 'Close', {duration: 3000});
			return;
		}

		const shortname: Record<string, string> = {};
		const longname: Record<string, string> = {};
		const description: Record<string, string> = {};
		const message: Record<string, string> = {};

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
			if(langValue.message) {
				message[langCode] = langValue.pluralShortname;
			}
		});

		const result = {
			id: code,
			shortname,
			longname,
			description,
			message,
			order: formValue.order
		};

		this.dialogRef.close(result);
	}
}
