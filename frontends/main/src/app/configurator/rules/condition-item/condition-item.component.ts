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
import {OPERATORS} from '../rule-constants';
import {RULE_ENTITIES, RuleProperty} from '../rule-entities';
import {MatSelectModule} from '@angular/material/select';

@Component({
	selector: 'app-condition-item',
	standalone: true,
	templateUrl: './condition-item.component.html',
	styleUrls: ['./condition-item.component.css', '../../dialogs/dialog-shared.css'],
	imports: [CommonModule, FormsModule, MatIconModule, MatCheckboxModule, MatTooltipModule, ConditionItemComponent,
		MatSelectModule]
})
export class ConditionItemComponent implements OnInit {
	@Input() condition!: RuleConditionExtended;
	@Input() domain = '';
	@Input() depth = 0;
	@Output() remove = new EventEmitter<void>();
	@Output() insertSibling = new EventEmitter<void>();
	@Output() changed = new EventEmitter<void>();

	readonly operators = OPERATORS;
	readonly conditionModes = ['AND', 'OR'];
	readonly breakTypes = ['NONE', 'ALLOW', 'DENY'];
	readonly BOOLEAN_OPERATORS = ['EQUALS', 'NOT_EQUALS', 'NULL', 'NOT_NULL'];

	constructor(
		public languageService: LanguageService,
		private scopeModelManager: ScopeModelManagerService,
		private eventModelManager: EventModelManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private formModelManager: FormModelManagerService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private profileManager: ProfileManagerService,
		private featureManager: FeatureManagerService,
		private eventGroupManager: EventGroupManagerService
	) {}

	ngOnInit(): void {
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

	get isBooleanProperty(): boolean {
		return this.propertyDef?.type === 'BOOLEAN';
	}

	get isValueProperty(): boolean {
		const prop = this.propertyDef;
		if(!prop) {
			return false;
		}
		return !!prop.type;
	}

	get availableOperators() {
		if(this.isBooleanProperty) {
			return this.operators.filter(o => this.BOOLEAN_OPERATORS.includes(o.value));
		}
		return this.operators;
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

	getEntityOptions(entityName: string): {id: string; label: string}[] {
		switch(entityName) {
			case 'ScopeModel':
				return this.scopeModelManager.getAll().map(e => ({id: e.scopeModelId, label: this.languageService.getLabel(e)}));
			case 'EventModel':
				return this.eventModelManager.getAll().map(e => ({id: e.eventModelId, label: this.languageService.getLabel(e)}));
			case 'DatasetModel':
				return this.datasetModelManager.getAll().map(e => ({id: e.datasetModelId, label: this.languageService.getLabel(e)}));
			case 'FormModel':
				return this.formModelManager.getAll().map(e => ({id: e.formModelId, label: this.languageService.getLabel(e)}));
			case 'Workflow':
				return this.workflowManager.getAll().map(e => ({id: e.workflowId, label: this.languageService.getLabel(e)}));
			case 'WorkflowState':
				return this.workflowStateManager.getAll().map(e => ({id: e.workflowStateId, label: this.languageService.getLabel(e)}));
			case 'Profile':
				return this.profileManager.getAll().map(e => ({id: e.profileId, label: this.languageService.getLabel(e)}));
			case 'Feature':
				return this.featureManager.getAll().map(e => ({id: e.featureId, label: this.languageService.getLabel(e)}));
			case 'EventGroup':
				return this.eventGroupManager.getAll().map(e => ({id: e.eventGroupId, label: this.languageService.getLabel(e)}));
			default:
				return [];
		}
	}

	getValueAt(index: number): string {
		return this.condition.criterion?.values?.[index] ?? '';
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
		const newCondition: RuleConditionExtended = {
			mode: 'OR',
			inverse: false,
			dependency: false,
			breakType: 'NONE',
			conditions: [],
			criterion: {property: '', operator: 'EQUALS', values: ['']}
		};
		const conditions = [...(this.condition.conditions ?? [])];
		const index = conditions.indexOf(after);
		conditions.splice(index + 1, 0, newCondition);
		this.condition.conditions = conditions;
		this.changed.emit();
	}

	addChild(): void {
		const newCondition: RuleConditionExtended = {
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
}
