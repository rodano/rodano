import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {TreeNode} from '../tree-node';
import {BaseTreeComponent} from '../base-tree.component';
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
	selector: 'app-dataset-models-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatTooltipModule],
	templateUrl: './dataset-models-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class DatasetModelsTreeComponent extends BaseTreeComponent {
	@Input() datasetModels: any[] = [];
	@Input() fieldModels: any[] = [];
	@Input() selectedDatasetModelId: string | null = null;
	@Input() selectedFieldModelId: string | null = null;

	constructor(languageService: LanguageService) {
		super(languageService);
	}

	getCategoryIcon(): string {return 'table_chart';}
	getCategoryLabel(): string {return 'Dataset Models';}
	getCategoryTheme(): string {return 'theme-dataset-model';}

	protected buildTree(): void {
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
