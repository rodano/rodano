import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatTabsModule} from '@angular/material/tabs';
import {WorkflowAction} from '@core/model/workflow-action';
import {ProjectLanguage} from '@core/model/project-language';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface WorkflowActionSignatureDialogData {
	workflowAction: WorkflowAction;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-workflow-action-signature-dialog',
	standalone: true,
	templateUrl: './workflow-action-signature-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatCheckboxModule, MatTabsModule]
})
export class WorkflowActionSignatureDialogComponent extends BaseDialogComponent<WorkflowActionSignatureDialogData> implements OnInit {
	form: FormGroup;
	signatureTextForms = new Map<string, FormGroup>();
	availableLanguages: ProjectLanguage[] = [];

	constructor(
		private fb: FormBuilder,
		dialogRef: MatDialogRef<WorkflowActionSignatureDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: WorkflowActionSignatureDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.availableLanguages = this.data.languages?.length
			? this.data.languages
			: [{languageCode: 'en', isDefault: true}];

		this.form = this.fb.group({
			requireSignature: [this.data.workflowAction.requireSignature ?? false]
		});

		this.availableLanguages.forEach(lang => {
			if(lang.languageCode) {
				this.signatureTextForms.set(lang.languageCode, this.fb.group({
					text: [this.data.workflowAction.requireSignatureText?.[lang.languageCode] || '']
				}));
			}
		});
	}

	get requireSignature(): boolean {
		return !!this.form.get('requireSignature')?.value;
	}

	getLanguageLabel(code: string, isDefault: boolean): string {
		try {
			const name = new Intl.DisplayNames(['en'], {type: 'language'}).of(code) || code.toUpperCase();
			return isDefault ? `${name} ☆` : name;
		}
		catch {
			return code.toUpperCase();
		}
	}

	onSave(): void {
		const result: any = {};
		const requireSignature = this.form.value.requireSignature;

		if(requireSignature !== this.data.workflowAction.requireSignature) {
			result.requireSignature = requireSignature;
		}

		if(!requireSignature) {
			result.requireSignatureText = null;
		}
		else {
			const requireSignatureText: Record<string, string> = {};
			this.signatureTextForms.forEach((langForm, langCode) => {
				const val = langForm.value.text;
				if(val) {
					requireSignatureText[langCode] = val;
				}
			});

			if(JSON.stringify(requireSignatureText) !== JSON.stringify(this.data.workflowAction.requireSignatureText || {})) {
				result.requireSignatureText = Object.keys(requireSignatureText).length > 0 ? requireSignatureText : null;
			}
		}

		this.dialogRef.close(Object.keys(result).length > 0 ? result : null);
	}
}
