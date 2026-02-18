import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {ScopeModelsTreeComponent} from '../scope-models-tree/scope-models-tree.component';
import {DatasetModelsTreeComponent} from '../dataset-models-tree/dataset-models-tree.component';
import {ProjectSettingsTreeComponent} from '../project-settings-tree/project-settings-tree.component';
import {ValidatorTreeComponent} from '../validator-tree/validator-tree.component';
import {WorkflowTreeComponent} from '../workflow-tree/workflow-tree.component';

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
		WorkflowTreeComponent
	],
	templateUrl: './configurator-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ConfiguratorTreeComponent {
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
	@Input() selectedScopeModelId: string | null = null;
	@Input() selectedEventModelId: string | null = null;
	@Input() selectedEventGroupId: string | null = null;
	@Input() selectedDatasetModelId: string | null = null;
	@Input() selectedFieldModelId: string | null = null;
	@Input() selectedValidatorId: string | null = null;
	@Input() selectedWorkflowId: string | null = null;
	@Input() selectedWorkflowStateId: string | null = null;
	@Input() selectedWorkflowActionId: string | null = null;

	@Output() categoryClicked = new EventEmitter<string>();

	expandedCategory: 'scope-models' | 'dataset-models' | 'validators' | 'workflows' | null = null;

	onScopeModelsClicked(): void {
		this.expandedCategory = this.expandedCategory === 'scope-models' ? null : 'scope-models';
		this.categoryClicked.emit('scope-models');
	}

	onDatasetModelsClicked(): void {
		this.expandedCategory = this.expandedCategory === 'dataset-models' ? null : 'dataset-models';
		this.categoryClicked.emit('dataset-models');
	}

	onValidatorsClicked(): void {
		this.expandedCategory = this.expandedCategory === 'validators' ? null : 'validators';
		this.categoryClicked.emit('validators');
	}

	onWorkflowsClicked(): void {
		this.expandedCategory = this.expandedCategory === 'workflows' ? null : 'workflows';
		this.categoryClicked.emit('workflows');
	}

	onCategoryClick(categoryId: string): void {
		this.expandedCategory = null;
		this.categoryClicked.emit(categoryId);
	}
}
