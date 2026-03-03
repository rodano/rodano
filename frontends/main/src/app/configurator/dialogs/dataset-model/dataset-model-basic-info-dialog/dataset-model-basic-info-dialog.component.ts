import {DatasetModel} from '@core/model/dataset-model';
import {ProjectLanguage} from '@core/model/project-language';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {DatasetModelManagerService} from '../../../services/manager/dataset-model-manager.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';
import {LanguageService} from '../../../services/language.service';

export interface DatasetModelBasicInfoDialogData {
	projectId: string;
	datasetModel: DatasetModel | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-dataset-model-basic-info-dialog',
	standalone: true,
	templateUrl: './dataset-model-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatTabsModule,
		MatCheckboxModule
	]
})
export class DatasetModelBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode: boolean;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<DatasetModelBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: DatasetModelBasicInfoDialogData,
		private datasetModelManager: DatasetModelManagerService,
		private snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef);
		this.isEditMode = !!data.datasetModel;
	}

	ngOnInit(): void {
		this.loadProjectLanguages(this.data.languages);
		this.initializeForm();
	}

	protected initializeLanguageForms(): void {
		this.availableLanguages.forEach(lang => {
			if(!lang.languageCode) {
				return;
			}
			const dm = this.data.datasetModel;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [dm?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [dm?.longname?.[lang.languageCode] || ''],
				description: [dm?.description?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		const dm = this.data.datasetModel;
		this.form = this.fb.group({
			id: [dm?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			multiple: [dm?.multiple ?? false]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentDatasetModelId = this.data.datasetModel?.datasetModelId;
		return this.datasetModelManager.getAll().some(dm =>
			dm.id.toUpperCase() === code.toUpperCase() && dm.datasetModelId !== currentDatasetModelId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.snackBar.open(`A dataset model with code "${code}" already exists`, 'Close', {duration: 3000});
			return;
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
