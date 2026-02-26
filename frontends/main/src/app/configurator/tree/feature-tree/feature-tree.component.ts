import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {TreeNode} from '../tree-node';
import {LanguageService} from '../../services/language.service';

@Component({
	selector: 'app-feature-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: './feature-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class FeatureTreeComponent implements OnChanges {
	@Input() projectId = '';
	@Input() features: any[] = [];
	@Input() expanded = false;
	@Input() selectedFeatureId: string | null = null;
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
		if(!this.features.length) {
			return;
		}

		this.treeNodes = this.features.map(f => ({
			id: `feature-${f.featureId}`,
			label: this.languageService.getDefaultTranslation(f.shortname) || f.id,
			icon: 'tune',
			type: 'feature',
			selected: this.selectedFeatureId === f.featureId,
			entityId: f.featureId,
			themeClass: 'theme-feature'
		}));
	}
}
