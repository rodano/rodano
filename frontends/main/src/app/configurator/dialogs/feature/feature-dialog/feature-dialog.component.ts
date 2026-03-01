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
import {FeatureManagerService} from '../../../services/manager/feature-manager.service';
import {LanguageService} from '../../../services/language.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';
import {Feature} from '@core/model/feature';
import {MatCheckbox} from '@angular/material/checkbox';

export interface FeatureDialogData {
	projectId: string;
	feature: Feature | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-feature-create-dialog',
	standalone: true,
	imports: [
		CommonModule,
		MatDialogModule,
		MatButtonModule,
		MatIconModule,
		ReactiveFormsModule,
		MatInputModule,
		MatTabsModule,
		MatCheckbox
	],
	templateUrl: './feature-dialog.component.html',
	styleUrls: ['../../dialog-shared.css']
})
export class FeatureDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode = false;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<FeatureDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: FeatureDialogData,
		private featureManager: FeatureManagerService,
		private snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef);
		this.isEditMode = !!data.feature;
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
			const f = this.data.feature;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [f?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [f?.longname?.[lang.languageCode] || ''],
				description: [f?.description?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		const f = this.data.feature;
		this.form = this.fb.group({
			id: [f?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			optional: [f?.optional ?? false]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentFeatureId = this.data.feature?.featureId;
		return this.featureManager.getAll().some(f =>
			f.id.toUpperCase() === code.toUpperCase() && f.featureId !== currentFeatureId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.snackBar.open(`A feature with code "${code}" already exists`, 'Close', {duration: 3000});
		}

		const {shortname, longname, description} = this.collectTranslations();
		this.dialogRef.close({
			id: code,
			shortname,
			longname,
			description,
			...this.form.value
		});
	}
}
