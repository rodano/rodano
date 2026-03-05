import {Workflow} from '@core/model/workflow';
import {ProjectLanguage} from '@core/model/project-language';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatTabsModule} from '@angular/material/tabs';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface WorkflowMiscDialogData {
	workflow: Workflow;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-workflow-misc-dialog',
	standalone: true,
	templateUrl: './workflow-misc-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatIconModule, MatTabsModule]
})
export class WorkflowMiscDialogComponent extends BaseDialogComponent<WorkflowMiscDialogData> implements OnInit {
	form: FormGroup;
	languageForms = new Map<string, FormGroup>();
	availableLanguages: ProjectLanguage[] = [];

	constructor(
		private fb: FormBuilder,
		dialogRef: MatDialogRef<WorkflowMiscDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: WorkflowMiscDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.availableLanguages = this.data.languages?.length
			? this.data.languages
			: [{languageCode: 'en', isDefault: true}];

		this.initializeForm();
		this.initializeLanguageForms();
	}

	private initializeForm(): void {
		this.form = this.fb.group({
			icon: [this.data.workflow.icon ?? '']
		});
	}

	private initializeLanguageForms(): void {
		this.availableLanguages.forEach((lang: ProjectLanguage) => {
			if(lang.languageCode) {
				const langForm = this.fb.group({
					message: [this.data.workflow.message?.[lang.languageCode] ?? '']
				});
				this.languageForms.set(lang.languageCode, langForm);
			}
		});
	}

	get iconPreview(): string {
		return this.form.get('icon')?.value?.trim() || '';
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

	onSave(): void {
		const formValue = this.form.getRawValue();
		const wf = this.data.workflow;

		const normalize = (value: any) =>
			value === '' || value === null || value === undefined ? null : value;

		const result: any = {};

		const newIcon = normalize(formValue.icon);
		if(newIcon !== normalize(wf.icon)) {
			result.icon = newIcon;
		}

		const message: Record<string, string> = {};
		this.languageForms.forEach((langForm: FormGroup, langCode: string) => {
			const value = langForm.value.message;
			if(value) {
				message[langCode] = value;
			}
		});

		if(JSON.stringify(message) !== JSON.stringify(wf.message ?? {})) {
			result.message = message;
		}

		this.dialogRef.close(Object.keys(result).length > 0 ? result : null);
	}
}
