import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {TreeNode} from '../tree-node';
import {LanguageService} from '../../services/language.service';

@Component({
	selector: 'app-report-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: './report-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ReportTreeComponent implements OnChanges {
	@Input() projectId = '';
	@Input() reports: any[] = [];
	@Input() expanded = false;
	@Input() selectedReportId: string | null = null;
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
		if(!this.reports.length) {
			return;
		}

		this.treeNodes = this.reports.map(r => ({
			id: `report-${r.reportId}`,
			label: this.languageService.getDefaultTranslation(r.shortname) || r.id,
			icon: 'article',
			type: 'report',
			selected: this.selectedReportId === r.reportId,
			entityId: r.reportId,
			themeClass: 'theme-report'
		}));
	}
}
