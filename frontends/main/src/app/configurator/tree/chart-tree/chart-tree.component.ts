import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {TreeNode} from '../tree-node';
import {LanguageService} from '../../services/language.service';

@Component({
	selector: 'app-chart-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: './chart-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ChartTreeComponent implements OnChanges {
	@Input() projectId = '';
	@Input() charts: any[] = [];
	@Input() expanded = false;
	@Input() selectedChartId: string | null = null;
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
		if(!this.charts.length) {
			return;
		}

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
