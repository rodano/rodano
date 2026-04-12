import {Injectable} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';

@Injectable({
	providedIn: 'root'
})
export class NotificationService {
	constructor(
		public snackBar: MatSnackBar
	) { }

	showSuccess(message: string): void {
		this.snackBar.open(message, '', {
			duration: 3000
		});
	}

	showError(message: string, duration = 3000) {
		this.snackBar.open(message, '', {
			duration,
			horizontalPosition: 'center',
			verticalPosition: 'bottom'
		});
	}
}
