import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormArray, FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatSelectModule} from '@angular/material/select';
import {MatTabsModule} from '@angular/material/tabs';
import {WorkflowAction} from '@core/model/workflow-action';
import {ProjectLanguage} from '@core/model/project-language';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface WorkflowActionDocumentationDialogData {
	workflowAction: WorkflowAction;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-workflow-action-documentation-dialog',
	standalone: true,
	templateUrl: './workflow-action-documentation-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatIconModule, MatCheckboxModule,
		MatSelectModule, MatTabsModule]
})
export class WorkflowActionDocumentationDialogComponent extends BaseDialogComponent<WorkflowActionDocumentationDialogData> implements OnInit {
	form: FormGroup;
	availableLanguages: ProjectLanguage[] = [];

	constructor(
		private fb: FormBuilder,
		dialogRef: MatDialogRef<WorkflowActionDocumentationDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: WorkflowActionDocumentationDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.availableLanguages = this.data.languages?.length
			? this.data.languages
			: [{languageCode: 'en', isDefault: true}];

		const existingOptions = this.data.workflowAction.documentableOptions || [];

		this.form = this.fb.group({
			documentable: [this.data.workflowAction.documentable ?? false],
			options: this.fb.array(existingOptions.map(opt => this.createOptionGroup(opt)))
		});
	}

	get documentable(): boolean {
		return !!this.form.get('documentable')?.value;
	}

	get options(): FormArray {
		return this.form.get('options') as FormArray;
	}

	private createOptionGroup(existing?: Record<string, string>): FormGroup {
		const group: Record<string, any> = {};
		this.availableLanguages.forEach(lang => {
			if(lang.languageCode) {
				group[lang.languageCode] = [existing?.[lang.languageCode] || ''];
			}
		});
		return this.fb.group(group);
	}

	addOption(): void {
		this.options.push(this.createOptionGroup());
	}

	removeOption(index: number): void {
		this.options.removeAt(index);
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
		const formValue = this.form.getRawValue();
		const documentable = formValue.documentable;

		const result: any = {};

		if(documentable !== this.data.workflowAction.documentable) {
			result.documentable = documentable;
		}

		if(!documentable) {
			result.documentableOptions = null;
		}
		else {
			const newOptions: Record<string, string>[] = formValue.options
				.map((opt: Record<string, string>) => {
					const filtered: Record<string, string> = {};
					Object.entries(opt).forEach(([k, v]) => {
						if(v) {
							filtered[k] = v;
						}
					});
					return filtered;
				})
				.filter((opt: Record<string, string>) => Object.keys(opt).length > 0);

			const originalOptions = JSON.stringify(this.data.workflowAction.documentableOptions || []);
			if(JSON.stringify(newOptions) !== originalOptions) {
				result.documentableOptions = newOptions.length > 0 ? newOptions : null;
			}
		}

		this.dialogRef.close(Object.keys(result).length > 0 ? result : null);
	}
}
