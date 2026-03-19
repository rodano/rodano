import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {CdkDragDrop, DragDropModule, moveItemInArray} from '@angular/cdk/drag-drop';
import {MenuConfig} from '@core/model/menu-config';
import {LanguageService} from '../../../services/language.service';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface MenuSubmenusOrderDialogData {
	submenus: MenuConfig[];
}

@Component({
	selector: 'app-menu-submenus-order-dialog',
	standalone: true,
	templateUrl: './menu-submenus-order-dialog.component.html',
	styleUrls: ['./menu-submenus-order-dialog.component.css'],
	imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, DragDropModule]
})
export class MenuSubmenusOrderDialogComponent
	extends BaseDialogComponent<MenuSubmenusOrderDialogData>
	implements OnInit {
	submenus: MenuConfig[] = [];

	constructor(
		public languageService: LanguageService,
		dialogRef: MatDialogRef<MenuSubmenusOrderDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: MenuSubmenusOrderDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.submenus = [...this.data.submenus].sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0));
	}

	drop(event: CdkDragDrop<MenuConfig[]>): void {
		moveItemInArray(this.submenus, event.previousIndex, event.currentIndex);
	}

	onSave(): void {
		const result = this.submenus.map((m, i) => ({...m, sortOrder: i}));
		this.dialogRef.close(result);
	}
}
