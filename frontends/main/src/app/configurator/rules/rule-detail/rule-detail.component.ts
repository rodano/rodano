import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Rule} from '@core/model/rule';
import {RuleAction} from '@core/model/rule-action';
import {RuleService} from '../../services/api/rule.service';
import {LanguageService} from '../../services/language.service';
import {ConditionItemComponent} from '../condition-item/condition-item.component';
import {ActionItemComponent, ConditionEntity} from '../action-item/action-item.component';
import {ScopeModelManagerService} from '../../services/manager/scope-model-manager.service';
import {EventModelManagerService} from '../../services/manager/event-model-manager.service';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';
import {FormModelManagerService} from '../../services/manager/form-model-manager.service';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';
import {FeatureManagerService} from '../../services/manager/feature-manager.service';
import {EventGroupManagerService} from '../../services/manager/event-group-manager.service';
import {DOMAINS} from '../rule-constants';
import {RULE_ENTITIES} from '../rule-entities';
import {RuleConditionExtended} from '../rule-condition-extended';
import {MatSelectModule} from '@angular/material/select';
import {MatDialog} from '@angular/material/dialog';
import {RuleBasicInfoDialogComponent} from '../../dialogs/rule/rule-basic-info-dialog/rule-basic-info-dialog.component';
import {SettingItemComponent} from '../../shared/setting-item/setting-item.component';
import {CdkDrag, CdkDragDrop, CdkDragHandle, CdkDropList, moveItemInArray} from '@angular/cdk/drag-drop';
import {AltDragService} from '../alt-drag.service';

@Component({
	selector: 'app-rule-detail',
	standalone: true,
	templateUrl: './rule-detail.component.html',
	styleUrls: ['./rule-detail.component.css', '../../shared/detail-shared.css', '../condition-item/condition-item.component.css'],
	imports: [CommonModule, ReactiveFormsModule, FormsModule, MatIconModule,
		MatButtonModule, MatCheckboxModule, MatTooltipModule,
		ConditionItemComponent, ActionItemComponent,
		MatSelectModule, SettingItemComponent,
		CdkDropList, CdkDrag, CdkDragHandle]
})
export class RuleDetailComponent implements OnInit, OnChanges {
	@Input() rule!: Rule;
	@Input() projectId = '';
	@Input() entityPath = '';
	@Input() availableDomains: string[] = [...DOMAINS];
	@Input() contextWorkflowId = '';
	@Input() showBasicInfo = true;
	@Input() showActions = true;
	@Input() constraintOnly = false;
	@Output() ruleSaved = new EventEmitter<Rule>();
	@Output() ruleChanged = new EventEmitter<void>();

	form!: FormGroup;
	saving = false;
	availableTags: string[] = [];

	constructor(
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
		private dialog: MatDialog,
		public altDragService: AltDragService
	) {}

	ngOnInit(): void {
		this.initForm();
		this.ruleService.getAllTags(this.projectId).subscribe(tags => {
			this.availableTags = tags;
		});
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
		this.rule = {...this.rule, constraint: {...this.rule.constraint, conditions}};
	}

	onEditBasicInfo(): void {
		this.dialog.open(RuleBasicInfoDialogComponent, {
			width: '500px',
			data: {
				rule: this.rule,
				languages: this.languageService.projectLanguages,
				projectId: this.projectId,
				entityPath: this.entityPath,
				availableTags: this.availableTags
			}
		}).afterClosed().subscribe(saved => {
			if(saved) {
				this.rule = saved;
				this.ruleSaved.emit(this.rule);
			}
		});
	}

