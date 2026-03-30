import {Injectable, signal} from '@angular/core';

@Injectable({providedIn: 'root'})
export class AltDragService {
	readonly isAltHeld = signal(false);

	constructor() {
		document.addEventListener('keydown', e => {
			if(e.key === 'Alt') {
				this.isAltHeld.set(true);
			}
		});
		document.addEventListener('keyup', e => {
			if(e.key === 'Alt') {
				this.isAltHeld.set(false);
			}
		});
		window.addEventListener('blur', () => this.isAltHeld.set(false));
	}
}
