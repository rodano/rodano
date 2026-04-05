import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {forkJoin, of} from 'rxjs';
import {ListHeaderComponent} from '../shared/list-header/list-header.component';
import {RuleDetailComponent} from '../rules/rule-detail/rule-detail.component';
import {RuleListComponent} from '../rules/rule-list/rule-list.component';
import {Rule} from '@core/model/rule';
import {TRIGGER_TYPES} from '../rules/trigger-constants';
import {LanguageService} from '../services/language.service';
import {EventModelManagerService} from '../services/manager/event-model-manager.service';
import {EventGroupManagerService} from '../services/manager/event-group-manager.service';
import {WorkflowStateManagerService} from '../services/manager/workflow-state-manager.service';
import {WorkflowActionManagerService} from '../services/manager/workflow-action-manager.service';
import {FieldModelManagerService} from '../services/manager/field-model-manager.service';

type ViewMode = 'options-list' | 'triggers';

@Component({
	selector: 'app-project-options',
	standalone: true,
	templateUrl: './project-options.component.html',
	styleUrls: ['./project-options.component.css', '../shared/breadcrumb-shared.css'],
	imports: [CommonModule, MatIconModule, MatTooltipModule, ListHeaderComponent, RuleDetailComponent,
		RuleListComponent, RuleDetailComponent, ListHeaderComponent]
})
export class ProjectOptionsComponent implements OnInit, OnChanges {
	@ViewChild(RuleDetailComponent) ruleDetailComponent!: RuleDetailComponent;

	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string | null>();

	viewMode: ViewMode = 'options-list';

	selectedRule: Rule | null = null;
	ruleModified = false;
	private originalRule: Rule | null = null;

	readonly ruleTypes = TRIGGER_TYPES.map(t => ({type: t.id, label: t.label}));

	constructor(
		public languageService: LanguageService,
		private eventModelManager: EventModelManagerService,
		private eventGroupManager: EventGroupManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private workflowActionManager: WorkflowActionManagerService,
		private fieldModelManager: FieldModelManagerService
	) {}

	ngOnInit(): void {
		if(this.selectedNode === 'options-triggers') {
			this.switchToTriggers();
		}
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['selectedNode']) {
			if(this.selectedNode === 'options-triggers') {
				this.switchToTriggers();
			}
			else if(this.selectedNode === 'project-options') {
				this.viewMode = 'options-list';
			}
		}
	}

	get activeTriggerDomains(): string[] {
		if(!this.selectedRule?.ruleType) {
			return [];
		}
		return TRIGGER_TYPES.find(t => t.id === this.selectedRule!.ruleType)?.domains ?? [];
	}

	switchToTriggers(): void {
		forkJoin({
			eventModels: this.eventModelManager.isLoaded() ? of(null) : this.eventModelManager.load(this.projectId),
			eventGroups: this.eventGroupManager.isLoaded() ? of(null) : this.eventGroupManager.load(this.projectId),
			workflowStates: this.workflowStateManager.isLoaded() ? of(null) : this.workflowStateManager.load(this.projectId),
			workflowActions: this.workflowActionManager.isLoaded() ? of(null) : this.workflowActionManager.load(this.projectId),
			fieldModels: this.fieldModelManager.isLoaded() ? of(null) : this.fieldModelManager.loadFull(this.projectId)
		}).subscribe(() => {
			this.viewMode = 'triggers';
			this.nodeSelected.emit('options-triggers');
		});
	}

	backToOptions(): void {
		this.viewMode = 'options-list';
		this.selectedRule = null;
		this.nodeSelected.emit('project-options');
	}

	switchToRuleEditor(rule: Rule): void {
		this.selectedRule = rule;
		this.originalRule = JSON.parse(JSON.stringify(rule));
		this.ruleModified = false;
	}

	onRuleChanged(): void {
		this.ruleModified = true;
	}

	onSaveRule(): void {
		this.ruleDetailComponent?.onSave();
	}

	onRevertRule(): void {
		if(this.originalRule) {
			this.selectedRule = JSON.parse(JSON.stringify(this.originalRule));
			this.ruleModified = false;
		}
	}

	onRuleSaved(rule: Rule): void {
		this.selectedRule = rule;
		this.originalRule = JSON.parse(JSON.stringify(rule));
		this.ruleModified = false;
	}

	backToTriggers(): void {
		this.selectedRule = null;
		this.ruleModified = false;
		this.viewMode = 'triggers';
	}
}
