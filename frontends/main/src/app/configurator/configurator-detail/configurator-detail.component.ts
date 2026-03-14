import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {MatIcon} from '@angular/material/icon';
import {ProjectSettingsDetailComponent} from '../project-settings/project-settings-detail/project-settings-detail.component';
import {ScopeModelsListComponent} from '../scope-model/scope-model-list/scope-models-list.component';
import {DatasetModelListComponent} from '../dataset-model/dataset-model-list/dataset-model-list.component';
import {ValidatorsListComponent} from '../validator/validators-list/validators-list.component';
import {WorkflowListComponent} from '../workflow/workflow-list/workflow-list.component';
import {ProfileListComponent} from '../profile/profile-list/profile-list.component';
import {EmptyStateComponent} from '../shared/empty-state/empty-state.component';
import {FeatureListComponent} from '../feature/feature-list/feature-list.component';
import {PrivacyPolicyListComponent} from '../privacy-policy/privacy-policy-list/privacy-policy-list.component';
import {
	ResourceCategoryListComponent
} from '../resource-category/resource-category-list/resource-category-list.component';
import {ReportListComponent} from '../report/report-list/report-list.component';
import {ChartListComponent} from '../chart/chart-list/chart-list.component';
import {FormModelListComponent} from '../form-model/form-model-list/form-model-list.component';
import {TimelineGraphListComponent} from '../timeline-graph/timeline-graph-list/timeline-graph-list.component';

@Component({
	selector: 'app-configurator-detail',
	standalone: true,
	templateUrl: './configurator-detail.component.html',
	styleUrls: ['./configurator-detail.component.css'],
	imports: [
		CommonModule,
		MatIcon,
		ProjectSettingsDetailComponent,
		ScopeModelsListComponent,
		DatasetModelListComponent,
		ValidatorsListComponent,
		WorkflowListComponent,
		ProfileListComponent,
		FeatureListComponent,
		PrivacyPolicyListComponent,
		ResourceCategoryListComponent,
		ReportListComponent,
		ChartListComponent,
		FormModelListComponent,
		TimelineGraphListComponent,
		EmptyStateComponent
	]
})
export class ConfiguratorDetailComponent implements OnChanges {
	@ViewChild(ScopeModelsListComponent) scopeModelsListComponent?: ScopeModelsListComponent;
	@ViewChild(DatasetModelListComponent) datasetModelsListComponent?: DatasetModelListComponent;
	@ViewChild(ValidatorsListComponent) validatorsListComponent?: ValidatorsListComponent;
	@ViewChild(WorkflowListComponent) workflowListComponent?: WorkflowListComponent;
	@ViewChild(ProfileListComponent) profileListComponent?: ProfileListComponent;
	@ViewChild(FeatureListComponent) featureListComponent?: FeatureListComponent;
	@ViewChild(PrivacyPolicyListComponent) privacyPolicyListComponent?: PrivacyPolicyListComponent;
	@ViewChild(ResourceCategoryListComponent) resourceCategoryListComponent?: ResourceCategoryListComponent;
	@ViewChild(ReportListComponent) reportListComponent?: ReportListComponent;
	@ViewChild(ChartListComponent) chartListComponent?: ChartListComponent;
	@ViewChild(FormModelListComponent) formModelListComponent?: FormModelListComponent;
	@ViewChild(TimelineGraphListComponent) timelineGraphListComponent?: TimelineGraphListComponent;

	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Input() workingProject: ConfiguratorProject | null = null;
	@Output() fieldsUpdated = new EventEmitter<Partial<ConfiguratorProject>>();
	@Output() nodeSelected = new EventEmitter<string | null>();

	@Output() scopeModelsChanged = new EventEmitter<boolean>();
	@Output() scopeModelContextChanged = new EventEmitter<{
		scopeModels: any[];
		eventModels: any[];
		eventGroups: any[];
		selectedScopeModelId: string | null;
		selectedEventModelId: string | null;
		selectedEventGroupId: string | null;
	}>();

	@Output() datasetModelsChanged = new EventEmitter<boolean>();
	@Output() datasetModelContextChanged = new EventEmitter<{
		datasetModels: any[];
		fieldModels: any[];
		selectedDatasetModelId: string | null;
		selectedFieldModelId: string | null;
	}>();

	@Output() validatorsChanged = new EventEmitter<boolean>();
	@Output() validatorContextChanged = new EventEmitter<{
		validators: any[];
		selectedValidatorId: string | null;
	}>();

