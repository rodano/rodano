import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {ScopeModelsTreeComponent} from '../scope-models-tree/scope-models-tree.component';
import {DatasetModelsTreeComponent} from '../dataset-models-tree/dataset-models-tree.component';
import {ProjectSettingsTreeComponent} from '../project-settings-tree/project-settings-tree.component';
import {ValidatorTreeComponent} from '../validator-tree/validator-tree.component';
import {WorkflowTreeComponent} from '../workflow-tree/workflow-tree.component';
import {ProfileTreeComponent} from '../profile-tree/profile-tree.component';
import {FeatureTreeComponent} from '../feature-tree/feature-tree.component';
import {Observable, of} from 'rxjs';
import {PrivacyPolicyTreeComponent} from '../privacy-policy-tree/privacy-policy-tree.component';
import {ResourceCategoryTreeComponent} from '../resource-category-tree/resource-category-tree.component';
import {ReportTreeComponent} from '../report-tree/report-tree.component';
import {ChartTreeComponent} from '../chart-tree/chart-tree.component';
import {FormModelTreeComponent} from '../form-model-tree/form-model-tree.component';
import {TimelineGraphTreeComponent} from '../timeline-graph-tree/timeline-graph-tree.component';
import {WorkflowWidgetTreeComponent} from '../workflow-widget-tree/workflow-widget-tree.component';
import {WorkflowSummaryTreeComponent} from '../workflow-summary-tree/workflow-summary-tree.component';
import {
	RuleDefinitionPropertyTreeComponent
} from '../rule-definition-property-tree/rule-definition-property-tree.component';
import {
	RuleDefinitionActionTreeComponent
} from '../rule-definition-action-tree/rule-definition-action-tree.component';
import {CronTreeComponent} from '../cron-tree/cron-tree.component';
import {MenuTreeComponent} from '../menu-tree/menu-tree.component';
import {MatTooltip} from '@angular/material/tooltip';

