import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {FieldModel} from '@core/model/field-model';
import {DatasetModel} from '@core/model/dataset-model';
import {PossibleValue} from '@core/model/possible-value';
import {FieldModelDialogService} from '../../../services/dialogs/field-model-dialog.service';
import {FieldModelManagerService} from '../../../services/manager/field-model-manager.service';
import {ValidatorManagerService} from '../../../services/manager/validator-manager.service';
import {WorkflowManagerService} from '../../../services/manager/workflow-manager.service';
import {LanguageService} from '../../../services/language.service';
import {ConfirmationDialogComponent} from '../../../../confirmation-dialog/confirmation-dialog.component';
import {DangerZoneComponent} from '../../../shared/danger-zone/danger-zone.component';
import {BaseDraftDetailComponent} from '../../../shared/base-draft-detail.component';
import {SettingItemComponent} from '../../../shared/setting-item/setting-item.component';
import {Rule} from '@core/model/rule';
import {RuleListComponent} from '../../../rules/rule-list/rule-list.component';

@Component({
	selector: 'app-field-model-detail',
	standalone: true,
	templateUrl: './field-model-detail.component.html',
	styleUrls: ['./field-model-detail.component.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent,
		RuleListComponent]
})
export class FieldModelDetailComponent extends BaseDraftDetailComponent<FieldModel> {
	@Input() fieldModelId = '';
	@Input() fieldModels: FieldModel[] = [];
	@Input() originalFieldModels: FieldModel[] = [];
	@Input() datasetModel: DatasetModel | null = null;
	@Input() initialTab: 'general' | 'rules' = 'general';

	@Output() fieldModelUpdated = this.entityUpdated;
	@Output() fieldModelDeleted = this.entityDeleted;
	@Output() switchToRuleEditor = new EventEmitter<Rule>();

	readonly ruleTypes = [{type: null, label: 'Rules'}];

