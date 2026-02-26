import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {TreeNode} from '../tree-node';
import {LanguageService} from '../../services/language.service';

@Component({
	selector: 'app-profile-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: './profile-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ProfileTreeComponent implements OnChanges {
	@Input() projectId = '';
	@Input() profiles: any[] = [];
	@Input() expanded = false;
	@Input() selectedProfileId: string | null = null;
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
		if(!this.profiles.length) {
			return;
		}

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
