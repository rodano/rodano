import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {BaseTreeComponent} from '../base-tree.component';
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
	selector: 'app-workflow-widget-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatTooltipModule],
	templateUrl: '../simple-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class WorkflowWidgetTreeComponent extends BaseTreeComponent {
	@Input() workflowWidgets: any[] = [];
	@Input() selectedWorkflowWidgetId: string | null = null;

	constructor(languageService: LanguageService) {
		super(languageService);
	}

	getCategoryIcon(): string {return 'widgets';}
	getCategoryLabel(): string {return 'Workflow Widgets';}
	getCategoryTheme(): string {return 'theme-workflow-widget';}

	protected buildTree(): void {
		this.treeNodes = this.workflowWidgets.map(ww => ({
			id: `workflow-widget-${ww.workflowWidgetId}`,
			label: this.languageService.getDefaultTranslation(ww.shortname) || ww.id,
			icon: 'widget_small',
			type: 'workflow-widget',
			selected: this.selectedWorkflowWidgetId === ww.workflowWidgetId,
			entityId: ww.workflowWidgetId,
			themeClass: 'theme-workflow-widget'
		}));
	}
}
