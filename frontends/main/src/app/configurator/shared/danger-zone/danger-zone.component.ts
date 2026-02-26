import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatIconModule} from '@angular/material/icon';

@Component({
	selector: 'app-danger-zone',
	standalone: true,
	imports: [MatIconModule],
	templateUrl: './danger-zone.component.html',
	styleUrls: ['./danger-zone.component.css']
})
export class DangerZoneComponent {
	@Input() title = 'Delete';
	@Input() message = 'This action is permanent and cannot be undone.';
	@Input() buttonLabel = 'Delete';
	@Output() confirmed = new EventEmitter<void>();

	onDelete(): void {
		this.confirmed.emit();
	}
}
