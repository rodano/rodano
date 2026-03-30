import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material/icon';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatTooltipModule} from '@angular/material/tooltip';
import {LanguageService} from '../../services/language.service';
import {ScopeModelManagerService} from '../../services/manager/scope-model-manager.service';
import {EventModelManagerService} from '../../services/manager/event-model-manager.service';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';
import {FormModelManagerService} from '../../services/manager/form-model-manager.service';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';
import {FeatureManagerService} from '../../services/manager/feature-manager.service';
import {EventGroupManagerService} from '../../services/manager/event-group-manager.service';
import {RuleConditionExtended} from '../rule-condition-extended';
import {OPERATORS, OPERATORS_BY_TYPE} from '../rule-constants';
import {RULE_ENTITIES, RuleProperty} from '../rule-entities';
import {MatSelectModule} from '@angular/material/select';
import {FieldModelManagerService} from '../../services/manager/field-model-manager.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {CdkDrag, CdkDragDrop, CdkDragHandle, CdkDropList, moveItemInArray} from '@angular/cdk/drag-drop';
import {AltDragService} from '../alt-drag.service';
import {ValidatorManagerService} from '../../services/manager/validator-manager.service';
import {WorkflowActionManagerService} from '../../services/manager/workflow-action-manager.service';

@Component({
	selector: 'app-condition-item',
	standalone: true,
	templateUrl: './condition-item.component.html',
	styleUrls: ['./condition-item.component.css', '../../dialogs/dialog-shared.css'],
	imports: [CommonModule, FormsModule, MatIconModule, MatCheckboxModule, MatTooltipModule, ConditionItemComponent,
		MatSelectModule, CdkDragHandle, CdkDropList, CdkDrag]
})
export class ConditionItemComponent implements OnInit {
	@Input() condition!: RuleConditionExtended;
	@Input() domain = '';
	@Input() depth = 0;
	@Input() contextScopeModelId = '';
	@Input() contextFieldModelId = '';
	@Input() contextWorkflowId = '';
	@Input() conditionId = '';
	@Input() allConditionIds = new Set<string>();
	@Input() dropListId = '';
	@Input() siblingDropListIds: string[] = [];
	@Output() remove = new EventEmitter<void>();
	@Output() insertSibling = new EventEmitter<void>();
	@Output() changed = new EventEmitter<void>();
	@Output() conditionIdChange = new EventEmitter<string>();
	@Output() idChanged = new EventEmitter<{oldId: string; newId: string}>();

	readonly operators = OPERATORS;

	constructor(
		public languageService: LanguageService,
		private scopeModelManager: ScopeModelManagerService,
		private eventModelManager: EventModelManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private fieldModelManager: FieldModelManagerService,
		private formModelManager: FormModelManagerService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private workflowActionManager: WorkflowActionManagerService,
		private profileManager: ProfileManagerService,
		private featureManager: FeatureManagerService,
		private eventGroupManager: EventGroupManagerService,
		private validatorManager: ValidatorManagerService,
		private snackBar: MatSnackBar,
		public altDragService: AltDragService
	) {}

	ngOnInit(): void {
		if(!this.condition.id) {
			this.condition.id = this.conditionId;
		}
		if(!this.condition.criterion) {
			this.condition.criterion = {property: '', operator: 'EQUALS', values: ['']};
		}
		if(!this.condition.conditions) {
			this.condition.conditions = [];
		}
		if(!this.condition.mode) {
			this.condition.mode = 'OR';
		}
		if(!this.condition.breakType) {
			this.condition.breakType = 'NONE';
		}
	}

	get nestedDropListId(): string {
		return this.dropListId || `cond-nested-${this.displayId}`;
	}

	getChildDropListId(child: RuleConditionExtended): string {
		return `cond-nested-${child.id ?? 'noid'}`;
	}

	getChildSiblingDropListIds(child: RuleConditionExtended): string[] {
		return (this.condition.conditions ?? [])
			.filter(c => c !== child)
			.map(c => this.getChildDropListId(c));
	}

	get propertiesForDomain(): RuleProperty[] {
		return RULE_ENTITIES[this.domain]?.properties ?? [];
	}

	get propertyDef(): RuleProperty | undefined {
		return this.propertiesForDomain.find(p => p.id === this.condition.criterion?.property);
	}

	get isNavigationProperty(): boolean {
		const prop = this.propertyDef;
		if(!prop) {
			return false;
		}
		return !prop.type;
	}

