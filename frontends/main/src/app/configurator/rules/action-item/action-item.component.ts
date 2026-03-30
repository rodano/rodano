import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatSelectModule} from '@angular/material/select';
import {RuleAction} from '@core/model/rule-action';
import {RuleActionParameter} from '@core/model/rule-action-parameter';
import {RuleDefinitionAction} from '@core/model/rule-definition-action';
import {ACTION_TYPES} from '../rule-constants';
import {RULE_ENTITIES, RuleActionParameterDef} from '../rule-entities';
import {STATIC_ACTIONS} from '../static-actions';
import {LanguageService} from '../../services/language.service';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {ScopeModelManagerService} from '../../services/manager/scope-model-manager.service';
import {EventModelManagerService} from '../../services/manager/event-model-manager.service';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';
import {FormModelManagerService} from '../../services/manager/form-model-manager.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';
import {FeatureManagerService} from '../../services/manager/feature-manager.service';
import {EventGroupManagerService} from '../../services/manager/event-group-manager.service';
import {RuleDefinitionActionManagerService} from '../../services/manager/rule-definition-action-manager.service';
import {CdkDragHandle} from '@angular/cdk/drag-drop';
import {WorkflowAction} from '@core/model/workflow-action';
import {WorkflowActionManagerService} from '../../services/manager/workflow-action-manager.service';
import {MatTabsModule} from '@angular/material/tabs';

export interface ConditionEntity {
	id: string;
	entityType: string;
}

@Component({
	selector: 'app-action-item',
	standalone: true,
	templateUrl: './action-item.component.html',
	styleUrls: [
		'./action-item.component.css',
		'../condition-item/condition-item.component.css',
		'../../dialogs/dialog-shared.css'
	],
	imports: [CommonModule, FormsModule, MatIconModule, MatTooltipModule, MatSelectModule, CdkDragHandle, MatTabsModule]
})
export class ActionItemComponent implements OnChanges {
	@Input() action!: RuleAction;
	@Input() availableDomains: string[] = [];
	@Input() conditionEntities: ConditionEntity[] = [];
	@Input() contextWorkflowId = '';
	@Input() conditionIdToWorkflowId: Record<string, string> = {};

	@Output() remove = new EventEmitter<void>();
	@Output() changed = new EventEmitter<void>();

	readonly actionTypes = ACTION_TYPES;

	private _selectedType = 'ENTITY_ACTION';
	selectedLabelLang = '';

	constructor(
		public languageService: LanguageService,
		public workflowManager: WorkflowManagerService,
		private scopeModelManager: ScopeModelManagerService,
		private eventModelManager: EventModelManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private formModelManager: FormModelManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private workflowActionManager: WorkflowActionManagerService,
		private profileManager: ProfileManagerService,
		private featureManager: FeatureManagerService,
		private eventGroupManager: EventGroupManagerService,
		private ruleDefinitionActionManager: RuleDefinitionActionManagerService
	) {}

	ngOnChanges(): void {
		if(this.action) {
			if(this.action.staticActionId) {
				this._selectedType = 'STATIC_ACTION';
			}
			else if(this.action.configurationWorkflowId) {
				this._selectedType = 'CONFIGURATION_ACTION';
			}
		}
		this.selectedLabelLang = this.languageService.currentLanguage ?? 'en';
	}

	get actionType(): string {
		return this._selectedType;
	}

	onTypeChange(type: string): void {
		this._selectedType = type;
		this.action.staticActionId = undefined;
		this.action.configurationWorkflowId = undefined;
		this.action.configurationActionId = undefined;
		this.action.rulableEntity = undefined;
		this.action.actionId = undefined;
		this.action.parameters = [];
		this.changed.emit();
	}

	get projectLanguages() {
		return this.languageService.projectLanguages ?? [];
	}

	getLabelForLang(lang: string): string {
		return this.action.label?.[lang] ?? '';
	}

	setLabelForLang(lang: string, value: string): void {
		this.action.label = {...(this.action.label ?? {}), [lang]: value};
		this.changed.emit();
	}

	onOptionalToggle(): void {
		if(!this.action.optional) {
			this.action.optional = true;
			this.action.id = '';
		}
		else {
			this.action.optional = false;
			this.action.label = {};
			this.action.id = this.action.staticActionId ?? this.action.actionId ?? this.action.configurationActionId ?? '';
		}
		this.changed.emit();
	}

	getStaticActionList() {
		return Object.values(STATIC_ACTIONS);
	}

	onStaticActionChanged(): void {
		this.action.parameters = [];
		this.changed.emit();
	}

	get selectedEntityType(): string {
		return this.action.rulableEntity ?? this.conditionEntities.find(c => c.id === this.action.conditionId)?.entityType ?? '';
	}

	getActionsForEntityType(entityType: string): {id: string; label: string}[] {
		const staticActions = (RULE_ENTITIES[entityType]?.actions ?? [])
			.map((a: any) => ({id: a.id, label: a.label}));
		const customActions = this.ruleDefinitionActionManager.getAll()
			.filter((a: RuleDefinitionAction) => a.entity === entityType)
			.map((a: RuleDefinitionAction) => ({
				id: a.ruleDefinitionActionId,
				label: `${a.label} (${a.id})`
			}));
		return [...staticActions, ...customActions];
	}

	onRulableEntityChanged(): void {
		const entity = this.conditionEntities.find(c => c.id === this.action.conditionId);
		this.action.rulableEntity = entity?.entityType ?? undefined;
		this.action.actionId = undefined;
		this.action.parameters = [];
		this.changed.emit();
	}

