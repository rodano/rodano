import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {TreeNode} from '../tree-node';
import {BaseTreeComponent} from '../base-tree.component';

@Component({
	selector: 'app-menu-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: './menu-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class MenuTreeComponent extends BaseTreeComponent {
	@Input() menus: any[] = [];
	@Input() selectedMenuId: string | null = null;

	constructor(languageService: LanguageService) {
		super(languageService);
	}

	getCategoryIcon(): string {return 'menu';}
	getCategoryLabel(): string {return 'Menus';}
	getCategoryTheme(): string {return 'theme-menu';}

	protected buildTree(): void {
		if(!this.menus.length) {
			return;
		}

		const roots = this.menus
			.filter(m => !m.parentMenuId)
			.sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0));

		this.treeNodes = roots.map(root => this.buildNode(root));
	}

	private buildNode(menu: any): TreeNode {
		const children = this.menus
			.filter(m => m.parentMenuId === menu.menuId)
			.sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0));

		const hasChildren = children.length > 0;
		const hasParent = !!menu.parentMenuId;
		const icon = hasChildren ? 'folder_open' : (hasParent ? 'fiber_manual_record' : 'circle');

		const node: TreeNode = {
			id: `menu-${menu.menuId}`,
			label: this.languageService.getDefaultTranslation(menu.shortname) || menu.id,
			icon,
			expanded: true,
			selected: this.selectedMenuId === menu.menuId,
			type: 'menu',
			entityId: menu.menuId,
			themeClass: 'theme-menu'
		};

		node.children = hasChildren ? children.map(child => this.buildNode(child)) : undefined;

		return node;
	}
}