	get isValueProperty(): boolean {
		const prop = this.propertyDef;
		if(!prop) {
			return false;
		}
		return !!prop.type;
	}

	get availableOperators() {
		const type = this.propertyDef?.type;
		const keys = type ? OPERATORS_BY_TYPE[type] : null;
		return keys ? this.operators.filter(o => keys.includes(o.value)) : this.operators;
	}

	get showOperator(): boolean {
		return this.isValueProperty;
	}

	get showValue(): boolean {
		return this.isValueProperty && this.operatorHasValue(this.condition.criterion?.operator);
	}

	operatorHasValue(op: string | undefined): boolean {
		return this.operators.find(o => o.value === op)?.hasValue ?? true;
	}

	get displayId(): string {
		return this.condition.id ?? this.conditionId;
	}

	onConditionIdChange(value: string, input: HTMLInputElement): void {
		if(this.allConditionIds.has(value) && value !== this.condition.id) {
			this.snackBar.open(`ID "${value}" is already in use`, 'Close', {duration: 3000});
			input.value = this.condition.id ?? this.conditionId;
			return;
		}
		const oldId = this.condition.id ?? '';
		this.condition.id = value;
		this.idChanged.emit({oldId, newId: value});
		this.changed.emit();
	}

	getContextFieldModelId(): string {
		const idCondition = this.condition.conditions?.find(
			c => c.criterion?.property === 'ID'
		);
		return idCondition?.criterion?.values?.[0] ?? '';
	}

	get possibleValuesForContext(): string[] {
		if(!this.contextFieldModelId) {
			return [];
		}
		const fm = this.fieldModelManager.getById(this.contextFieldModelId);
		return fm?.possibleValues?.map((pv: any) => pv.id ?? pv) ?? [];
	}

	getEntityOptions(entityName: string): {id: string; label: string}[] {
		switch(entityName) {
			case 'ScopeModel':
				return this.scopeModelManager.getAll().map(e => ({id: e.scopeModelId, label: this.languageService.getLabel(e)}));
			case 'EventModel':
				return this.eventModelManager.getAll().map(e => ({id: e.eventModelId, label: this.languageService.getLabel(e)}));
			case 'DatasetModel':
				return this.datasetModelManager.getAll().map(e => ({id: e.datasetModelId, label: this.languageService.getLabel(e)}));
			case 'FieldModel': {
				let fieldModels = this.fieldModelManager.getAll();
				if(this.contextScopeModelId) {
					const scopeModel = this.scopeModelManager.getById(this.contextScopeModelId);
					const linkedDatasetIds = new Set(scopeModel?.datasetModelIds ?? []);
					fieldModels = fieldModels.filter(f => linkedDatasetIds.has(f.datasetModelId));
				}
				return fieldModels.map(f => {
					const ds = this.datasetModelManager.getById(f.datasetModelId);
					const dsLabel = ds ? this.languageService.getLabel(ds) : f.datasetModelId;
					return {id: f.fieldModelId, label: `${dsLabel} - ${this.languageService.getLabel(f)}`};
				});
			}
			case 'FormModel':
				return this.formModelManager.getAll().map(e => ({id: e.formModelId, label: this.languageService.getLabel(e)}));
			case 'Workflow':
				return this.workflowManager.getAll().map(e => ({id: e.workflowId, label: this.languageService.getLabel(e)}));
			case 'WorkflowState':
				return this.workflowStateManager.getAll()
					.filter((e: any) => !this.contextWorkflowId || e.workflowId === this.contextWorkflowId)
					.map((e: any) => {
						const wf = this.workflowManager.getById(e.workflowId);
						const wfLabel = wf ? this.languageService.getLabel(wf) : e.workflowId;
						return {id: e.workflowStateId, label: `${wfLabel} - ${this.languageService.getLabel(e)}`};
					});
			case 'WorkflowAction':
				return this.workflowActionManager.getAll()
					.filter((e: any) => !this.contextWorkflowId || e.workflowId === this.contextWorkflowId)
					.map((e: any) => {
						const wf = this.workflowManager.getById(e.workflowId);
						const wfLabel = wf ? this.languageService.getLabel(wf) : e.workflowId;
						return {id: e.workflowActionId, label: `${wfLabel} - ${this.languageService.getLabel(e)}`};
					});
			case 'Profile':
				return this.profileManager.getAll().map(e => ({id: e.profileId, label: this.languageService.getLabel(e)}));
			case 'Feature':
				return this.featureManager.getAll().map(e => ({id: e.featureId, label: this.languageService.getLabel(e)}));
			case 'EventGroup':
				return this.eventGroupManager.getAll().map(e => ({id: e.eventGroupId, label: this.languageService.getLabel(e)}));
			case 'Validator':
				return this.validatorManager.getAll()
					.filter((e: any) => !this.contextWorkflowId || e.workflowId === this.contextWorkflowId)
					.map((e: any) => ({
						id: e.validatorId,
						label: this.languageService.getLabel(e)
					}));
			default:
				return [];
		}
	}

