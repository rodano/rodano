import {Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {MatButton} from '@angular/material/button';

export interface ConfirmDialogData {
	title: string;
	message: string;
	//when undefined, the dialog only displays a dismiss button and acts as a simple alert
	confirmLabel?: string;
	dismissLabel?: string;
}

@Component({
	selector: 'app-confirm-dialog',
	templateUrl: './confirm.dialog.html',
	imports: [MatDialogModule, MatButton]
})
export class ConfirmDialogComponent {
	readonly data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
}
