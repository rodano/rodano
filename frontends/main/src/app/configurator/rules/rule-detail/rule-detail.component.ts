import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Rule} from '@core/model/rule';
import {RuleAction} from '@core/model/rule-action';
import {RuleActionParameter} from '@core/model/rule-action-parameter';
import {RuleService} from '../../services/api/rule.service';
import {LanguageService} from '../../services/language.service';
import {ConditionItemComponent} from '../condition-item/condition-item.component';
import {ScopeModelManagerService} from '../../services/manager/scope-model-manager.service';
import {EventModelManagerService} from '../../services/manager/event-model-manager.service';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';
import {FormModelManagerService} from '../../services/manager/form-model-manager.service';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';
import {FeatureManagerService} from '../../services/manager/feature-manager.service';
import {EventGroupManagerService} from '../../services/manager/event-group-manager.service';
import {ACTION_TYPES, DOMAINS} from '../rule-constants';
import {RuleConditionExtended} from '../rule-condition-extended';
import {RULE_ENTITIES, RuleActionParameterDef, RuleEntityAction} from '../rule-entities';
import {STATIC_ACTIONS} from '../static-actions';
import {MatSelectModule} from '@angular/material/select';
import {MatDialog} from '@angular/material/dialog';
import {
	RuleBasicInfoDialogComponent
} from '../../dialogs/rule/rule-basic-info-dialog/rule-basic-info-dialog.component';
import {SettingItemComponent} from '../../shared/setting-item/setting-item.component';

@Component({
	selector: 'app-rule-detail',
	standalone: true,
	templateUrl: './rule-detail.component.html',
	styleUrls: ['./rule-detail.component.css', '../../shared/detail-shared.css', '../condition-item/condition-item.component.css'],
	imports: [CommonModule, ReactiveFormsModule, FormsModule, MatIconModule,
		MatButtonModule, MatCheckboxModule, MatTooltipModule, ConditionItemComponent, MatSelectModule, SettingItemComponent]
})
export class RuleDetailComponent implements OnInit, OnChanges {
	@Input() rule!: Rule;
	@Input() projectId = '';
	@Input() entityPath = '';
	@Input() availableDomains: string[] = [...DOMAINS];
	@Output() ruleSaved = new EventEmitter<Rule>();

	form!: FormGroup;
	saving = false;

	readonly domains = DOMAINS;
	readonly actionTypes = ACTION_TYPES;
	readonly conditionModes = ['AND', 'OR'];

	constructor(
		private fb: FormBuilder,
		private ruleService: RuleService,
		public languageService: LanguageService,
		private snackBar: MatSnackBar,
		public scopeModelManager: ScopeModelManagerService,
		private eventModelManager: EventModelManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private formModelManager: FormModelManagerService,
		public workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private profileManager: ProfileManagerService,
		private featureManager: FeatureManagerService,
		private eventGroupManager: EventGroupManagerService,
		private dialog: MatDialog
	) {}

