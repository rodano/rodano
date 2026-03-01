import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {TreeNode} from '../tree-node';
import {LanguageService} from '../../services/language.service';

@Component({
	selector: 'app-resource-category-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: './resource-category-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ResourceCategoryTreeComponent implements OnChanges {
	@Input() projectId = '';
	@Input() resourceCategories: any[] = [];
	@Input() expanded = false;
	@Input() selectedResourceCategoryId: string | null = null;
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
		if(!this.resourceCategories.length) {
			return;
		}

		this.treeNodes = this.resourceCategories.map(rc => ({
			id: `resource-category-${rc.categoryId}`,
			label: this.languageService.getDefaultTranslation(rc.shortname) || rc.id,
			icon: 'label',
			type: 'resource-category',
			selected: this.selectedResourceCategoryId === rc.categoryId,
			entityId: rc.categoryId,
			themeClass: 'theme-resource-category'
		}));
	}
}
