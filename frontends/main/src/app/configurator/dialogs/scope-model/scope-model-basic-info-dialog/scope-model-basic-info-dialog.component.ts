import {Component, Inject, OnInit} from '@angular/core';
import {ScopeModel} from '@core/model/scope-model';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {CommonModule} from '@angular/common';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatSelectModule} from '@angular/material/select';
import {MatTabsModule} from '@angular/material/tabs';
import {MatInputModule} from '@angular/material/input';
import {ScopeModelService} from '../../../services/api/scope-model.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {HttpErrorResponse} from '@angular/common/http';
import {ProjectLanguage} from '@core/model/project-language';

interface DialogData {
	projectId: string;
	scopeModel: ScopeModel | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-scope-model-basic-info-dialog',
	standalone: true,
	templateUrl: './scope-model-basic-info-dialog.component.html',
	styleUrls: ['../../shared-dialog-styles.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatFormFieldModule,
		MatInputModule,
		MatButtonModule,
		MatCheckboxModule,
		MatSelectModule,
		MatTabsModule
	]
})
export class ScopeModelBasicInfoDialogComponent implements OnInit {
	form: FormGroup;
	languageForms = new Map<string, FormGroup>();
	isEditMode: boolean;
	saving = false;

	allScopeModels: ScopeModel[] = [];
	availableLanguages: ProjectLanguage[] = [];

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<ScopeModelBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: DialogData,
		private configuratorConfigService: ScopeModelService,
		private snackBar: MatSnackBar
	) {
		this.isEditMode = !!data.scopeModel;

		this.form = this.fb.group({
			id: ['', [Validators.required]],
			virtual: [false],
			maxNumber: [null, [Validators.min(0)]]
		});
	}

	ngOnInit(): void {
		this.loadProjectLanguages();
		this.loadAllScopeModels();
	}

	loadProjectLanguages(): void {
		this.availableLanguages = this.data.languages || [];

		if(this.availableLanguages.length === 0) {
			this.availableLanguages = [{languageCode: 'en', isDefault: true}];
		}

		this.initializeLanguageForms();
	}

	loadAllScopeModels(): void {
		this.configuratorConfigService.getScopeModels(this.data.projectId).subscribe({
			next: (scopeModels: ScopeModel[]) => {
				this.allScopeModels = scopeModels;
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error loading scope models:', error);
			}
		});
	}

	initializeLanguageForms(): void {
		this.availableLanguages.forEach((lang: ProjectLanguage) => {
			if(lang.languageCode) {
				const langForm = this.fb.group({
					shortname: ['', lang.isDefault ? Validators.required : []],
					longname: [''],
					description: [''],
					pluralShortname: ['', lang.isDefault ? Validators.required : []]
				});
				this.languageForms.set(lang.languageCode, langForm);
			}
		});

		if(this.data.scopeModel) {
			this.populateForm();
		}
	}

	populateForm(): void {
		if(!this.data.scopeModel) {
			return;
		}

		const scopeModel = this.data.scopeModel;

		this.form.patchValue({
			id: scopeModel.id,
			virtual: scopeModel.virtual,
			maxNumber: scopeModel.maxNumber || null
		});

		this.languageForms.forEach((langForm: FormGroup, langCode: string) => {
			langForm.patchValue({
				shortname: scopeModel.shortname[langCode] || '',
				longname: scopeModel.longname?.[langCode] || '',
				description: scopeModel.description?.[langCode] || '',
				pluralShortname: scopeModel.pluralShortname[langCode] || ''
			});
		});
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const formValue = this.form.getRawValue();
		const code = formValue.id.toUpperCase();

		if(!this.isEditMode && this.isCodeDuplicate(code)) {
			this.snackBar.open(`A scope model with code "${code}" already exists`, 'Close', {duration: 3000});
			return;
		}

		const shortname: Record<string, string> = {};
		const longname: Record<string, string> = {};
		const description: Record<string, string> = {};
		const pluralShortname: Record<string, string> = {};

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
			if(langValue.pluralShortname) {
				pluralShortname[langCode] = langValue.pluralShortname;
			}
		});

		const result = {
			id: code,
			shortname,
			longname,
			description,
			pluralShortname,
			virtual: formValue.virtual,
			maxNumber: formValue.maxNumber
		};

		this.dialogRef.close(result);
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

	onCodeInput(event: Event): void {
		const input = event.target as HTMLInputElement;
		const uppercaseValue = input.value.toUpperCase();
		input.value = uppercaseValue;
		this.form.patchValue({id: uppercaseValue}, {emitEvent: false});
	}

	isCodeDuplicate(code: string): boolean {
		const currentScopeModelId = this.data.scopeModel?.scopeModelId;
		return this.allScopeModels.some(sm =>
			sm.id.toUpperCase() === code.toUpperCase() && sm.scopeModelId !== currentScopeModelId);
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}

	getLanguageName(code: string | undefined): string {
		if(!code) {
			return 'Unknown';
		}
		try {
			const displayNames = new Intl.DisplayNames(['en'], {type: 'language'});
			return displayNames.of(code) || code.toUpperCase();
		}
		catch (e) {
			console.error(e);
			return code.toUpperCase();
		}
	}

	getLanguageLabel(code: string, isDefault: boolean): string {
		const name = this.getLanguageName(code);
		return isDefault ? `${name} ★` : name;
	}
}
