import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {BaseTreeComponent} from '../base-tree.component';
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
	selector: 'app-rule-definition-action-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatTooltipModule],
	templateUrl: '../simple-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class RuleDefinitionActionTreeComponent extends BaseTreeComponent {
	@Input() ruleDefinitionActions: any[] = [];
	@Input() selectedRuleDefinitionActionId: string | null = null;

	constructor(languageService: LanguageService) {
		super(languageService);
	}

	getCategoryIcon(): string {return 'more_up';}
	getCategoryLabel(): string {return 'Rule Definition Actions';}
	getCategoryTheme(): string {return 'theme-rule-def-action';}

	protected buildTree(): void {
		this.treeNodes = this.ruleDefinitionActions.map(rda => ({
			id: `rule-definition-action-${rda.ruleDefinitionActionId}`,
			label: this.languageService.getDefaultTranslation(rda.label) || rda.id,
			icon: 'arrow_circle_right',
			type: 'rule-definition-action',
			selected: this.selectedRuleDefinitionActionId === rda.ruleDefinitionActionId,
			entityId: rda.ruleDefinitionActionId,
			themeClass: 'theme-rule-def-action'
		}));
	}
}
