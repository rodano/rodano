import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {BaseTreeComponent} from '../base-tree.component';

@Component({
	selector: 'app-workflow-summary-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: '../simple-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class WorkflowSummaryTreeComponent extends BaseTreeComponent {
	@Input() workflowSummaries: any[] = [];
	@Input() selectedWorkflowSummaryId: string | null = null;

	constructor(languageService: LanguageService) {
		super(languageService);
	}

	getCategoryIcon(): string {return 'summarize';}
	getCategoryLabel(): string {return 'Workflow Summaries';}
	getCategoryTheme(): string {return 'theme-workflow-summary';}

	protected buildTree(): void {
		this.treeNodes = this.workflowSummaries.map(ws => ({
			id: `workflow-summary-${ws.workflowSummaryId}`,
			label: this.languageService.getDefaultTranslation(ws.title) || ws.id,
			icon: 'receipt',
			type: 'workflow-summary',
			selected: this.selectedWorkflowSummaryId === ws.workflowSummaryId,
			entityId: ws.workflowSummaryId,
			themeClass: 'theme-workflow-summary'
		}));
	}
}
