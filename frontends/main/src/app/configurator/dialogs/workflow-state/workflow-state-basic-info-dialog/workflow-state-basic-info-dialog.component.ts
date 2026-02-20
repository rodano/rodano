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
import {ProjectLanguage} from '@core/model/project-language';
import {MatSnackBar} from '@angular/material/snack-bar';
import {WorkflowState} from '@core/model/workflow-state';

export interface WorkflowStateBasicInfoDialogData {
	projectId: string;
	workflowId: string;
	workflowState: WorkflowState | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-workflow-state-basic-info-dialog',
	standalone: true,
	templateUrl: './workflow-state-basic-info-dialog.component.html',
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
export class WorkflowStateBasicInfoDialogComponent implements OnInit {
	form: FormGroup;
	languageForms = new Map<string, FormGroup>();
	availableLanguages: ProjectLanguage[] = [];
	isEditMode: boolean;

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<WorkflowStateBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: WorkflowStateBasicInfoDialogData,
		private snackBar: MatSnackBar
	) {
		this.isEditMode = !!data.workflowState;
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
		const wfs = this.data.workflowState;

		this.form = this.fb.group({
			id: [
				wfs?.id || '',
				[Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]
			],
			workflowStateId: [wfs?.workflowStateId || null],
			important: [wfs?.important || false],
			color: [wfs?.color || null, Validators.required],
			icon: [wfs?.icon || null]
		});
	}

	initializeLanguageForms(): void {
		this.availableLanguages.forEach((lang: ProjectLanguage) => {
			if(lang.languageCode) {
				const wfs = this.data.workflowState;
				const langForm = this.fb.group({
					shortname: [
						wfs?.shortname?.[lang.languageCode] || '',
						lang.isDefault ? Validators.required : []
					],
					longname: [wfs?.longname?.[lang.languageCode] || ''],
					description: [wfs?.description?.[lang.languageCode] || '']
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

	get iconPreview(): string {
		return this.form.get('icon')?.value?.trim() || '';
	}

	onColorInput(event: Event): void {
		const input = event.target as HTMLInputElement;
		const value = input.value;

		if(/^#[0-9A-F]{6}$/i.test(value)) {
			this.form.patchValue({color: value});
		}
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
			workflowStateId: formValue.workflowStateId,
			important: formValue.important,
			color: formValue.color,
			icon: formValue.icon,
			shortname,
			longname,
			description
		};

		this.dialogRef.close(result);
	}
}