	getMessageForCurrentLanguage(): string {
		const lang = this.languageService.currentLanguage ?? 'en';
		return this.rule.message?.[lang] ?? '';
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

	getAllConditionIds(): Set<string> {
		const ids = new Set<string>(this.availableDomains);
		this.collectIds(ids, this.rule.constraint?.conditions ?? {});
		return ids;
	}

	private collectIds(ids: Set<string>, conditions: any): void {
		for(const domain of this.availableDomains) {
			for(const c of (conditions[domain]?.conditions ?? [])) {
				if(c.id) {
					ids.add(c.id);
				}
				this.collectConditionIds(ids, c);
			}
		}
	}

	private collectConditionIds(ids: Set<string>, condition: RuleConditionExtended): void {
		for(const child of condition.conditions ?? []) {
			if(child.id) {
				ids.add(child.id);
			}
			this.collectConditionIds(ids, child);
		}
	}

	getScopeModelIdFromConditions(): string {
		for(const c of this.getConditionsForDomain('SCOPE')) {
			if(c.criterion?.property === 'MODEL' && c.criterion?.values?.[0]) {
				return c.criterion.values[0];
			}
		}
		return '';
	}

	getContextFieldModelIdForDomain(domain: string): string {
		if(domain !== 'FIELD') {
			return '';
		}
		for(const c of this.getConditionsForDomain(domain)) {
			if(c.criterion?.property === 'ID' && c.criterion?.values?.[0]) {
				return c.criterion.values[0];
			}
		}
		return '';
	}

	addCondition(domain: string): void {
		if(!this.rule.constraint!.conditions![domain]) {
			(this.rule.constraint!.conditions as any)[domain] = {mode: 'OR', conditions: []};
		}
		const domainIndex = this.availableDomains.indexOf(domain) + 1;
		const existingCount = this.getConditionsForDomain(domain).length + 1;
		const newId = `${domainIndex}${existingCount}`;
		const newCondition: RuleConditionExtended = {
			id: newId, mode: 'OR', inverse: false, dependency: false,
			breakType: 'NONE', conditions: [],
			criterion: {property: '', operator: 'EQUALS', values: ['']}
		};
		const list = this.rule.constraint!.conditions![domain];
		(list as any).conditions = [...((list as any).conditions ?? []), newCondition];
		this.rule = {...this.rule};
		this.ruleChanged.emit();
	}

	removeCondition(domain: string, condition: RuleConditionExtended): void {
		const list = this.rule.constraint!.conditions![domain];
		(list as any).conditions = ((list as any).conditions ?? []).filter((c: any) => c !== condition);
		this.rule = {...this.rule};
		this.ruleChanged.emit();
	}

	insertConditionAfter(domain: string, after: RuleConditionExtended): void {
		const domainIndex = this.availableDomains.indexOf(domain) + 1;
		const conditions = [...((this.rule.constraint!.conditions![domain] as any).conditions ?? [])];
		const index = conditions.indexOf(after);
		const newId = `${domainIndex}${index + 2}`;
		const newCondition: RuleConditionExtended = {
			id: newId, mode: 'OR', inverse: false, dependency: false,
			breakType: 'NONE', conditions: [],
			criterion: {property: '', operator: 'EQUALS', values: ['']}
		};
		conditions.splice(index + 1, 0, newCondition);
		(this.rule.constraint!.conditions![domain] as any).conditions = conditions;
		this.rule = {...this.rule};
		this.ruleChanged.emit();
	}

	onConditionChanged(): void {
		this.rule = {...this.rule};
		this.ruleChanged.emit();
	}

	onDropCondition(domain: string, event: CdkDragDrop<RuleConditionExtended[]>): void {
		const list = this.rule.constraint!.conditions![domain] as any;
		const conditions = [...list.conditions];
		moveItemInArray(conditions, event.previousIndex, event.currentIndex);
		list.conditions = conditions;
		this.rule = {...this.rule};
		this.ruleChanged.emit();
	}

	getConditionEntities(): ConditionEntity[] {
		const result: ConditionEntity[] = [];
		for(const domain of this.availableDomains) {
			result.push({id: domain, entityType: domain});
			for(const c of this.getConditionsForDomain(domain)) {
				this.collectConditionEntitiesRecursive(result, c, domain);
			}
		}
		return result;
	}

	private collectConditionEntitiesRecursive(
		result: ConditionEntity[],
		condition: RuleConditionExtended,
		entityType: string
	): void {
		const propDef = RULE_ENTITIES[entityType]?.properties?.find(
			(p: any) => p.id === condition.criterion?.property
		);
		const childEntityType = (propDef && !propDef.type && propDef.target)
			? propDef.target
			: entityType;

		if(condition.id) {
			result.push({id: condition.id, entityType: childEntityType});
		}

		for(const child of condition.conditions ?? []) {
			this.collectConditionEntitiesRecursive(result, child, childEntityType);
		}
	}

	getTopLevelDropListId(condition: RuleConditionExtended): string {
		return `cond-nested-${condition.id ?? ''}`;
	}

	getTopLevelSiblingDropListIds(domain: string, condition: RuleConditionExtended): string[] {
		return this.getConditionsForDomain(domain)
			.filter(c => c !== condition)
			.map(c => this.getTopLevelDropListId(c));
	}

	addAction(): void {
		const newAction: RuleAction = {id: '', optional: false, parameters: []};
		this.rule = {...this.rule, actions: [...(this.rule.actions ?? []), newAction]};
		this.ruleChanged.emit();
	}

	removeAction(action: RuleAction): void {
		this.rule = {...this.rule, actions: (this.rule.actions ?? []).filter(a => a !== action)};
		this.ruleChanged.emit();
	}

	onDropAction(event: CdkDragDrop<RuleAction[] | undefined>): void {
		const actions = [...(event.container.data ?? this.rule.actions ?? [])];
		moveItemInArray(actions, event.previousIndex, event.currentIndex);
		this.rule = {...this.rule, actions};
		this.ruleChanged.emit();
	}

	onConditionIdChanged(event: {oldId: string; newId: string}): void {
		(this.rule.actions ?? []).forEach(action => {
			if(action.conditionId === event.oldId) {
				action.conditionId = event.newId;
			}
		});
		this.rule = {...this.rule};
		this.ruleChanged.emit();
	}

	getConditionIdToWorkflowId(): Record<string, string> {
		const map: Record<string, string> = {};
		const collect = (conditions: RuleConditionExtended[]) => {
			for(const c of conditions) {
				if(c.criterion?.property === 'ID' && c.criterion?.values?.[0]) {
					if(c.id) {
						map[c.id] = c.criterion.values[0];
					}
					for(const sibling of conditions) {
						if(sibling !== c && sibling.id) {
							map[sibling.id] = c.criterion.values[0];
						}
					}
				}
				collect(c.conditions ?? []);
			}
		};
		for(const domain of this.availableDomains) {
			collect(this.getConditionsForDomain(domain));
		}
		return map;
	}

	onSave(): void {
		this.saving = true;

		const referencedConditionIds = new Set(
			(this.rule.actions ?? [])
				.filter(a => a.conditionId)
				.map(a => a.conditionId!)
		);

		const filterConditions = (allConditions: Record<string, any>) =>
			Object.fromEntries(
				Object.entries(allConditions)
					.filter(([domain, list]) =>
						(list as any).conditions?.length > 0
						|| referencedConditionIds.has(domain)
					)
			);

		if(this.constraintOnly) {
			this.ruleService.saveConstraint(this.projectId, this.entityPath, {
				ruleConstraintId: this.rule.constraint?.ruleConstraintId ?? undefined,
				conditions: filterConditions(this.rule.constraint?.conditions ?? {})
			}).subscribe({
				next: saved => {
					this.saving = false;
					this.snackBar.open('Constraint saved', 'Close', {duration: 2000});
					this.ruleSaved.emit({...this.rule, constraint: saved as any});
				},
				error: () => {
					this.saving = false;
					this.snackBar.open('Failed to save constraint', 'Close', {duration: 3000});
				}
			});
			return;
		}

		const ruleToSave = {
			...this.rule,
			constraint: {
				...this.rule.constraint,
				conditions: filterConditions(this.rule.constraint?.conditions ?? {})
			}
		};
		this.ruleService.updateRule(this.projectId, this.entityPath, this.rule.ruleId!, ruleToSave).subscribe({
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
