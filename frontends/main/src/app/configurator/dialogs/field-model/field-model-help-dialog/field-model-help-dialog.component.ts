import {FieldModel} from '@core/model/field-model';
import {ProjectLanguage} from '@core/model/project-language';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface FieldModelHelpDialogData {
	fieldModel: FieldModel;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-field-model-help-dialog',
	standalone: true,
	templateUrl: './field-model-help-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule,
		MatIconModule, MatTabsModule]
})
export class FieldModelHelpDialogComponent extends BaseDialogComponent<FieldModelHelpDialogData> implements OnInit {
	form: FormGroup;
	availableLanguages: ProjectLanguage[] = [];

	constructor(
		private fb: FormBuilder,
		dialogRef: MatDialogRef<FieldModelHelpDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: FieldModelHelpDialogData,
		snackBar: MatSnackBar
	) {
		super(dialogRef, data, snackBar);
	}

	ngOnInit(): void {
		this.loadProjectLanguages();
		this.initializeForm();
	}

	loadProjectLanguages(): void {
		this.availableLanguages = this.data.languages || [];
		if(this.availableLanguages.length === 0) {
			this.availableLanguages = [{languageCode: 'en', isDefault: true}];
		}
	}

	initializeForm(): void {
		const fm = this.data.fieldModel;

		const formConfig: any = {
			inlineHelp: [fm.inlineHelp || '']
		};

		this.availableLanguages.forEach(lang => {
			if(lang.languageCode) {
				formConfig[`advancedHelp_${lang.languageCode}`] = [
					fm.advancedHelp?.[lang.languageCode] || ''
				];
			}
		});

		this.form = this.fb.group(formConfig);
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

	onSave(): void {
		const formValue = this.form.getRawValue();

		const advancedHelp: Record<string, string> = {};
		this.availableLanguages.forEach(lang => {
			if(lang.languageCode) {
				const value = formValue[`advancedHelp_${lang.languageCode}`];
				if(value) {
					advancedHelp[lang.languageCode] = value;
				}
			}
		});

		const result = {
			inlineHelp: formValue.inlineHelp,
			advancedHelp
		};

		this.dialogRef.close(result);
	}
}
