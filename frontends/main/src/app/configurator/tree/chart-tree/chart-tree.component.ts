import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {BaseTreeComponent} from '../base-tree.component';
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
	selector: 'app-chart-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatTooltipModule],
	templateUrl: '../simple-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ChartTreeComponent extends BaseTreeComponent {
	@Input() charts: any[] = [];
	@Input() selectedChartId: string | null = null;

	constructor(languageService: LanguageService) {
		super(languageService);
	}

	getCategoryIcon(): string {return 'bar_chart';}
	getCategoryLabel(): string {return 'Charts';}
	getCategoryTheme(): string {return 'theme-chart';}

	protected buildTree(): void {
		this.treeNodes = this.charts.map(c => ({
			id: `chart-${c.chartId}`,
			label: this.languageService.getDefaultTranslation(c.shortname) || c.id,
			icon: 'bar_chart_4_bars',
			type: 'chart',
			selected: this.selectedChartId === c.chartId,
			entityId: c.chartId,
			themeClass: 'theme-chart'
		}));
	}
}
