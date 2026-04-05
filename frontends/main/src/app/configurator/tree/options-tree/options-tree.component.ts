import {Component, Input, Output, EventEmitter} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
	selector: 'app-options-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatTooltipModule],
	templateUrl: './options-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class OptionsTreeComponent {
	@Input() collapsed = false;
	@Input() selected = false;
	@Output() categoryClicked = new EventEmitter<void>();
}