	@Output() workflowsChanged = new EventEmitter<boolean>();
	@Output() workflowContextChanged = new EventEmitter<{
		workflows: any[];
		workflowStates: any[];
		workflowActions: any[];
		selectedWorkflowId: string | null;
		selectedWorkflowStateId: string | null;
		selectedWorkflowActionId: string | null;
	}>();

	@Output() profilesChanged = new EventEmitter<boolean>();
	@Output() profileContextChanged = new EventEmitter<{
		profiles: any[];
		selectedProfileId: string | null;
	}>();

	@Output() featuresChanged = new EventEmitter<boolean>();
	@Output() featureContextChanged = new EventEmitter<{
		features: any[];
		selectedFeatureId: string | null;
	}>();

	@Output() privacyPoliciesChanged = new EventEmitter<boolean>();
	@Output() privacyPolicyContextChanged = new EventEmitter<{
		privacyPolicies: any[];
		selectedPrivacyPolicyId: string | null;
	}>();

	@Output() resourceCategoriesChanged = new EventEmitter<boolean>();
	@Output() resourceCategoryContextChanged = new EventEmitter<{
		resourceCategories: any[];
		selectedResourceCategoryId: string | null;
	}>();

	@Output() reportsChanged = new EventEmitter<boolean>();
	@Output() reportContextChanged = new EventEmitter<{
		reports: any[];
		selectedReportId: string | null;
	}>();

	@Output() chartsChanged = new EventEmitter<boolean>();
	@Output() chartContextChanged = new EventEmitter<{
		charts: any[];
		selectedChartId: string | null;
	}>();

	@Output() formModelsChanged = new EventEmitter<boolean>();
	@Output() formModelContextChanged = new EventEmitter<{
		formModels: any[];
		selectedFormModelId: string | null;
	}>();

	@Output() timelineGraphsChanged = new EventEmitter<boolean>();
	@Output() timelineGraphContextChanged = new EventEmitter<{
		timelineGraphs: any[];
		sections: any[];
		selectedTimelineGraphId: string | null;
		selectedGraphSectionId: string | null;
	}>();

