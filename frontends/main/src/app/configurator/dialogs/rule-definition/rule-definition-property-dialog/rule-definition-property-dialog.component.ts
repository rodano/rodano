import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSelectModule} from '@angular/material/select';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';
import {RuleDefinitionProperty} from '@core/model/rule-definition-property';
import {
	RuleDefinitionPropertyManagerService
} from '../../../services/manager/rule-definition-property-manager.service';
import {ENTITY_OPTIONS} from '../entity-options';
import {CONFIGURATION_ENTITY_OPTIONS} from '../configuration-entity-options';
import {LanguageService} from '../../../services/language.service';

export interface RuleDefinitionPropertyDialogData {
	projectId: string;
	ruleDefinitionProperty: RuleDefinitionProperty | null;
}

interface TypeOption {
	value: string;
	label: string;
}

@Component({
	selector: 'app-rule-definition-property-dialog',
	standalone: true,
	templateUrl: './rule-definition-property-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatTabsModule, MatSelectModule]
})
export class RuleDefinitionPropertyDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode: boolean;

	readonly entityOptions = [...ENTITY_OPTIONS];
	readonly targetOptions = [...ENTITY_OPTIONS];
	readonly configEntityOptions = [...CONFIGURATION_ENTITY_OPTIONS];

	typeOptions: TypeOption[] = [
		{value: 'STRING', label: 'String'},
		{value: 'DATE', label: 'Date'},
		{value: 'NUMBER', label: 'Number'},
		{value: 'BOOLEAN', label: 'Boolean'},
		{value: 'BLOB', label: 'Blob'}
	];

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<RuleDefinitionPropertyDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: RuleDefinitionPropertyDialogData,
		private ruleDefinitionPropertyManager: RuleDefinitionPropertyManagerService,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
		this.isEditMode = !!data.ruleDefinitionProperty;
	}

	ngOnInit(): void {
		this.initializeForm();
	}

	initializeLanguageForms(): void {/*empty */}

	initializeForm(): void {
		const rdp = this.data.ruleDefinitionProperty;
		this.form = this.fb.group({
			id: [rdp?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			label: [rdp?.label || '', Validators.required],
			entity: [rdp?.entity || '', Validators.required],
			target: [rdp?.target || '', Validators.required],
			type: [rdp?.type || '', Validators.required],
			configurationEntity: [rdp?.configurationEntity || '', Validators.required],
			options: [rdp?.options || null]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentRuleDefinitionPropertyId = this.data.ruleDefinitionProperty?.ruleDefinitionPropertyId;
		return this.ruleDefinitionPropertyManager.getAll().some(rdp =>
			rdp.id.toUpperCase() === code.toUpperCase() && rdp.ruleDefinitionPropertyId !== currentRuleDefinitionPropertyId
		);
	}

	onSave(): void {
		if(this.form.invalid) {
			this.showError();
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.showError(`A rule definition property with code "${code}" already exists`);
			return;
		}

		this.dialogRef.close({
			...this.form.value,
			id: code
		});
	}
}
