import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../services/language.service';
import {BaseTreeComponent} from '../base-tree.component';
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
	selector: 'app-validator-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatTooltipModule],
	templateUrl: '../simple-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ValidatorTreeComponent extends BaseTreeComponent {
	@Input() validators: any[] = [];
	@Input() selectedValidatorId: string | null = null;

	constructor(languageService: LanguageService) {
		super(languageService);
	}

	getCategoryIcon(): string {return 'check_circle';}
	getCategoryLabel(): string {return 'Validators';}
	getCategoryTheme(): string {return 'theme-validator';}

	protected buildTree(): void {
		this.treeNodes = this.validators.map(v => ({
			id: `validator-${v.validatorId}`,
			label: this.languageService.getDefaultTranslation(v.shortname) || v.id,
			icon: 'verified',
			type: 'validator',
			selected: this.selectedValidatorId === v.validatorId,
			entityId: v.validatorId,
			themeClass: 'theme-validator'
		}));
	}
}
