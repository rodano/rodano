import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {UsedByService, UsageResult} from '../../services/used-by.service';
import {ConfiguratorNavigationService} from '../../services/configurator-navigation.service';
import {LanguageService} from '../../services/language.service';

@Component({
	selector: 'app-used-by',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: './used-by.component.html',
	styleUrls: ['../../shared/detail-shared.css']
})
export class UsedByComponent {
	@Input() entityId: string | undefined = undefined;

	constructor(
		public navigationService: ConfiguratorNavigationService,
		public languageService: LanguageService,
		private usedByService: UsedByService
	) {}

	get usages(): UsageResult[] {
		return this.entityId ? this.usedByService.getUsages(this.entityId) : [];
	}

	navigateTo(entityType: string, entityId: string): void {
		this.navigationService.navigateTo(entityType, entityId);
	}
}
