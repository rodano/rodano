import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';

@Component({
	selector: 'app-project-settings-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: 'project-settings-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ProjectSettingsTreeComponent {
	@Input() selected = false;
	@Output() categoryClicked = new EventEmitter<void>();

	onCategoryClick(): void {
		this.categoryClicked.emit();
	}
}
