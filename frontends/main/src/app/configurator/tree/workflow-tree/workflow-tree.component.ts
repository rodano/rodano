import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {TreeNode} from '../tree-node';

@Component({
	selector: 'app-workflow-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: './workflow-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class WorkflowTreeComponent implements OnChanges {
	@Input() projectId = '';
	@Input() workflows: any[] = [];
	@Input() expanded = false;
	@Input() workflowStates: any[] = [];
	@Input() workflowActions: any[] = [];
	@Input() selectedWorkflowId: string | null = null;
	@Input() selectedWorkflowStateId: string | null = null;
	@Input() selectedWorkflowActionId: string | null = null;
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
		if(!this.workflows.length) {
			return;
		}

		this.treeNodes = this.workflows.map(wf => {
			const node: TreeNode = {
				id: `workflow-${wf.workflowId}`,
				label: this.languageService.getDefaultTranslation(wf.shortname) || wf.id,
				icon: 'work',
				type: 'workflow',
				selected: this.selectedWorkflowId === wf.workflowId,
				entityId: wf.workflowId,
				themeClass: 'theme-workflow'
			};

			if(this.selectedWorkflowId === wf.workflowId) {
				const stateNodes: TreeNode[] = this.workflowStates
					.filter(wfs => wfs.workflowId === wf.workflowId)
					.map(wfs => ({
						id: `workflow-state-${wfs.workflowStateId}`,
						label: this.languageService.getDefaultTranslation(wfs.shortname) || wfs.id,
						icon: 'text_ad',
						type: 'workflow-state',
						selected: this.selectedWorkflowStateId === wfs.workflowStateId,
						entityId: wfs.workflowStateId,
						themeClass: 'theme-workflow-state'
					}));

				const actionNodes: TreeNode[] = this.workflowActions
					.filter(wfa => wfa.workflowId === wf.workflowId)
					.map(wfa => ({
						id: `workflow-action-${wfa.workflowActionId}`,
						label: this.languageService.getDefaultTranslation(wfa.shortname) || wfa.id,
						icon: 'text_ad',
						type: 'workflow-action',
						selected: this.selectedWorkflowActionId === wfa.workflowActionId,
						entityId: wfa.workflowActionId,
						themeClass: 'theme-workflow-action'
					}));

				const children = [...stateNodes, ...actionNodes];
				if(children.length > 0) {
					node.children = children;
					node.expanded = true;
				}
			}

			return node;
		});
	}
}