@Component({
	selector: 'app-configurator-tree',
	standalone: true,
	imports: [
		CommonModule,
		MatIconModule,
		ProjectSettingsTreeComponent,
		ScopeModelsTreeComponent,
		DatasetModelsTreeComponent,
		ValidatorTreeComponent,
		WorkflowTreeComponent,
		ProfileTreeComponent,
		FeatureTreeComponent,
		PrivacyPolicyTreeComponent,
		ResourceCategoryTreeComponent,
		ReportTreeComponent,
		ChartTreeComponent,
		FormModelTreeComponent,
		TimelineGraphTreeComponent,
		WorkflowWidgetTreeComponent,
		WorkflowSummaryTreeComponent,
		RuleDefinitionPropertyTreeComponent,
		RuleDefinitionActionTreeComponent,
		CronTreeComponent,
		MenuTreeComponent,
		MatTooltip
	],
	templateUrl: './configurator-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ConfiguratorTreeComponent implements OnInit {
	@Input() projectId = '';
	@Input() selectedNode: string | null = null;
	@Input() scopeModels: any[] = [];
	@Input() eventModels: any[] = [];
	@Input() eventGroups: any[] = [];
	@Input() datasetModels: any[] = [];
	@Input() fieldModels: any[] = [];
	@Input() validators: any[] = [];
	@Input() workflows: any[] = [];
	@Input() workflowStates: any[] = [];
	@Input() workflowActions: any[] = [];
	@Input() profiles: any[] = [];
	@Input() features: any[] = [];
	@Input() privacyPolicies: any[] = [];
	@Input() resourceCategories: any[] = [];
	@Input() reports: any[] = [];
	@Input() charts: any[] = [];
	@Input() formModels: any[] = [];
	@Input() layouts: any[] = [];
	@Input() timelineGraphs: any[] = [];
	@Input() sections: any[] = [];
	@Input() workflowWidgets: any[] = [];
	@Input() workflowSummaries: any[] = [];
	@Input() ruleDefinitionProperties: any[] = [];
	@Input() ruleDefinitionActions: any[] = [];
	@Input() crons: any[] = [];
	@Input() menus: any[] = [];
	@Input() selectedScopeModelId: string | null = null;
	@Input() selectedEventModelId: string | null = null;
	@Input() selectedEventGroupId: string | null = null;
	@Input() selectedDatasetModelId: string | null = null;
	@Input() selectedFieldModelId: string | null = null;
	@Input() selectedValidatorId: string | null = null;
	@Input() selectedWorkflowId: string | null = null;
	@Input() selectedWorkflowStateId: string | null = null;
	@Input() selectedWorkflowActionId: string | null = null;
	@Input() selectedProfileId: string | null = null;
	@Input() selectedFeatureId: string | null = null;
	@Input() selectedPrivacyPolicyId: string | null = null;
	@Input() selectedResourceCategoryId: string | null = null;
	@Input() selectedReportId: string | null = null;
	@Input() selectedChartId: string | null = null;
	@Input() selectedFormModelId: string | null = null;
	@Input() selectedLayoutId: string | null = null;
	@Input() selectedTimelineGraphId: string | null = null;
	@Input() selectedGraphSectionId: string | null = null;
	@Input() selectedWorkflowWidgetId: string | null = null;
	@Input() selectedWorkflowSummaryId: string | null = null;
	@Input() selectedRuleDefinitionPropertyId: string | null = null;
	@Input() selectedRuleDefinitionActionId: string | null = null;
	@Input() selectedCronId: string | null = null;
	@Input() selectedMenuId: string | null = null;
	@Input() canNavigate?: () => Observable<boolean>;

	@Output() categoryClicked = new EventEmitter<string>();
	@Output() sidebarCollapsed = new EventEmitter<boolean>();

	collapsed = false;

	expandedCategory: 'scope-models' | 'dataset-models' | 'validators' | 'workflows' | 'profiles' | 'features' | 'privacy-policies' | 'resource-categories' | 'reports' | 'charts' | 'form-models' | 'timeline-graphs' | 'workflow-widgets' | 'workflow-summaries' | 'rule-definition-properties' | 'rule-definition-actions' | 'crons' | 'menus' | null = null;

	ngOnInit(): void {
		this.collapsed = localStorage.getItem('configurator-tree-collapsed') === 'true';
	}

	toggleCollapsed(): void {
		this.collapsed = !this.collapsed;
		localStorage.setItem('configurator-tree-collapsed', String(this.collapsed));
		this.sidebarCollapsed.emit(this.collapsed);
	}

	private runGuarded(action: () => void): void {
		const guard$ = this.canNavigate ? this.canNavigate() : of(true);
		guard$.subscribe(canNavigate => {
			if(canNavigate) {
				action();
			}
		});
	}

	onScopeModelsClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'scope-models' ? null : 'scope-models';
			this.categoryClicked.emit('scope-models');
		});
	}

	onDatasetModelsClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'dataset-models' ? null : 'dataset-models';
			this.categoryClicked.emit('dataset-models');
		});
	}

	onValidatorsClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'validators' ? null : 'validators';
			this.categoryClicked.emit('validators');
		});
	}

	onWorkflowsClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'workflows' ? null : 'workflows';
			this.categoryClicked.emit('workflows');
		});
	}

	onProfilesClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'profiles' ? null : 'profiles';
			this.categoryClicked.emit('profiles');
		});
	}

	onFeaturesClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'features' ? null : 'features';
			this.categoryClicked.emit('features');
		});
	}

	onPrivacyPolicyClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'privacy-policies' ? null : 'privacy-policies';
			this.categoryClicked.emit('privacy-policies');
		});
	}

	onResourceCategoryClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'resource-categories' ? null : 'resource-categories';
			this.categoryClicked.emit('resource-categories');
		});
	}

	onReportClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'reports' ? null : 'reports';
			this.categoryClicked.emit('reports');
		});
	}

	onChartClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'charts' ? null : 'charts';
			this.categoryClicked.emit('charts');
		});
	}

	onFormModelClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'form-models' ? null : 'form-models';
			this.categoryClicked.emit('form-models');
		});
	}

	onTimelineGraphClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'timeline-graphs' ? null : 'timeline-graphs';
			this.categoryClicked.emit('timeline-graphs');
		});
	}

	onWorkflowWidgetClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'workflow-widgets' ? null : 'workflow-widgets';
			this.categoryClicked.emit('workflow-widgets');
		});
	}

	onWorkflowSummaryClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'workflow-summaries' ? null : 'workflow-summaries';
			this.categoryClicked.emit('workflow-summaries');
		});
	}

	onRuleDefinitionPropertyClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'rule-definition-properties' ? null : 'rule-definition-properties';
			this.categoryClicked.emit('rule-definition-properties');
		});
	}

	onRuleDefinitionActionClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'rule-definition-actions' ? null : 'rule-definition-actions';
			this.categoryClicked.emit('rule-definition-actions');
		});
	}

	onCronClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'crons' ? null : 'crons';
			this.categoryClicked.emit('crons');
		});
	}

	onMenuClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'menus' ? null : 'menus';
			this.categoryClicked.emit('menus');
		});
	}

	onCategoryClick(categoryId: string): void {
		this.runGuarded(() => {
			this.expandedCategory = null;
			this.categoryClicked.emit(categoryId);
		});
	}
}
