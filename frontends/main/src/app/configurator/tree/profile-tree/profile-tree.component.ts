import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {BaseTreeComponent} from '../base-tree.component';
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
	selector: 'app-profile-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatTooltipModule],
	templateUrl: '../simple-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ProfileTreeComponent extends BaseTreeComponent {
	@Input() profiles: any[] = [];
	@Input() selectedProfileId: string | null = null;

	constructor(languageService: LanguageService) {
		super(languageService);
	}

	getCategoryIcon(): string {return 'badge';}
	getCategoryLabel(): string {return 'Profiles';}
	getCategoryTheme(): string {return 'theme-profile';}

	protected buildTree(): void {
		this.treeNodes = this.profiles.map(p => ({
			id: `profile-${p.profileId}`,
			label: this.languageService.getDefaultTranslation(p.shortname) || p.id,
			icon: 'account_circle',
			type: 'profile',
			selected: this.selectedProfileId === p.profileId,
			entityId: p.profileId,
			themeClass: 'theme-profile'
		}));
	}
}
