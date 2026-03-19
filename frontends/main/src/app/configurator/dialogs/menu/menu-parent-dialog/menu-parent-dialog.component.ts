import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatSelectModule} from '@angular/material/select';
import {MatButtonModule} from '@angular/material/button';
import {MenuConfig} from '@core/model/menu-config';
import {LanguageService} from '../../../services/language.service';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface MenuParentDialogData {
	menu: MenuConfig;
	allMenus: MenuConfig[];
}

@Component({
	selector: 'app-menu-parent-dialog',
	standalone: true,
	templateUrl: './menu-parent-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatSelectModule, MatButtonModule]
})
export class MenuParentDialogComponent extends BaseDialogComponent<MenuParentDialogData> implements OnInit {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		public languageService: LanguageService,
		dialogRef: MatDialogRef<MenuParentDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: MenuParentDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.form = this.fb.group({
			parentMenuId: [this.data.menu.parentMenuId ?? null]
		});
	}

	get availableParents(): MenuConfig[] {
		return this.data.allMenus.filter(m => m.menuId !== this.data.menu.menuId);
	}

	onSave(): void {
		this.dialogRef.close({
			parentMenuId: this.form.value.parentMenuId ?? null
		});
	}
}
