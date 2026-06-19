import {Service, inject} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';

@Service()
export class NotificationService {
	public readonly snackBar = inject(MatSnackBar);

	showSuccess(message: string): void {
		this.snackBar.open(message, '', {
			duration: 3000
		});
	}

	showError(message: string) {
		this.snackBar.open(message, '', {
			duration: 3000,
			horizontalPosition: 'center',
			verticalPosition: 'bottom'
		});
	}
}
