import {ProjectLanguage} from '@core/model/project-language';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatInputModule} from '@angular/material/input';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {LanguageService} from '../../../services/language.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';
import {MatSelectModule} from '@angular/material/select';
import {MenuConfig} from '@core/model/menu-config';
import {MenuManagerService} from '../../../services/manager/menu-manager.service';
import {MatCheckboxModule} from '@angular/material/checkbox';

export interface MenuBasicInfoDialogData {
	projectId: string;
	menu: MenuConfig | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-menu-basic-info-dialog',
	standalone: true,
	templateUrl: './menu-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, ReactiveFormsModule, MatInputModule,
		MatTabsModule, MatSelectModule, MatCheckboxModule]
})
export class MenuBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode = false;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<MenuBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: MenuBasicInfoDialogData,
		private menuManager: MenuManagerService,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
		this.isEditMode = !!data.menu;
	}

	ngOnInit(): void {
		this.loadProjectLanguages(this.data.languages);
		this.initializeForm();
	}

	initializeLanguageForms(): void {
		this.availableLanguages.forEach(lang => {
			if(!lang.languageCode) {
				return;
			}
			const m = this.data.menu;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [m?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [m?.longname?.[lang.languageCode] || ''],
				description: [m?.description?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		const m = this.data.menu;
		this.form = this.fb.group({
			id: [m?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			orderBy: [m?.orderBy || null],
			public: [m?.public ?? false],
			homePage: [m?.homePage ?? false]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentCronId = this.data.menu?.menuId;
		return this.menuManager.getAll().some(m =>
			m.id.toUpperCase() === code.toUpperCase() && m.menuId !== currentCronId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.showError();
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.showError(`A menu with code "${code}" already exists`);
		}

		const {shortname, longname, description} = this.collectTranslations();
		this.dialogRef.close({
			...this.form.value,
			id: code,
			shortname,
			longname,
			description
		});
	}
}
