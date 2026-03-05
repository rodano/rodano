import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {BaseTreeComponent} from '../base-tree.component';

@Component({
	selector: 'app-report-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: '../simple-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ReportTreeComponent extends BaseTreeComponent {
	@Input() reports: any[] = [];
	@Input() selectedReportId: string | null = null;

	constructor(languageService: LanguageService) {
		super(languageService);
	}

	getCategoryIcon(): string {return 'analytics';}
	getCategoryLabel(): string {return 'Reports';}
	getCategoryTheme(): string {return 'theme-report';}

	protected buildTree(): void {
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
