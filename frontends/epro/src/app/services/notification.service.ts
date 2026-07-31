import {Service, inject} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';

@Service()
export class NotificationService {
	private snackBar = inject(MatSnackBar);

	showSuccess(message: string): void {
		this.snackBar.open(message, '', {
			duration: 3000
		});
	}

	showError(message: string): void {
		this.snackBar.open(message, '', {
			duration: 3000,
			horizontalPosition: 'center',
			verticalPosition: 'bottom'
		});
	}
}