	ngOnInit(): void {
		this.initForm();
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['rule'] && !changes['rule'].firstChange) {
			this.initForm();
		}
	}

	private initForm(): void {
		this.ensureDomains();
	}

	private ensureDomains(): void {
		const conditions: Record<string, any> = {};
		const existing = this.rule.constraint?.conditions ?? {};

		for(const domain of this.availableDomains) {
			conditions[domain] = existing[domain] ?? {mode: 'OR', conditions: []};
		}

		this.rule = {
			...this.rule,
			constraint: {
				...this.rule.constraint,
				conditions
			}
		};
	}

	onEditBasicInfo(): void {
		this.dialog.open(RuleBasicInfoDialogComponent, {
			width: '500px',
			data: {
				rule: this.rule,
				languages: this.languageService.projectLanguages
			}
		}).afterClosed().subscribe(result => {
			if(result) {
				this.rule = {...this.rule, ...result};
				this.ruleSaved.emit(this.rule);
			}
		});
	}

	getMessageForCurrentLanguage(): string {
		const lang = this.languageService.currentLanguage ?? 'en';
		return this.rule.message?.[lang] ?? '';
	}

	getTagsDisplay(): string {
		return this.rule.tags?.join(', ') || '';
	}

	getConditionsForDomain(domain: string): RuleConditionExtended[] {
		return (this.rule.constraint?.conditions?.[domain]?.conditions ?? []) as RuleConditionExtended[];
	}

	getDomainMode(domain: string): string {
		return this.rule.constraint?.conditions?.[domain]?.mode ?? 'OR';
	}

	setDomainMode(domain: string, mode: string): void {
		const list = this.rule.constraint?.conditions?.[domain];
		if(list) {
			(list as any).mode = mode;
		}
	}

	addCondition(domain: string): void {
		const newCondition: RuleConditionExtended = {
			mode: 'OR',
			inverse: false,
			dependency: false,
			breakType: 'NONE',
			conditions: [],
			criterion: {property: '', operator: 'EQUALS', values: ['']}
		};
		const list = this.rule.constraint!.conditions![domain];
		(list as any).conditions = [...((list as any).conditions ?? []), newCondition];
		this.rule = {...this.rule};
	}

	removeCondition(domain: string, condition: RuleConditionExtended): void {
		const list = this.rule.constraint!.conditions![domain];
		(list as any).conditions = ((list as any).conditions ?? []).filter((c: any) => c !== condition);
		this.rule = {...this.rule};
	}

	getActionType(action: RuleAction): string {
		if(action.staticActionId) {
			return 'STATIC_ACTION';
		}
		if(action.configurationWorkflowId) {
			return 'CONFIGURATION_ACTION';
		}
		return 'ENTITY_ACTION';
	}

	setActionType(action: RuleAction, type: string): void {
		action.staticActionId = undefined;
		action.configurationWorkflowId = undefined;
		action.configurationActionId = undefined;
		action.rulableEntity = undefined;
		action.actionId = undefined;
		action.parameters = [];
		this.rule = {...this.rule};
	}

	getEntityActionsForDomain(domain: string): RuleEntityAction[] {
		return RULE_ENTITIES[domain]?.actions ?? [];
	}

	getStaticActionList() {
		return Object.values(STATIC_ACTIONS);
	}

	getActionParamDefs(action: RuleAction): RuleActionParameterDef[] {
		const type = this.getActionType(action);
		if(type === 'STATIC_ACTION' && action.staticActionId) {
			return STATIC_ACTIONS[action.staticActionId]?.parameters ?? [];
		}
		if(type === 'ENTITY_ACTION' && action.rulableEntity && action.actionId) {
			return RULE_ENTITIES[action.rulableEntity]?.actions
				.find(a => a.id === action.actionId)?.parameters ?? [];
		}
		return [];
	}

	getParamValue(action: RuleAction, paramId: string): string {
		return action.parameters?.find((p: RuleActionParameter) => p.id === paramId)?.value ?? '';
	}

	setParamValue(action: RuleAction, paramId: string, value: string): void {
		if(!action.parameters) {
			action.parameters = [];
		}
		const existing = action.parameters.find((p: RuleActionParameter) => p.id === paramId);
		if(existing) {
			existing.value = value;
		}
		else {
			action.parameters = [...action.parameters, {id: paramId, value}];
		}
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

	insertConditionAfter(domain: string, after: RuleConditionExtended): void {
		const newCondition: RuleConditionExtended = {
			mode: 'OR',
			inverse: false,
			dependency: false,
			breakType: 'NONE',
			conditions: [],
			criterion: {property: '', operator: 'EQUALS', values: ['']}
		};
		const list = this.rule.constraint!.conditions![domain];
		const conditions = [...((list as any).conditions ?? [])];
		const index = conditions.indexOf(after);
		conditions.splice(index + 1, 0, newCondition);
		(list as any).conditions = conditions;
		this.rule = {...this.rule};
	}

	addAction(): void {
		const newAction: RuleAction = {
			id: '',
			optional: false,
			parameters: []
		};
		this.rule = {...this.rule, actions: [...(this.rule.actions ?? []), newAction]};
	}

	removeAction(action: RuleAction): void {
		this.rule = {...this.rule, actions: (this.rule.actions ?? []).filter(a => a !== action)};
	}

	onConditionChanged(): void {
		this.rule = {...this.rule};
	}

	onActionTypeChanged(action: RuleAction, type: string): void {
		this.setActionType(action, type);
	}

	onStaticActionChanged(action: RuleAction): void {
		action.parameters = [];
		this.rule = {...this.rule};
	}

	onRulableEntityChanged(action: RuleAction): void {
		action.actionId = undefined;
		action.parameters = [];
		this.rule = {...this.rule};
	}

	onEntityActionChanged(action: RuleAction): void {
		action.parameters = [];
		this.rule = {...this.rule};
	}

	getActionId(action: RuleAction): string {
		return action.actionId ?? '';
	}

	setActionId(action: RuleAction, value: string): void {
		action.actionId = value;
		action.parameters = [];
		this.rule = {...this.rule};
	}

	onSave(): void {
		const v = this.form.getRawValue();
		const updated: Rule = {
			...this.rule,
			description: v.description,
			tags: v.tags ? v.tags.split(',').map((t: string) => t.trim()).filter((t: string) => t) : []
		};
		this.saving = true;
		this.ruleService.updateRule(this.projectId, this.entityPath, this.rule.ruleId!, updated).subscribe({
			next: saved => {
				this.saving = false;
				this.snackBar.open('Rule saved', 'Close', {duration: 2000});
				this.ruleSaved.emit(saved);
			},
			error: () => {
				this.saving = false;
				this.snackBar.open('Failed to save rule', 'Close', {duration: 3000});
			}
		});
	}
}
