import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {BaseTreeComponent} from '../base-tree.component';
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
	selector: 'app-feature-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatTooltipModule],
	templateUrl: '../simple-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class FeatureTreeComponent extends BaseTreeComponent {
	@Input() features: any[] = [];
	@Input() selectedFeatureId: string | null = null;

	constructor(languageService: LanguageService) {
		super(languageService);
	}

	getCategoryIcon(): string {return 'stars';}
	getCategoryLabel(): string {return 'Features';}
	getCategoryTheme(): string {return 'theme-feature';}

	protected buildTree(): void {
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
