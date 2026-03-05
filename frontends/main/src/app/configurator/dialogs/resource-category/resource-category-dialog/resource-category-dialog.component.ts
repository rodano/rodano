import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSelectModule} from '@angular/material/select';
import {ProjectLanguage} from '@core/model/project-language';
import {MatSnackBar} from '@angular/material/snack-bar';
import {LanguageService} from '../../../services/language.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';
import {ResourceCategory} from '@core/model/resource-category';
import {ResourceCategoryManagerService} from '../../../services/manager/resource-category-manager.service';

export interface ResourceCategoryDialogData {
	projectId: string;
	resourceCategory: ResourceCategory | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-resource-category-dialog',
	standalone: true,
	templateUrl: './resource-category-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule,
		MatCheckboxModule, MatTabsModule, MatSelectModule]
})
export class ResourceCategoryDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode: boolean;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<ResourceCategoryDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: ResourceCategoryDialogData,
		private resourceCategoryManager: ResourceCategoryManagerService,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
		this.isEditMode = !!data.resourceCategory;
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
			const rc = this.data.resourceCategory;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [rc?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [rc?.longname?.[lang.languageCode] || ''],
				description: [rc?.description?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		const rc = this.data.resourceCategory;
		this.form = this.fb.group({
			id: [rc?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			categoryId: [rc?.categoryId || null],
			icon: [rc?.icon || null],
			color: [rc?.color || null]
		});
	}

	get iconPreview(): string {
		return this.form.get('icon')?.value?.trim() || '';
	}

	isCodeDuplicate(code: string): boolean {
		const currentResourceCategoryId = this.data.resourceCategory?.categoryId;
		return this.resourceCategoryManager.getAll().some(rc =>
			rc.id.toUpperCase() === code.toUpperCase() && rc.categoryId !== currentResourceCategoryId
		);
	}

	onColorInput(event: Event): void {
		const input = event.target as HTMLInputElement;
		const value = input.value;

		if(/^#[0-9A-F]{6}$/i.test(value)) {
			this.form.patchValue({color: value});
		}
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.showError();
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.showError(`A resource category with code "${code}" already exists`);
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
