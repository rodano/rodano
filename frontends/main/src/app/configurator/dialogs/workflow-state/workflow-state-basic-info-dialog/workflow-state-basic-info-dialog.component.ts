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
import {LanguageService} from '../../../services/language.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';

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
export class WorkflowStateBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode: boolean;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<WorkflowStateBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: WorkflowStateBasicInfoDialogData,
		private snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef);
		this.isEditMode = !!data.workflowState;
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
			const wfs = this.data.workflowState;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [wfs?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [wfs?.longname?.[lang.languageCode] || ''],
				description: [wfs?.description?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		const wfs = this.data.workflowState;
		this.form = this.fb.group({
			id: [wfs?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			workflowStateId: [wfs?.workflowStateId || null],
			important: [wfs?.important || false],
			color: [wfs?.color || null, Validators.required],
			icon: [wfs?.icon || null]
		});
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

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const {shortname, longname, description} = this.collectTranslations();
		this.dialogRef.close({
			...this.form.value,
			shortname,
			longname,
			description
		});
	}
}
