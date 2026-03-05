import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {BaseTreeComponent} from '../base-tree.component';

@Component({
	selector: 'app-privacy-policy-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: '../simple-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class PrivacyPolicyTreeComponent extends BaseTreeComponent {
	@Input() privacyPolicies: any[] = [];
	@Input() selectedPrivacyPolicyId: string | null = null;

	constructor(languageService: LanguageService) {
		super(languageService);
	}

	getCategoryIcon(): string {return 'security';}
	getCategoryLabel(): string {return 'Privacy Policies';}
	getCategoryTheme(): string {return 'theme-privacy-policy';}

	protected buildTree(): void {
		this.treeNodes = this.privacyPolicies.map(pp => ({
			id: `privacy-policy-${pp.policyId}`,
			label: this.languageService.getDefaultTranslation(pp.shortname) || pp.id,
			icon: 'policy',
			type: 'privacy-policy',
			selected: this.selectedPrivacyPolicyId === pp.policyId,
			entityId: pp.policyId,
			themeClass: 'theme-privacy-policy'
		}));
	}
}
