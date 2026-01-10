import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';

@Component({
	selector: 'app-create-snapshot-dialog',
	standalone: true,
	imports: [
		CommonModule,
		FormsModule,
		MatDialogModule,
		MatButtonModule,
		MatFormFieldModule,
		MatInputModule
	],
	templateUrl: './create-snapshot-dialog.component.html',
	styleUrls: ['./create-snapshot-dialog.component.css']
})
export class CreateSnapshotDialogComponent {
	summary = '';

	constructor(
		public dialogRef: MatDialogRef<CreateSnapshotDialogComponent>
	) {}

	onCancel(): void {
		this.dialogRef.close();
	}

	onConfirm(): void {
		if(this.summary.trim()) {
			this.dialogRef.close(this.summary.trim());
		}
	}
}
