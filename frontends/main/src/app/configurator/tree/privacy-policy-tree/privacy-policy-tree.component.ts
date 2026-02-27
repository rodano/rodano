import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {TreeNode} from '../tree-node';
import {LanguageService} from '../../services/language.service';

@Component({
	selector: 'app-privacy-policy-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: './privacy-policy-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class PrivacyPolicyTreeComponent implements OnChanges {
	@Input() projectId = '';
	@Input() privacyPolicies: any[] = [];
	@Input() expanded = false;
	@Input() selectedPrivacyPolicyId: string | null = null;
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
		if(!this.privacyPolicies.length) {
			return;
		}

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
