import {Component, EventEmitter, Input, OnChanges, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {ScopeModelService} from '../../services/api/scope-model.service';
import {LanguageService} from '../../services/language.service';
import {TreeNode} from '../tree-node';

@Component({
	selector: 'app-scope-models-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: './scope-models-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ScopeModelsTreeComponent implements OnInit, OnChanges {
	@Input() projectId = '';
	@Input() expanded = false;
	@Input() eventModels: any[] = [];
	@Input() eventGroups: any[] = [];
	@Input() selectedScopeModelId: string | null = null;
	@Input() selectedEventModelId: string | null = null;
	@Input() selectedEventGroupId: string | null = null;
	@Output() categoryClicked = new EventEmitter<void>();

	scopeModels: any[] = [];
	treeNodes: TreeNode[] = [];

	constructor(
		private scopeModelService: ScopeModelService,
		private languageService: LanguageService
	) {}

	ngOnInit(): void {
		this.loadScopeModels();
	}

	ngOnChanges(): void {
		this.buildTree();
	}

	loadScopeModels(): void {
		this.scopeModelService.getScopeModels(this.projectId).subscribe({
			next: models => {
				this.scopeModels = models;
				this.buildTree();
			},
			error: error => console.error('Error loading scope models:', error)
		});
	}

	onCategoryClick(): void {
		this.categoryClicked.emit();
	}

	private buildTree(): void {
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
			entityId: scopeModel.scopeModelId
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
						entityId: eg.eventGroupId
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
						entityId: em.eventModelId
					});
				});

			node.children = [...childScopeNodes, ...eventNodes];
		}
		else {
			node.children = childScopeNodes.length > 0 ? childScopeNodes : undefined;
		}

		return node;
	}

	reload(): void {
		this.loadScopeModels();
	}
}
