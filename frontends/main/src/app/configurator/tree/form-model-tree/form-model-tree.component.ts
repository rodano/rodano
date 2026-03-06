import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {TreeNode} from '../tree-node';
import {BaseTreeComponent} from '../base-tree.component';

@Component({
	selector: 'app-form-model-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: './form-model-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class FormModelTreeComponent extends BaseTreeComponent {
	@Input() formModels: any[] = [];
	@Input() layouts: any[] = [];
	@Input() selectedFormModelId: string | null = null;
	@Input() selectedLayoutId: string | null = null;

	constructor(languageService: LanguageService) {
		super(languageService);
	}

	getCategoryIcon(): string {return 'description';}
	getCategoryLabel(): string {return 'Form Models';}
	getCategoryTheme(): string {return 'theme-form-model';}

	protected buildTree(): void {
		if(!this.formModels.length) {
			return;
		}

		this.treeNodes = this.formModels.map(fm => {
			const node: TreeNode = {
				id: `form-model-${fm.formModelId}`,
				label: this.languageService.getDefaultTranslation(fm.shortname) || fm.id,
				icon: 'forms_add_on',
				type: 'form-model',
				selected: this.selectedFormModelId === fm.formModelId,
				entityId: fm.formModelId,
				themeClass: 'theme-form-model'
			};

			if(this.selectedFormModelId === fm.formModelId && this.layouts.length > 0) {
				const layoutNodes: TreeNode[] = this.layouts.map(layout => ({
					id: `layout-${layout.formLayoutId}`,
					label: layout.id,
					icon: 'view_quilt',
					type: 'layout',
					selected: this.selectedLayoutId === layout.formLayoutId,
					entityId: layout.formLayoutId,
					themeClass: 'theme-form-layout'
				}));

				if(layoutNodes.length > 0) {
					node.children = layoutNodes;
					node.expanded = true;
				}
			}

			return node;
		});
	}
}
