import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {BaseTreeComponent} from '../base-tree.component';

@Component({
	selector: 'app-cron-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: '../simple-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class CronTreeComponent extends BaseTreeComponent {
	@Input() crons: any[] = [];
	@Input() selectedCronId: string | null = null;

	constructor(languageService: LanguageService) {
		super(languageService);
	}

	getCategoryIcon(): string {return 'alarm';}
	getCategoryLabel(): string {return 'Crons';}
	getCategoryTheme(): string {return 'theme-cron';}

	protected buildTree(): void {
		this.treeNodes = this.crons.map(c => ({
			id: `cron-${c.cronId}`,
			label: this.languageService.getDefaultTranslation(c.description) || c.id,
			icon: 'schedule',
			type: 'cron',
			selected: this.selectedCronId === c.cronId,
			entityId: c.cronId,
			themeClass: 'theme-cron'
		}));
	}
}
