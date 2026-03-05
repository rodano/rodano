import {Directive, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';

@Directive()
export abstract class BaseDialogComponent<TData = any> {
	protected constructor(
		protected dialogRef: MatDialogRef<any>,
		@Inject(MAT_DIALOG_DATA) public data: TData,
		private snackBar?: MatSnackBar
	) {}

	onCancel(): void {
		this.dialogRef.close(null);
	}

	protected showError(message = 'Please fill in all required fields'): void {
		this.snackBar?.open(message, 'Close', {duration: 3000});
	}

	protected normalize(value: any): any {
		return value === '' || value === null || value === undefined ? null : value;
	}
}
