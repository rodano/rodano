import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';

@Component({
	selector: 'app-list-header',
	standalone: true,
	imports: [CommonModule],
	templateUrl: './list-header.component.html',
	styleUrls: ['./list-header.component.css']
})
export class ListHeaderComponent {
	@Input() title = '';
	@Input() subtitle = '';
}
