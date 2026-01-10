import {Component, Inject} from '@angular/core';
import {ConfigSnapshot} from '@core/model/config-snapshot';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatListModule} from '@angular/material/list';
import {MatTooltipModule} from '@angular/material/tooltip';

export interface SnapshotsListDialogData {
	snapshots: ConfigSnapshot;
}

@Component({
	selector: 'app-snapshots-list-dialog',
	standalone: true,
	imports: [
		CommonModule,
		MatDialogModule,
		MatButtonModule,
		MatIconModule,
		MatListModule,
		MatTooltipModule
	],
	templateUrl: './snapshots-list-dialog.component.html',
	styleUrls: ['./snapshots-list-dialog.component.css']
})
export class SnapshotsListDialogComponent {
	selectedIndex: number | null = null;

	constructor(
		public dialogRef: MatDialogRef<SnapshotsListDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: SnapshotsListDialogData
	) {
		this.selectedIndex = this.data.snapshots.currentIndex ?? null;
	}

	onClose(): void {
		this.dialogRef.close();
	}

	onSelectSnapshot(index: number): void {
		this.selectedIndex = index;
	}

	onRestore(): void {
		if(this.selectedIndex !== null && this.selectedIndex !== this.data.snapshots.currentIndex) {
			this.dialogRef.close({action: 'restore', index: this.selectedIndex});
		}
	}

	get canRestore(): boolean {
		return this.selectedIndex !== null && this.selectedIndex !== this.data.snapshots.currentIndex;
	}
}
