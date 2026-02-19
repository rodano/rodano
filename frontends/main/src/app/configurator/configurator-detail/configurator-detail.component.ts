import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {MatIcon} from '@angular/material/icon';
import {ProjectSettingsDetailComponent} from '../project-settings/project-settings-detail/project-settings-detail.component';
import {ScopeModelsListComponent} from '../scope-model/scope-model-list/scope-models-list.component';
import {DatasetModelListComponent} from '../dataset-model/dataset-model-list/dataset-model-list.component';
import {ValidatorsListComponent} from '../validator/validators-list/validators-list.component';
import {WorkflowListComponent} from '../workflow/workflow-list/workflow-list.component';

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
		WorkflowListComponent
	]
})
export class ConfiguratorDetailComponent implements OnChanges {
	@ViewChild(ScopeModelsListComponent) scopeModelsListComponent?: ScopeModelsListComponent;
	@ViewChild(DatasetModelListComponent) datasetModelsListComponent?: DatasetModelListComponent;
	@ViewChild(ValidatorsListComponent) validatorsListComponent?: ValidatorsListComponent;
	@ViewChild(WorkflowListComponent) workflowListComponent?: WorkflowListComponent;

	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Input() modifiedFields = new Set<string>();
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
		workflowStates: any[];
		workflowActions: any[];
		selectedWorkflowId: string | null;
		selectedWorkflowStateId: string | null;
		selectedWorkflowActionId: string | null;
	}>();

	selectedNodeType: 'project-settings' | 'scope-models' | 'dataset-models' | 'validators' | 'workflows' | 'overview' | null = null;

	scopeModelModificationCount = 0;
	datasetModelModificationCount = 0;
	validatorModificationCount = 0;
	workflowModificationCount = 0;

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
		workflowStates: any[];
		workflowActions: any[];
		selectedWorkflowId: string | null;
		selectedWorkflowStateId: string | null;
		selectedWorkflowActionId: string | null;
	}): void {
		this.workflowContextChanged.emit(context);
	}
}
