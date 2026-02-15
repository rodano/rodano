import {Component, EventEmitter, Input, OnChanges, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {DatasetModelService} from '../../services/api/dataset-model.service';
import {LanguageService} from '../../services/language.service';
import {TreeNode} from '../tree-node';

@Component({
	selector: 'app-dataset-models-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: './dataset-models-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class DatasetModelsTreeComponent implements OnInit, OnChanges {
	@Input() projectId = '';
	@Input() expanded = false;
	@Input() fieldModels: any[] = [];
	@Input() selectedDatasetModelId: string | null = null;
	@Input() selectedFieldModelId: string | null = null;
	@Output() categoryClicked = new EventEmitter<void>();

	datasetModels: any[] = [];
	treeNodes: TreeNode[] = [];

	constructor(
		private datasetModelService: DatasetModelService,
		private languageService: LanguageService
	) {}

	ngOnInit(): void {
		this.loadDatasetModels();
	}

	ngOnChanges(): void {
		this.buildTree();
	}

	loadDatasetModels(): void {
		this.datasetModelService.getDatasetModels(this.projectId).subscribe({
			next: models => {
				this.datasetModels = models;
				this.buildTree();
			},
			error: error => console.error('Error loading dataset models:', error)
		});
	}

	onCategoryClick(): void {
		this.categoryClicked.emit();
	}

	private buildTree(): void {
		if(!this.datasetModels.length) {
			return;
		}

		this.treeNodes = this.datasetModels.map(dm => {
			const node: TreeNode = {
				id: `dataset-model-${dm.datasetModelId}`,
				label: this.languageService.getDefaultTranslation(dm.shortname) || dm.id,
				icon: 'table_chart',
				type: 'dataset-model',
				selected: this.selectedDatasetModelId === dm.datasetModelId,
				entityId: dm.datasetModelId
			};

			if(this.selectedDatasetModelId === dm.datasetModelId && this.fieldModels.length > 0) {
				const fieldNodes: TreeNode[] = this.fieldModels
					.filter(fm => fm.datasetModelId === dm.datasetModelId)
					.map(fm => ({
						id: `field-model-${fm.fieldModelId}`,
						label: this.languageService.getDefaultTranslation(fm.shortname) || fm.id,
						icon: 'text_ad',
						type: 'field-model',
						selected: this.selectedFieldModelId === fm.fieldModelId,
						entityId: fm.fieldModelId
					}));

				if(fieldNodes.length > 0) {
					node.children = fieldNodes;
					node.expanded = true;
				}
			}

			return node;
		});
	}

	reload(): void {
		this.loadDatasetModels();
	}
}