	selectedNodeType: 'project-settings' | 'scope-models' | 'dataset-models' | 'validators' | 'workflows' | 'profiles' | 'features' | 'privacy-policies' | 'resource-categories' | 'reports' | 'charts' | 'form-models' | 'timeline-graphs' | 'overview' | null = null;

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['selectedNode']) {
			this.determineNodeType();
		}
	}

	onScopeModelSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onScopeModelsChanged(value: boolean): void {
		this.scopeModelsChanged.emit(value);
	}

	onDatasetModelSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onDatasetModelsChanged(value: boolean): void {
		this.datasetModelsChanged.emit(value);
	}

	onValidatorSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onValidatorsChanged(value: boolean): void {
		this.validatorsChanged.emit(value);
	}

	onWorkflowSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onWorkflowsChanged(value: boolean): void {
		this.workflowsChanged.emit(value);
	}

	onProfileSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onProfilesChanged(value: boolean): void {
		this.profilesChanged.emit(value);
	}

	onFeatureSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onFeaturesChanged(value: boolean): void {
		this.featuresChanged.emit(value);
	}

	onPrivacyPolicySelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onPrivacyPoliciesChanged(value: boolean): void {
		this.privacyPoliciesChanged.emit(value);
	}

	onResourceCategorySelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onResourceCategoriesChanged(value: boolean): void {
		this.resourceCategoriesChanged.emit(value);
	}

	onReportSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onReportsChanged(value: boolean): void {
		this.reportsChanged.emit(value);
	}

	onChartSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onChartsChanged(value: boolean): void {
		this.chartsChanged.emit(value);
	}

	onFormModelSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onFormModelsChanged(value: boolean): void {
		this.formModelsChanged.emit(value);
	}

	onTimelineGraphSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onTimelineGraphsChanged(value: boolean): void {
		this.timelineGraphsChanged.emit(value);
	}

	private determineNodeType(): void {
		if(!this.selectedNode) {
			this.selectedNodeType = 'overview';
			return;
		}

		if(this.selectedNode.startsWith('scope-model-') || this.selectedNode.startsWith('event-model-') || this.selectedNode.startsWith('event-group-')) {
			this.selectedNodeType = 'scope-models';
			return;
		}

		if(this.selectedNode.startsWith('dataset-model-') || this.selectedNode.startsWith('field-model-')) {
			this.selectedNodeType = 'dataset-models';
			return;
		}

		if(this.selectedNode.startsWith('workflow-') || this.selectedNode.startsWith('workflow-state-') || this.selectedNode.startsWith('workflow-action-')) {
			this.selectedNodeType = 'workflows';
			return;
		}

		if(this.selectedNode.startsWith('timeline-graph-') || this.selectedNode.startsWith('timeline-graph-section-')) {
			this.selectedNodeType = 'timeline-graphs';
			return;
		}

		if(this.selectedNode === 'project-settings') {
			this.selectedNodeType = 'project-settings';
			return;
		}

		if(this.selectedNode === 'scope-models') {
			this.selectedNodeType = 'scope-models';
			return;
		}

		if(this.selectedNode === 'dataset-models') {
			this.selectedNodeType = 'dataset-models';
			return;
		}

		if(this.selectedNode === 'validators') {
			this.selectedNodeType = 'validators';
			return;
		}

		if(this.selectedNode === 'workflows') {
			this.selectedNodeType = 'workflows';
			return;
		}

		if(this.selectedNode === 'profiles') {
			this.selectedNodeType = 'profiles';
			return;
		}

		if(this.selectedNode === 'features') {
			this.selectedNodeType = 'features';
			return;
		}

		if(this.selectedNode === 'privacy-policies') {
			this.selectedNodeType = 'privacy-policies';
			return;
		}

		if(this.selectedNode === 'resource-categories') {
			this.selectedNodeType = 'resource-categories';
			return;
		}

		if(this.selectedNode === 'reports') {
			this.selectedNodeType = 'reports';
			return;
		}

		if(this.selectedNode === 'charts') {
			this.selectedNodeType = 'charts';
			return;
		}

		if(this.selectedNode === 'form-models') {
			this.selectedNodeType = 'form-models';
			return;
		}

		if(this.selectedNode === 'timeline-graphs') {
			this.selectedNodeType = 'timeline-graphs';
			return;
		}

		this.selectedNodeType = 'overview';
	}

	onScopeModelContextChanged(context: {
		scopeModels: any[];
		eventModels: any[];
		eventGroups: any[];
		selectedScopeModelId: string | null;
		selectedEventModelId: string | null;
		selectedEventGroupId: string | null;
	}): void {
		this.scopeModelContextChanged.emit(context);
	}

	onDatasetModelContextChanged(context: {
		datasetModels: any[];
		fieldModels: any[];
		selectedDatasetModelId: string | null;
		selectedFieldModelId: string | null;
	}): void {
		this.datasetModelContextChanged.emit(context);
	}

	onValidatorContextChanged(context: {
		validators: any[];
		selectedValidatorId: string | null;
	}): void {
		this.validatorContextChanged.emit(context);
	}

	onWorkflowContextChanged(context: {
		workflows: any[];
		workflowStates: any[];
		workflowActions: any[];
		selectedWorkflowId: string | null;
		selectedWorkflowStateId: string | null;
		selectedWorkflowActionId: string | null;
	}): void {
		this.workflowContextChanged.emit(context);
	}

	onProfileContextChanged(context: {
		profiles: any[];
		selectedProfileId: string | null;
	}): void {
		this.profileContextChanged.emit(context);
	}

	onFeatureContextChanged(context: {
		features: any[];
		selectedFeatureId: string | null;
	}): void {
		this.featureContextChanged.emit(context);
	}

	onPrivacyPolicyContextChanged(context: {
		privacyPolicies: any[];
		selectedPrivacyPolicyId: string | null;
	}): void {
		this.privacyPolicyContextChanged.emit(context);
	}

	onResourceCategoryContextChanged(context: {
		resourceCategories: any[];
		selectedResourceCategoryId: string | null;
	}): void {
		this.resourceCategoryContextChanged.emit(context);
	}

	onReportContextChanged(context: {
		reports: any[];
		selectedReportId: string | null;
	}): void {
		this.reportContextChanged.emit(context);
	}

	onChartContextChanged(context: {
		charts: any[];
		selectedChartId: string | null;
	}): void {
		this.chartContextChanged.emit(context);
	}

	onFormModelContextChanged(context: {
		formModels: any[];
		selectedFormModelId: string | null;
	}): void {
		this.formModelContextChanged.emit(context);
	}

	onTimelineGraphContextChanged(context: {
		timelineGraphs: any[];
		sections: any[];
		selectedTimelineGraphId: string | null;
		selectedGraphSectionId: string | null;
	}): void {
		this.timelineGraphContextChanged.emit(context);
	}
}
