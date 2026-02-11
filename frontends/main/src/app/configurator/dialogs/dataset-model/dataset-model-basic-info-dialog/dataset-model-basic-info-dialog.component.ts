import {DatasetModel} from '@core/model/dataset-model';
import {ProjectLanguage} from '@core/model/project-language';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {DatasetModelService} from '../../../services/api/dataset-model.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {HttpErrorResponse} from '@angular/common/http';
import {MatCheckboxModule} from '@angular/material/checkbox';

export interface DatasetModelBasicInfoDialogData {
	projectId: string;
	datasetModel: DatasetModel | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-dataset-model-basic-info-dialog',
	standalone: true,
	templateUrl: './dataset-model-basic-info-dialog.component.html',
	styleUrls: ['../../shared-dialog-styles.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatTabsModule,
		MatCheckboxModule
	]
})
export class DatasetModelBasicInfoDialogComponent implements OnInit {
	form: FormGroup;
	languageForms = new Map<string, FormGroup>();
	availableLanguages: ProjectLanguage[] = [];
	isEditMode: boolean;
	allDatasetModels: DatasetModel[] = [];

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<DatasetModelBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: DatasetModelBasicInfoDialogData,
		private datasetModelService: DatasetModelService,
		private snackBar: MatSnackBar
	) {
		this.isEditMode = !!data.datasetModel;
	}

	ngOnInit(): void {
		this.loadProjectLanguages();
		this.loadAllDatasetModels();
		this.initializeForm();
	}

	loadProjectLanguages(): void {
		this.availableLanguages = this.data.languages || [];

		if(this.availableLanguages.length === 0) {
			this.availableLanguages = [{languageCode: 'en', isDefault: true}];
		}

		this.initializeLanguageForms();
	}

	loadAllDatasetModels(): void {
		this.datasetModelService.getDatasetModels(this.data.projectId).subscribe({
			next: (datasetModels: DatasetModel[]) => {
				this.allDatasetModels = datasetModels;
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error loading dataset models:', error);
			}
		});
	}

	initializeForm(): void {
		const dm = this.data.datasetModel;

		this.form = this.fb.group({
			id: [
				dm?.id || '',
				[Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]
			],
			multiple: [dm?.multiple ?? false]
		});
	}

	initializeLanguageForms(): void {
		this.availableLanguages.forEach((lang: ProjectLanguage) => {
			if(lang.languageCode) {
				const dm = this.data.datasetModel;
				const langForm = this.fb.group({
					shortname: [
						dm?.shortname?.[lang.languageCode] || '',
						lang.isDefault ? Validators.required : []
					],
					longname: [dm?.longname?.[lang.languageCode] || ''],
					description: [dm?.description?.[lang.languageCode] || '']
				});
				this.languageForms.set(lang.languageCode, langForm);
			}
		});
	}

	getLanguageLabel(code: string, isDefault: boolean): string {
		const name = this.getLanguageName(code);
		return isDefault ? `${name} ★` : name;
	}

	getLanguageName(code: string): string {
		try {
			const displayNames = new Intl.DisplayNames(['en'], {type: 'language'});
			return displayNames.of(code) || code.toUpperCase();
		}
		catch (error) {
			console.error(error);
			return code.toUpperCase();
		}
	}

	areLanguageFormsValid(): boolean {
		let allValid = true;
		this.languageForms.forEach((langForm: FormGroup) => {
			if(langForm.invalid) {
				allValid = false;
			}
		});
		return allValid;
	}

	onIdInput(event: Event): void {
		const input = event.target as HTMLInputElement;
		const uppercaseValue = input.value.toUpperCase();
		input.value = uppercaseValue;
		this.form.patchValue({id: uppercaseValue}, {emitEvent: false});
	}

	isCodeDuplicate(code: string): boolean {
		const currentDatasetModelId = this.data.datasetModel?.datasetModelId;
		return this.allDatasetModels.some(dm =>
			dm.id.toUpperCase() === code.toUpperCase() && dm.datasetModelId !== currentDatasetModelId
		);
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const formValue = this.form.getRawValue();
		const code = formValue.id.toUpperCase();

		if(this.isCodeDuplicate(code)) {
			this.snackBar.open(`A dataset model with code "${code}" already exists`, 'Close', {duration: 3000});
			return;
		}

		const shortname: Record<string, string> = {};
		const longname: Record<string, string> = {};
		const description: Record<string, string> = {};

		this.languageForms.forEach((langForm: FormGroup, langCode: string) => {
			const langValue = langForm.value;
			if(langValue.shortname) {
				shortname[langCode] = langValue.shortname;
			}
			if(langValue.longname) {
				longname[langCode] = langValue.longname;
			}
			if(langValue.description) {
				description[langCode] = langValue.description;
			}
		});

		const result = {
			id: code,
			shortname,
			longname,
			description,
			multiple: formValue.multiple
		};

		this.dialogRef.close(result);
	}
}
