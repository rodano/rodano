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

	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Input() workingProject: ConfiguratorProject | null = null;
	@Output() fieldsUpdated = new EventEmitter<Partial<ConfiguratorProject>>();
	@Output() nodeSelected = new EventEmitter<string | null>();
	@Output() modificationCountChanged = new EventEmitter<number>();

	@Output() scopeModelsChanged = new EventEmitter<{modificationCount: number}>();
	@Output() scopeModelContextChanged = new EventEmitter<{
		scopeModels: any[];
		eventModels: any[];
		eventGroups: any[];
		selectedScopeModelId: string | null;
		selectedEventModelId: string | null;
		selectedEventGroupId: string | null;
	}>();

	@Output() datasetModelsChanged = new EventEmitter<{modificationCount: number}>();
	@Output() datasetModelContextChanged = new EventEmitter<{
		datasetModels: any[];
		fieldModels: any[];
		selectedDatasetModelId: string | null;
		selectedFieldModelId: string | null;
	}>();

	@Output() validatorsChanged = new EventEmitter<{modificationCount: number}>();
	@Output() validatorContextChanged = new EventEmitter<{
		validators: any[];
		selectedValidatorId: string | null;
	}>();

	@Output() workflowsChanged = new EventEmitter<{modificationCount: number}>();
	@Output() workflowContextChanged = new EventEmitter<{
		workflows: any[];
		workflowStates: any[];
		workflowActions: any[];
		selectedWorkflowId: string | null;
		selectedWorkflowStateId: string | null;
		selectedWorkflowActionId: string | null;
	}>();

	@Output() profilesChanged = new EventEmitter<{modificationCount: number}>();
	@Output() profileContextChanged = new EventEmitter<{
		profiles: any[];
		selectedProfileId: string | null;
	}>();

	@Output() featuresChanged = new EventEmitter<{modificationCount: number}>();
	@Output() featureContextChanged = new EventEmitter<{
		features: any[];
		selectedFeatureId: string | null;
	}>();

	@Output() privacyPoliciesChanged = new EventEmitter<{modificationCount: number}>();
	@Output() privacyPolicyContextChanged = new EventEmitter<{
		privacyPolicies: any[];
		selectedPrivacyPolicyId: string | null;
	}>();

	@Output() resourceCategoriesChanged = new EventEmitter<{modificationCount: number}>();
	@Output() resourceCategoryContextChanged = new EventEmitter<{
		resourceCategories: any[];
		selectedResourceCategoryId: string | null;
	}>();

	@Output() reportsChanged = new EventEmitter<{modificationCount: number}>();
	@Output() reportContextChanged = new EventEmitter<{
		reports: any[];
		selectedReportId: string | null;
	}>();

	selectedNodeType: 'project-settings' | 'scope-models' | 'dataset-models' | 'validators' | 'workflows' | 'profiles' | 'features' | 'privacy-policies' | 'resource-categories' | 'reports' | 'overview' | null = null;

	scopeModelModificationCount = 0;
	datasetModelModificationCount = 0;
	validatorModificationCount = 0;
	workflowModificationCount = 0;
	profileModificationCount = 0;
	featureModificationCount = 0;
	privacyPolicyModificationCount = 0;
	resourceCategoryModificationCount = 0;
	reportModificationCount = 0;

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['selectedNode']) {
			this.determineNodeType();
		}
	}

	onScopeModelSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onScopeModelsChanged(event: {modificationCount: number}): void {
		this.scopeModelModificationCount = event.modificationCount;
		this.scopeModelsChanged.emit(event);
		this.modificationCountChanged.emit(event.modificationCount);
	}

	onDatasetModelSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onDatasetModelsChanged(event: {modificationCount: number}): void {
		this.datasetModelModificationCount = event.modificationCount;
		this.datasetModelsChanged.emit(event);
		this.modificationCountChanged.emit(event.modificationCount);
	}

	onValidatorSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onValidatorsChanged(event: {modificationCount: number}): void {
		this.validatorModificationCount = event.modificationCount;
		this.validatorsChanged.emit(event);
		this.modificationCountChanged.emit(event.modificationCount);
	}

	onWorkflowSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onWorkflowsChanged(event: {modificationCount: number}): void {
		this.workflowModificationCount = event.modificationCount;
		this.workflowsChanged.emit(event);
		this.modificationCountChanged.emit(event.modificationCount);
	}

	onProfileSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onProfilesChanged(event: {modificationCount: number}): void {
		this.profileModificationCount = event.modificationCount;
		this.profilesChanged.emit(event);
		this.modificationCountChanged.emit(event.modificationCount);
	}

	onFeatureSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onFeaturesChanged(event: {modificationCount: number}): void {
		this.featureModificationCount = event.modificationCount;
		this.featuresChanged.emit(event);
		this.modificationCountChanged.emit(event.modificationCount);
	}

	onPrivacyPolicySelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onPrivacyPoliciesChanged(event: {modificationCount: number}): void {
		this.privacyPolicyModificationCount = event.modificationCount;
		this.privacyPoliciesChanged.emit(event);
		this.modificationCountChanged.emit(event.modificationCount);
	}

	onResourceCategorySelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onResourceCategoriesChanged(event: {modificationCount: number}): void {
		this.resourceCategoryModificationCount = event.modificationCount;
		this.resourceCategoriesChanged.emit(event);
		this.modificationCountChanged.emit(event.modificationCount);
	}

	onReportSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;
		this.nodeSelected.emit(nodeId);
	}

	onReportsChanged(event: {modificationCount: number}): void {
		this.reportModificationCount = event.modificationCount;
		this.reportsChanged.emit(event);
		this.modificationCountChanged.emit(event.modificationCount);
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
}
