import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {TreeNode} from '../tree-node';
import {LanguageService} from '../../services/language.service';

@Component({
	selector: 'app-validator-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: './validator-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ValidatorTreeComponent implements OnChanges {
	@Input() projectId = '';
	@Input() validators: any[] = [];
	@Input() expanded = false;
	@Input() selectedValidatorId: string | null = null;
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
		if(!this.validators.length) {
			return;
		}

		this.treeNodes = this.validators.map(v => ({
			id: `validator-${v.validatorId}`,
			label: this.languageService.getDefaultTranslation(v.shortname) || v.id,
			icon: 'verified',
			type: 'validator',
			selected: this.selectedValidatorId === v.validatorId,
			entityId: v.validatorId,
			themeClass: 'theme-validator'
		}));
	}
}
