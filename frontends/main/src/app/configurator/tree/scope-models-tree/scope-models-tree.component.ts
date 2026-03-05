import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {TreeNode} from '../tree-node';
import {BaseTreeComponent} from '../base-tree.component';

@Component({
	selector: 'app-scope-models-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: './scope-models-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ScopeModelsTreeComponent extends BaseTreeComponent {
	@Input() scopeModels: any[] = [];
	@Input() eventModels: any[] = [];
	@Input() eventGroups: any[] = [];
	@Input() selectedScopeModelId: string | null = null;
	@Input() selectedEventModelId: string | null = null;
	@Input() selectedEventGroupId: string | null = null;

	constructor(languageService: LanguageService) {
		super(languageService);
	}

	getCategoryIcon(): string {return 'account_tree';}
	getCategoryLabel(): string {return 'Scope Models';}
	getCategoryTheme(): string {return 'theme-scope-model';}

	protected buildTree(): void {
		if(!this.scopeModels.length) {
			return;
		}

		const roots = this.scopeModels.filter(sm => !sm.parentIds || sm.parentIds.length === 0);
		this.treeNodes = roots.map(root => this.buildNode(root, this.scopeModels));
	}

	private buildNode(scopeModel: any, allModels: any[]): TreeNode {
		const children = allModels.filter(sm =>
			sm.parentIds && sm.parentIds.includes(scopeModel.scopeModelId)
		);

		const hasChildren = children.length > 0;
		const hasParents = scopeModel.parentIds && scopeModel.parentIds.length > 0;
		const icon = hasChildren ? 'folder_open' : (hasParents ? 'fiber_manual_record' : 'circle');

		const node: TreeNode = {
			id: `scope-model-${scopeModel.scopeModelId}`,
			label: this.languageService.getDefaultTranslation(scopeModel.shortname) || scopeModel.id,
			icon,
			expanded: true,
			selected: this.selectedScopeModelId === scopeModel.scopeModelId,
			type: 'scope-model',
			entityId: scopeModel.scopeModelId,
			themeClass: 'theme-scope-model'
		};

		const childScopeNodes = children.map(child => this.buildNode(child, allModels));

		if(this.selectedScopeModelId === scopeModel.scopeModelId) {
			const eventNodes: TreeNode[] = [];

			this.eventGroups
				.filter(eg => eg.scopeModelId === scopeModel.scopeModelId)
				.forEach(eg => {
					eventNodes.push({
						id: `event-group-${eg.eventGroupId}`,
						label: this.languageService.getDefaultTranslation(eg.shortname) || eg.id,
						icon: 'group',
						type: 'event-group',
						selected: this.selectedEventGroupId === eg.eventGroupId,
						entityId: eg.eventGroupId,
						themeClass: 'theme-event-group'
					});
				});

			this.eventModels
				.filter(em => em.scopeModelId === scopeModel.scopeModelId)
				.forEach(em => {
					eventNodes.push({
						id: `event-model-${em.eventModelId}`,
						label: this.languageService.getDefaultTranslation(em.shortname) || em.id,
						icon: 'event',
						type: 'event-model',
						selected: this.selectedEventModelId === em.eventModelId,
						entityId: em.eventModelId,
						themeClass: 'theme-event-model'
					});
				});

			node.children = [...childScopeNodes, ...eventNodes];
		}
		else {
			node.children = childScopeNodes.length > 0 ? childScopeNodes : undefined;
		}

		return node;
	}
}
