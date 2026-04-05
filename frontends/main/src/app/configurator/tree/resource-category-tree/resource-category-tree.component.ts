import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {BaseTreeComponent} from '../base-tree.component';
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
	selector: 'app-resource-category-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatTooltipModule],
	templateUrl: '../simple-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ResourceCategoryTreeComponent extends BaseTreeComponent {
	@Input() resourceCategories: any[] = [];
	@Input() selectedResourceCategoryId: string | null = null;

	constructor(languageService: LanguageService) {
		super(languageService);
	}

	getCategoryIcon(): string {return 'eco';}
	getCategoryLabel(): string {return 'Resource Categories';}
	getCategoryTheme(): string {return 'theme-resource-category';}

	protected buildTree(): void {
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
