import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {TreeNode} from '../tree-node';

@Component({
	selector: 'app-dataset-models-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: './dataset-models-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class DatasetModelsTreeComponent implements OnChanges {
	@Input() projectId = '';
	@Input() datasetModels: any[] = [];
	@Input() expanded = false;
	@Input() fieldModels: any[] = [];
	@Input() selectedDatasetModelId: string | null = null;
	@Input() selectedFieldModelId: string | null = null;
	@Output() categoryClicked = new EventEmitter<void>();

	treeNodes: TreeNode[] = [];

	constructor(private languageService: LanguageService) {}

	ngOnChanges(): void {
		this.buildTree();
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
				entityId: dm.datasetModelId,
				themeClass: 'theme-dataset-model'
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
						entityId: fm.fieldModelId,
						themeClass: 'theme-field-model'
					}));

				if(fieldNodes.length > 0) {
					node.children = fieldNodes;
					node.expanded = true;
				}
			}

			return node;
		});
	}
}
