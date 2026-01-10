import {Component, Inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatDialogRef, MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';

export interface ConfirmationDialogData {
	title: string;
	message: string;
	confirmText?: string;
	cancelText?: string;
	type?: 'warning' | 'danger' | 'info';
}

@Component({
	selector: 'app-confirmation-dialog',
	standalone: true,
	templateUrl: './confirmation-dialog.component.html',
	styleUrls: ['./confirmation-dialog.component.css'],
	imports: [
		CommonModule,
		MatDialogModule,
		MatButtonModule,
		MatIconModule
	]
})
export class ConfirmationDialogComponent {
	constructor(
		public dialogRef: MatDialogRef<ConfirmationDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: ConfirmationDialogData
	) {
		this.data.confirmText = data.confirmText || 'Confirm';
		this.data.cancelText = data.cancelText || 'Cancel';
		this.data.type = data.type || 'warning';
	}

	onCancel(): void {
		this.dialogRef.close(false);
	}

	onConfirm(): void {
		this.dialogRef.close(true);
	}

	getIconForType(): string {
		switch(this.data.type) {
			case 'danger':
				return 'delete_forever';
			case 'warning':
				return 'warning';
			case 'info':
				return 'info';
			default:
				return 'help_outline';
		}
	}
}