	get actionId(): string {
		return this.action.actionId ?? '';
	}

	setActionId(value: string): void {
		this.action.actionId = value;
		this.action.parameters = [];
		this.changed.emit();
	}

	get workflowActions(): WorkflowAction[] {
		if(!this.action.configurationWorkflowId) {
			return [];
		}
		return this.workflowActionManager.getAll()
			.filter((wfa: WorkflowAction) => wfa.workflowId === this.action.configurationWorkflowId);
	}

	onWorkflowChanged(): void {
		this.action.configurationActionId = undefined;
		this.action.parameters = [];
		this.changed.emit();
	}

	onWorkflowActionChanged(): void {
		this.action.parameters = [];
		this.changed.emit();
	}

	get paramDefs(): RuleActionParameterDef[] {
		if(this._selectedType === 'STATIC_ACTION' && this.action.staticActionId) {
			return STATIC_ACTIONS[this.action.staticActionId]?.parameters ?? [];
		}
		if(this._selectedType === 'ENTITY_ACTION' && this.action.rulableEntity && this.action.actionId) {
			const staticDef = (RULE_ENTITIES[this.selectedEntityType]?.actions ?? [])
				.find((a: any) => a.id === this.action.actionId);
			if(staticDef) {
				return staticDef.parameters ?? [];
			}
			const customAction = this.ruleDefinitionActionManager.getAll()
				.find((a: RuleDefinitionAction) =>
					a.entity === this.selectedEntityType
					&& a.ruleDefinitionActionId === this.action.actionId
				);
			if(customAction) {
				return this.toParamDefs(customAction);
			}
		}
		return [];
	}

	private toParamDefs(action: RuleDefinitionAction): RuleActionParameterDef[] {
		return (action.parameters ?? [])
			.sort((a: any, b: any) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
			.map((p: any) => ({
				id: p.paramCode ?? p.id,
				label: p.label ?? p.paramCode ?? p.id,
				type: 'STRING' as const,
				options: p.options ? String(p.options).split(',').map((s: string) => s.trim()) : undefined,
				configurationEntity: p.configurationEntity,
				dataEntity: p.dataEntity,
				optional: p.optional ?? true
			}));
	}

	getParamValue(paramId: string): string {
		return this.action.parameters?.find((p: RuleActionParameter) => p.id === paramId)?.value ?? '';
	}

	setParamValue(paramId: string, value: string): void {
		if(!this.action.parameters) {
			this.action.parameters = [];
		}
		const existing = this.action.parameters.find((p: RuleActionParameter) => p.id === paramId);
		if(existing) {
			existing.value = value;
		}
		else {
			this.action.parameters = [...this.action.parameters, {id: paramId, value}];
		}
		this.changed.emit();
	}

	getEntityOptions(entityName: string): {id: string; label: string}[] {
		switch(entityName) {
			case 'ScopeModel': return this.scopeModelManager.getAll().map((e: any) => ({id: e.scopeModelId, label: this.languageService.getLabel(e)}));
			case 'EventModel': return this.eventModelManager.getAll().map((e: any) => ({id: e.eventModelId, label: this.languageService.getLabel(e)}));
			case 'DatasetModel': return this.datasetModelManager.getAll().map((e: any) => ({id: e.datasetModelId, label: this.languageService.getLabel(e)}));
			case 'FormModel': return this.formModelManager.getAll().map((e: any) => ({id: e.formModelId, label: this.languageService.getLabel(e)}));
			case 'Workflow': return this.workflowManager.getAll().map((e: any) => ({id: e.workflowId, label: this.languageService.getLabel(e)}));
			case 'WorkflowState': {
				const workflowId = this.conditionIdToWorkflowId[this.action.conditionId ?? ''] || this.contextWorkflowId || '';
				console.log('conditionId:', this.action.conditionId);
				console.log('conditionIdToWorkflowId:', this.conditionIdToWorkflowId);
				console.log('resolved workflowId:', workflowId);
				console.log('all states:', this.workflowStateManager.getAll());
				return this.workflowStateManager.getAll()
					.filter((e: any) => !workflowId || e.workflowId === workflowId)
					.map((e: any) => {
						const wf = this.workflowManager.getById(e.workflowId);
						const wfLabel = wf ? this.languageService.getLabel(wf) : e.workflowId;
						return {id: e.workflowStateId, label: `${wfLabel} - ${this.languageService.getLabel(e)}`};
					});
			}
			case 'WorkflowAction': {
				const workflowId = this.conditionIdToWorkflowId[this.action.conditionId ?? ''] || this.contextWorkflowId || '';
				return this.workflowActionManager.getAll()
					.filter((e: any) => !workflowId || e.workflowId === workflowId)
					.map((e: any) => {
						const wf = this.workflowManager.getById(e.workflowId);
						const wfLabel = wf ? this.languageService.getLabel(wf) : e.workflowId;
						return {id: e.workflowActionId, label: `${wfLabel} - ${this.languageService.getLabel(e)}`};
					});
			}
			case 'Profile': return this.profileManager.getAll().map((e: any) => ({id: e.profileId, label: this.languageService.getLabel(e)}));
			case 'Feature': return this.featureManager.getAll().map((e: any) => ({id: e.featureId, label: this.languageService.getLabel(e)}));
			case 'EventGroup': return this.eventGroupManager.getAll().map((e: any) => ({id: e.eventGroupId, label: this.languageService.getLabel(e)}));
			default: return [];
		}
	}

	getConditionEntitiesByType(entityType: string): ConditionEntity[] {
		return this.conditionEntities.filter(ce => ce.entityType === entityType);
	}
}
