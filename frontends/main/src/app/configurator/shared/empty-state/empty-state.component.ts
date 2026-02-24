import {Component, Input} from '@angular/core';
import {MatIconModule} from '@angular/material/icon';

@Component({
	selector: 'app-empty-state',
	standalone: true,
	imports: [MatIconModule],
	templateUrl: './empty-state.component.html',
	styleUrls: ['./empty-state.component.css']
})
export class EmptyStateComponent {
	@Input() icon = '';
	@Input() title = '';
	@Input() message = '';
	@Input() size: 'normal' | 'small' = 'normal';
}
