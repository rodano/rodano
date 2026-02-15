import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {ScopeModelsTreeComponent} from '../scope-models-tree/scope-models-tree.component';
import {DatasetModelsTreeComponent} from '../dataset-models-tree/dataset-models-tree.component';
import {ProjectSettingsTreeComponent} from '../project-settings-tree/project-settings-tree.component';

@Component({
	selector: 'app-configurator-tree',
	standalone: true,
	imports: [
		CommonModule,
		MatIconModule,
		ProjectSettingsTreeComponent,
		ScopeModelsTreeComponent,
		DatasetModelsTreeComponent
	],
	templateUrl: './configurator-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ConfiguratorTreeComponent {
	@ViewChild(ScopeModelsTreeComponent) scopeModelsTree!: ScopeModelsTreeComponent;
	@ViewChild(DatasetModelsTreeComponent) datasetModelsTree!: DatasetModelsTreeComponent;

	@Input() projectId = '';
	@Input() selectedNode: string | null = null;
	@Input() eventModels: any[] = [];
	@Input() eventGroups: any[] = [];
	@Input() fieldModels: any[] = [];
	@Input() selectedScopeModelId: string | null = null;
	@Input() selectedEventModelId: string | null = null;
	@Input() selectedEventGroupId: string | null = null;
	@Input() selectedDatasetModelId: string | null = null;
	@Input() selectedFieldModelId: string | null = null;

	@Output() categoryClicked = new EventEmitter<string>();

	expandedCategory: 'scope-models' | 'dataset-models' | null = null;

	onScopeModelsClicked(): void {
		this.expandedCategory = this.expandedCategory === 'scope-models' ? null : 'scope-models';
		this.categoryClicked.emit('scope-models');
	}

	onDatasetModelsClicked(): void {
		this.expandedCategory = this.expandedCategory === 'dataset-models' ? null : 'dataset-models';
		this.categoryClicked.emit('dataset-models');
	}

	onCategoryClick(categoryId: string): void {
		this.expandedCategory = null;
		this.categoryClicked.emit(categoryId);
	}

	reloadScopeModels(): void {
		this.scopeModelsTree?.reload();
	}

	reloadDatasetModels(): void {
		this.datasetModelsTree?.reload();
	}
}
