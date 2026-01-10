import {Component, Input, Output, EventEmitter, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {MatProgressSpinner} from '@angular/material/progress-spinner';

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
	imports: [CommonModule, MatIconModule, MatProgressSpinner]
})
export class ConfiguratorTreeComponent implements OnInit {
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
			expanded: true,
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

	constructor() {/*empty */}

	ngOnInit(): void {
		this.loadConfiguration();
	}

	loadConfiguration(): void {
		this.loading = true;

		//TODO: Load configuration entities from backend
		this.scopeModels = [];
		this.formModels = [];
		this.workflows = [];

		this.loading = false;
	}

	onNodeClick(node: TreeNode): void {
		this.nodeSelected.emit(node.id);
	}

	toggleExpand(node: TreeNode, event: Event): void {
		event.stopPropagation();
		node.expanded = !node.expanded;
	}
}
