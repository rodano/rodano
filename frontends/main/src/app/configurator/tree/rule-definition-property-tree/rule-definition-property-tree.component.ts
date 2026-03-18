import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {BaseTreeComponent} from '../base-tree.component';

@Component({
	selector: 'app-rule-definition-property-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: '../simple-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class RuleDefinitionPropertyTreeComponent extends BaseTreeComponent {
	@Input() ruleDefinitionProperties: any[] = [];
	@Input() selectedRuleDefinitionPropertyId: string | null = null;

	constructor(languageService: LanguageService) {
		super(languageService);
	}

	getCategoryIcon(): string {return 'apartment';}
	getCategoryLabel(): string {return 'Rule Definition Properties';}
	getCategoryTheme(): string {return 'theme-rule-def-property';}

	protected buildTree(): void {
		this.treeNodes = this.ruleDefinitionProperties.map(rdp => ({
			id: `rule-definition-property-${rdp.ruleDefinitionPropertyId}`,
			label: this.languageService.getDefaultTranslation(rdp.label) || rdp.id,
			icon: 'other_houses',
			type: 'rule-definition-property',
			selected: this.selectedRuleDefinitionPropertyId === rdp.ruleDefinitionPropertyId,
			entityId: rdp.ruleDefinitionPropertyId,
			themeClass: 'theme-rule-def-property'
		}));
	}
}