	constructor(
		languageService: LanguageService,
		private fieldModelDialogService: FieldModelDialogService,
		private fieldModelManager: FieldModelManagerService,
		private validatorManager: ValidatorManagerService,
		private workflowManager: WorkflowManagerService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	protected entityIdInputName(): string {return 'fieldModelId';}
	protected entitiesInputName(): string {return 'fieldModels';}
	protected getEntityId(): string {return this.fieldModelId;}
	protected getEntities(): FieldModel[] {return this.fieldModels;}
	protected getOriginals(): FieldModel[] {return this.originalFieldModels;}
	protected findInArray(arr: FieldModel[], id: string): FieldModel | undefined {
		return arr.find(fm => fm.fieldModelId === id);
	}

	protected override syncDraft(): void {
		const entity = this.fieldModelManager.getById(this.fieldModelId);
		this.draftEntity = entity ? JSON.parse(JSON.stringify(entity)) : null;
		const original = this.fieldModelManager.getOriginals().find(fm => fm.fieldModelId === this.fieldModelId);
		this.originalEntity = original ? JSON.parse(JSON.stringify(original)) : null;
	}

	override isFieldModified(field: keyof FieldModel): boolean {
		return this.fieldModelManager.isFieldModified(this.fieldModelId, field as string);
	}

	get draftFieldModel(): FieldModel | null {return this.draftEntity;}

	onEditBasicInfo(): void {
		if(!this.draftEntity || !this.datasetModel) {
			return;
		}
		this.fieldModelDialogService.openBasicInfoDialog(
			this.draftEntity, this.projectId,
			this.datasetModel.datasetModelId, this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditValidation(): void {
		if(!this.draftEntity) {
			return;
		}
		this.fieldModelDialogService.openValidationDialog(
			this.draftEntity, this.projectId, this.project?.languages || []
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditCalculatedValue(): void {
		if(!this.draftEntity) {
			return;
		}
		this.fieldModelDialogService.openCalculatedValueDialog(
			this.draftEntity, this.projectId
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditExport(): void {
		if(!this.draftEntity) {
			return;
		}
		this.fieldModelDialogService.openExportDialog(this.draftEntity).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditHelp(): void {
		if(!this.draftEntity) {
			return;
		}
		this.fieldModelDialogService.openHelpDialog(
			this.draftEntity, this.project?.languages || []
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditPossibleValues(): void {
		if(!this.draftEntity) {
			return;
		}
		this.fieldModelDialogService.openPossibleValuesDialog(
			this.draftEntity, this.projectId, this.project?.languages || []
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditResources(): void {
		if(!this.draftEntity) {
			return;
		}
		this.fieldModelDialogService.openResourcesDialog(this.draftEntity).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onDelete(): void {
		if(!this.draftEntity) {
			return;
		}
		const name = this.languageService.getTranslatedValue(this.draftEntity.shortname) || this.draftEntity.id;
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Field Model',
				message: `Are you sure you want to delete "${name}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed && this.draftEntity) {
				this.entityDeleted.emit(this.draftEntity.fieldModelId);
			}
		});
	}

	getSortedPossibleValues(): PossibleValue[] {
		return [...(this.draftEntity?.possibleValues || [])].sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0));
	}

	getValidatorLabel(validatorId: string): string {
		return this.languageService.getLabelById(validatorId, id => this.validatorManager.getById(id));
	}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}

	getTypeLabel(type: string): string {
		const typeMap: Record<string, string> = {
			STRING: 'String', AUTO_COMPLETION: 'Autocompleted String', DATE: 'Date',
			DATE_SELECT: 'Date with Selection', NUMBER: 'Number', SELECT: 'Combobox',
			RADIO: 'Radio', CHECKBOX: 'Checkbox', CHECKBOX_GROUP: 'Checkbox Group',
			TEXTAREA: 'Text Area', FILE: 'File'
		};
		return typeMap[type] || type;
	}

	getDataTypeLabel(dataType: string): string {
		const dataTypeMap: Record<string, string> = {
			STRING: 'String', DATE: 'Date', NUMBER: 'Number', BOOLEAN: 'Boolean', BLOB: 'Blob'
		};
		return dataTypeMap[dataType] || dataType;
	}

	hasValidationSettings(): boolean {
		const type = this.draftEntity?.type;
		if(!type) {
			return false;
		}
		return !['AUTO_COMPLETION', 'SELECT', 'RADIO', 'CHECKBOX', 'CHECKBOX_GROUP'].includes(type);
	}

	showStringValidation(): boolean {return this.draftEntity?.type === 'STRING';}
	showTextAreaValidation(): boolean {return this.draftEntity?.type === 'TEXTAREA' || this.draftEntity?.type === 'FILE';}
	showDateValidation(): boolean {return this.draftEntity?.type === 'DATE';}
	showDateSelectValidation(): boolean {return this.draftEntity?.type === 'DATE_SELECT';}
	showNumberValidation(): boolean {return this.draftEntity?.type === 'NUMBER';}

	get dateConfigModified(): boolean {
		return this.isFieldModified('withYears')
		  || this.isFieldModified('withMonths')
		  || this.isFieldModified('withDays')
		  || this.isFieldModified('withHours')
		  || this.isFieldModified('withMinutes')
		  || this.isFieldModified('withSeconds');
	}

	get mandatoryFieldsModified(): boolean {
		return this.isFieldModified('yearsMandatory')
		  || this.isFieldModified('monthsMandatory')
		  || this.isFieldModified('daysMandatory')
		  || this.isFieldModified('hoursMandatory')
		  || this.isFieldModified('minutesMandatory')
		  || this.isFieldModified('secondsMandatory');
	}

	get showPossibleValues(): boolean {
		const type = this.draftEntity?.type;
		if(!type) {
			return false;
		}
		return ['SELECT', 'RADIO', 'CHECKBOX_GROUP', 'AUTO_COMPLETION'].includes(type);
	}
}
