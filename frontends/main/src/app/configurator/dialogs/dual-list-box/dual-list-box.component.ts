import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';

@Component({
	selector: 'app-dual-list-box',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: './dual-list-box.component.html',
	styleUrls: ['./dual-list-box.component.css']
})
export class DualListBoxComponent<T = any> {
	@Input() title = '';
	@Input() hint = '';
	@Input() available: T[] = [];
	@Input() selected: T[] = [];
	@Input() labelFn: (item: T) => string = (item: any) => item?.toString() ?? '';
	@Input() trackFn?: (item: T) => any;
	@Input() availablePlaceholder = 'No available items';
	@Input() selectedPlaceholder = 'No items selected';

	@Output() add = new EventEmitter<T>();
	@Output() remove = new EventEmitter<T>();
}
