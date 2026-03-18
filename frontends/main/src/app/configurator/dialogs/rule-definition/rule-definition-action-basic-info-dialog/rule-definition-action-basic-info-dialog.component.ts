import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSelectModule} from '@angular/material/select';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';
import {ENTITY_OPTIONS} from '../entity-options';
import {LanguageService} from '../../../services/language.service';
import {RuleDefinitionAction} from '@core/model/rule-definition-action';
import {RuleDefinitionActionManagerService} from '../../../services/manager/rule-definition-action-manager.service';

export interface RuleDefinitionActionBasicInfoDialogData {
	projectId: string;
	ruleDefinitionAction: RuleDefinitionAction | null;
}

@Component({
	selector: 'app-rule-definition-action-basic-info-dialog',
	standalone: true,
	templateUrl: './rule-definition-action-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatTabsModule, MatSelectModule]
})
export class RuleDefinitionActionBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode: boolean;

	readonly entityOptions = [...ENTITY_OPTIONS];

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<RuleDefinitionActionBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: RuleDefinitionActionBasicInfoDialogData,
		private ruleDefinitionActionManager: RuleDefinitionActionManagerService,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
		this.isEditMode = !!data.ruleDefinitionAction;
	}

	ngOnInit(): void {
		this.initializeForm();
	}

	initializeLanguageForms(): void {/*empty */}

	initializeForm(): void {
		const rda = this.data.ruleDefinitionAction;
		this.form = this.fb.group({
			id: [rda?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			label: [rda?.label || '', Validators.required],
			entity: [rda?.entity || '', Validators.required]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentRuleDefinitionActionId = this.data.ruleDefinitionAction?.ruleDefinitionActionId;
		return this.ruleDefinitionActionManager.getAll().some(rda =>
			rda.id.toUpperCase() === code.toUpperCase() && rda.ruleDefinitionActionId !== currentRuleDefinitionActionId
		);
	}

	onSave(): void {
		if(this.form.invalid) {
			this.showError();
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.showError(`A rule definition action with code "${code}" already exists`);
			return;
		}

		this.dialogRef.close({
			...this.form.value,
			id: code
		});
	}
}
