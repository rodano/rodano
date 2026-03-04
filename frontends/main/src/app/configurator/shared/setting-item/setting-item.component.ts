import {Component, HostBinding, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
	selector: 'app-setting-item',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatTooltipModule],
	templateUrl: './setting-item.component.html',
	styleUrls: ['./setting-item.component.css']
})
export class SettingItemComponent {
	@Input() label = '';
	@Input() modified = false;
	@Input() notConfigured = false;
	@Input() fullWidth = false;
	@Input() plain = false;

	@HostBinding('style.grid-column') get gridColumn() {
		return this.fullWidth ? '1 / -1' : null;
	}
}
