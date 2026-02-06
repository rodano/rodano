import {Component, Input, Output, EventEmitter, OnInit, OnChanges, ChangeDetectorRef} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {ScopeModelService} from '../services/scope-model.service';

interface TreeNode {
	id: string;
	label: string;
	icon?: string;
	children?: TreeNode[];
	expanded?: boolean;
	fixed?: boolean;
}

@Component({
	selector: 'app-configurator-tree',
	standalone: true,
	templateUrl: './configurator-tree.component.html',
	styleUrls: ['./configurator-tree.component.css'],
	imports: [CommonModule, MatIconModule]
})
export class ConfiguratorTreeComponent implements OnInit, OnChanges {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string>();

	loading = true;
	scopeModels: any[] = [];
	formModels: any[] = [];
	workflows: any[] = [];

	treeData: TreeNode[] = [
		{
			id: 'project-settings',
			label: 'Project Settings',
			icon: 'settings',
			fixed: true
		},
		{
			id: 'scope-models',
			label: 'Scope Models',
			icon: 'account_tree',
			expanded: false,
			children: []
		},
		{
			id: 'dataset-models',
			label: 'Dataset Models',
			icon: 'dataset',
			children: []
		},
		{
			id: 'validators',
			label: 'Validators',
			icon: 'rule',
			children: []
		},
		{
			id: 'form-models',
			label: 'Form Models',
			icon: 'description',
			expanded: true,
			children: [
				{id: 'form-demographics', label: 'Demographics', icon: 'person'},
				{id: 'form-medical-history', label: 'Medical History', icon: 'medical_services'}
			]
		},
		{
			id: 'workflows',
			label: 'Workflows',
			icon: 'account_tree',
			children: []
		},
		{
			id: 'profiles',
			label: 'Profiles',
			icon: 'badge',
			children: []
		},
		{
			id: 'features',
			label: 'Features',
			icon: 'star',
			children: []
		},
		{
			id: 'languages',
			label: 'Languages',
			icon: 'language',
			children: []
		},
		{
			id: 'menus',
			label: 'Menus',
			icon: 'menu',
			children: []
		}
	];

	constructor(
		private scopeModelService: ScopeModelService,
		private changeDetectorRef: ChangeDetectorRef
	) {}

	ngOnInit(): void {
		this.loadConfiguration();
	}

	ngOnChanges(changes: any): void {
		if(changes['selectedNode'] && this.selectedNode) {
			this.expandParentsOfNode(this.selectedNode, this.treeData);
		}
	}

	loadConfiguration(): void {
		this.loading = true;
		this.loadScopeModels();

		//TODO: Load configuration entities from backend
		this.formModels = [];
		this.workflows = [];

		this.loading = false;
	}

	private loadScopeModels(): void {
		this.scopeModelService.getScopeModels(this.projectId).subscribe({
			next: scopeModels => {
				const scopeModelsIndex = this.treeData.findIndex(node => node.id === 'scope-models');

				if(scopeModelsIndex !== -1) {
					const currentNode = this.treeData[scopeModelsIndex];
					const newChildren = this.buildScopeModelTree(scopeModels);

					const newScopeModelsNode = {
						...currentNode,
						children: newChildren
					};

					this.treeData = [
						...this.treeData.slice(0, scopeModelsIndex),
						newScopeModelsNode,
						...this.treeData.slice(scopeModelsIndex + 1)
					];

					this.changeDetectorRef.detectChanges();
				}
				this.loading = false;
			},
			error: error => {
				console.error('Error loading scope models:', error);
				this.loading = false;
			}
		});
	}

	public reloadScopeModels(): void {
		this.loadScopeModels();
	}

	private buildScopeModelTree(scopeModels: any[]): TreeNode[] {
		const roots = scopeModels.filter(sm =>
			!sm.parentIds || sm.parentIds.length === 0
		);

		return roots.map(root => this.buildScopeModelNodeRecursive(root, scopeModels));
	}

	private buildScopeModelNodeRecursive(scopeModel: any, allScopeModels: any[]): TreeNode {
		const children = allScopeModels.filter(sm =>
			sm.parentIds && sm.parentIds.includes(scopeModel.scopeModelId)
		);

		let icon;
		const hasParents = scopeModel.parentIds && scopeModel.parentIds.length > 0;
		const hasChildren = children.length > 0;

		if(hasChildren) {
			icon = 'folder_open';
		}
		else if(hasParents && !hasChildren) {
			icon = 'fiber_manual_record';
		}
		else {
			icon = 'circle';
		}

		const node: TreeNode = {
			id: `scope-model-${scopeModel.scopeModelId}`,
			label: scopeModel.shortname['en'] || scopeModel.shortname['de'] || scopeModel.id,
			icon: icon,
			expanded: true
		};

		if(children.length > 0) {
			node.children = children.map(child =>
				this.buildScopeModelNodeRecursive(child, allScopeModels)
			);
		}

		return node;
	}

	private expandParentsOfNode(nodeId: string, tree: TreeNode[]): boolean {
		for(const node of tree) {
			if(node.id === nodeId) {
				return true;
			}
			if(node.children) {
				const found = this.expandParentsOfNode(nodeId, node.children);
				if(found) {
					node.expanded = true;
					return true;
				}
			}
		}
		return false;
	}

	onNodeClick(node: TreeNode, event?: Event): void {
		if(event) {
			event.stopPropagation();
		}

		const isTopLevel = this.treeData.includes(node);

		if(isTopLevel) {
			this.treeData.forEach(n => {
				if(n !== node) {
					n.expanded = false;
				}
			});

			if(node.children && node.children.length > 0) {
				node.expanded = true;
			}
		}

		this.nodeSelected.emit(node.id);
	}
}