	setValueAt(index: number, value: string): void {
		if(!this.condition.criterion) {
			return;
		}
		const values = [...(this.condition.criterion.values ?? [])];
		values[index] = value;
		this.condition.criterion.values = values;
		this.changed.emit();
	}

	addValue(): void {
		if(!this.condition.criterion) {
			return;
		}
		this.condition.criterion.values = [...(this.condition.criterion.values ?? []), ''];
		this.changed.emit();
	}

	removeValue(index: number): void {
		if(!this.condition.criterion) {
			return;
		}
		const values = [...(this.condition.criterion.values ?? [])];
		values.splice(index, 1);
		this.condition.criterion.values = values.length > 0 ? values : [''];
		this.changed.emit();
	}

	get criterionValues(): string[] {
		return this.condition.criterion?.values ?? [''];
	}

	cycleBreakType(): void {
		const order = ['NONE', 'ALLOW', 'DENY'];
		const current = this.condition.breakType || 'NONE';
		this.condition.breakType = order[(order.indexOf(current) + 1) % order.length];
		this.changed.emit();
	}

	getBreakIcon(): string {
		switch(this.condition.breakType) {
			case 'ALLOW': return 'check_circle';
			case 'DENY': return 'cancel';
			default: return 'radio_button_unchecked';
		}
	}

	onPropertyChange(propertyId: string): void {
		const prop = RULE_ENTITIES[this.domain]?.properties.find(p => p.id === propertyId);
		const defaultOperator = prop?.type === 'BOOLEAN' ? 'EQUALS' : 'EQUALS';
		this.condition.criterion = {
			...(this.condition.criterion ?? {}),
			property: propertyId,
			operator: defaultOperator,
			values: ['']
		};
		this.changed.emit();
	}

	insertChildAfter(after: RuleConditionExtended): void {
		const index = (this.condition.conditions ?? []).indexOf(after);
		const newId = `${this.displayId}${index + 2}`;
		const newCondition: RuleConditionExtended = {
			id: newId,
			mode: 'OR',
			inverse: false,
			dependency: false,
			breakType: 'NONE',
			conditions: [],
			criterion: {property: '', operator: 'EQUALS', values: ['']}
		};
		const conditions = [...(this.condition.conditions ?? [])];
		conditions.splice(index + 1, 0, newCondition);
		this.condition.conditions = conditions;
		this.changed.emit();
	}

	addChild(): void {
		const newId = `${this.displayId}${(this.condition.conditions?.length ?? 0) + 1}`;
		const newCondition: RuleConditionExtended = {
			id: newId,
			mode: 'OR',
			inverse: false,
			dependency: false,
			breakType: 'NONE',
			conditions: [],
			criterion: {property: '', operator: 'EQUALS', values: ['']}
		};
		this.condition.conditions = [...(this.condition.conditions ?? []), newCondition];
		this.changed.emit();
	}

	removeChild(child: RuleConditionExtended): void {
		this.condition.conditions = (this.condition.conditions ?? []).filter(c => c !== child);
		this.changed.emit();
	}

	onDropChild(event: CdkDragDrop<RuleConditionExtended[] | undefined>): void {
		if(event.previousContainer === event.container) {
			const conditions = [...(this.condition.conditions ?? [])];
			moveItemInArray(conditions, event.previousIndex, event.currentIndex);
			this.condition.conditions = conditions;
		}
		else {
			const source = event.previousContainer.data;
			if(!source) {
				return;
			}
			const moved = source[event.previousIndex];
			source.splice(event.previousIndex, 1);
			const target = [...(this.condition.conditions ?? [])];
			target.splice(event.currentIndex, 0, moved);
			this.condition.conditions = target;
		}
		this.changed.emit();
	}
}
