import {Directive, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {TreeNode} from './tree-node';
import {LanguageService} from '../services/language.service';

@Directive()
export abstract class BaseTreeComponent implements OnChanges {
	@Input() projectId = '';
	@Input() expanded = false;
	@Output() categoryClicked = new EventEmitter<void>();

	treeNodes: TreeNode[] = [];

	protected constructor(protected languageService: LanguageService) {}

	ngOnChanges(): void {
		this.buildTree();
	}

	onCategoryClick(): void {
		this.categoryClicked.emit();
	}

	abstract getCategoryIcon(): string;
	abstract getCategoryLabel(): string;
	abstract getCategoryTheme(): string;
	protected abstract buildTree(): void;
}
