import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatIconModule} from '@angular/material/icon';
import {ChartModel} from '@core/model/chart-model';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface ChartColorsDialogData {
	chart: ChartModel;
}

interface ColorEntry {
	id: string;
	value: string;
}

@Component({
	selector: 'app-chart-colors-dialog',
	standalone: true,
	templateUrl: './chart-colors-dialog.component.html',
	styleUrls: ['../../dialog-shared.css', './chart-colors-dialog.component.css'],
	imports: [CommonModule, MatDialogModule, MatIconModule]
})
export class ChartColorsDialogComponent extends BaseDialogComponent<ChartColorsDialogData> implements OnInit {
	colors: ColorEntry[] = [];

	constructor(
		dialogRef: MatDialogRef<ChartColorsDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: ChartColorsDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.colors = this.data.chart.colors
			? this.data.chart.colors.map(c => ({id: crypto.randomUUID(), value: c}))
			: [];
	}

	addColor(): void {
		this.colors = [...this.colors, {id: crypto.randomUUID(), value: '#000000'}];
	}

	removeColor(id: string): void {
		this.colors = this.colors.filter(c => c.id !== id);
	}

	onColorPickerClick(input: HTMLInputElement): void {
		input.click();
	}

	onColorPickerChange(id: string, event: Event): void {
		const value = (event.target as HTMLInputElement).value;
		this.updateColor(id, value);
	}

	onColorTextInput(id: string, event: Event): void {
		const value = (event.target as HTMLInputElement).value;
		if(/^#[0-9A-Fa-f]{6}$/.test(value)) {
			this.updateColor(id, value);
		}
	}

	private updateColor(id: string, value: string): void {
		this.colors = this.colors.map(c => c.id === id ? {...c, value} : c);
	}

	onSave(): void {
		this.dialogRef.close(this.colors.map(c => c.value));
	}
}
