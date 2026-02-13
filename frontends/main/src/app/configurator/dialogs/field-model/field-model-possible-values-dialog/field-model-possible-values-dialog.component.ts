import {Component, Inject, OnInit} from '@angular/core';
import {FieldModel} from '@core/model/field-model';
import {CommonModule} from '@angular/common';
import {FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatTabsModule} from '@angular/material/tabs';
import {ProjectLanguage} from '@core/model/project-language';
import {PossibleValue} from '@core/model/possible-value';

export interface FieldModelPossibleValuesDialogData {
	fieldModel: FieldModel;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-field-model-possible-values-dialog',
	standalone: true,
	templateUrl: './field-model-possible-values-dialog.component.html',
	styleUrls: ['./field-model-possible-values-dialog.component.css', '../../dialog-shared.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatFormFieldModule,
		MatInputModule,
		MatButtonModule,
		MatIconModule,
		MatCheckboxModule,
		MatTabsModule
	]
})
export class FieldModelPossibleValuesDialogComponent implements OnInit {
	form: FormGroup;
	availableLanguages: ProjectLanguage[] = [];
	showPossibleValuesSection = false;

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<FieldModelPossibleValuesDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: FieldModelPossibleValuesDialogData,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadProjectLanguages();
		this.initializeForm();
		this.checkIfShowPossibleValues();
	}

	loadProjectLanguages(): void {
		this.availableLanguages = this.data.languages || [];
		if(this.availableLanguages.length === 0) {
			this.availableLanguages = [{languageCode: 'en', isDefault: true}];
		}
	}

	checkIfShowPossibleValues(): void {
		const fieldType = this.data.fieldModel.type;
		this.showPossibleValuesSection = ['AUTO_COMPLETION', 'SELECT', 'RADIO', 'CHECKBOX_GROUP'].includes(fieldType || '');
	}

	initializeForm(): void {
		const fm = this.data.fieldModel;

		this.form = this.fb.group({
			dictionary: [fm.dictionary || ''],
			possibleValuesProvider: [fm.possibleValuesProvider || ''],
			possibleValuesProviderDescription: [fm.possibleValuesProviderDescription || ''],
			possibleValues: this.fb.array([])
		});

		if(fm.possibleValues && fm.possibleValues.length > 0) {
			const sortedPvs = [...fm.possibleValues].sort((a, b) =>
				(a.sortOrder ?? 0) - (b.sortOrder ?? 0)
			);

			sortedPvs.forEach((pv, index) => {
				this.possibleValuesArray.push(this.createPossibleValueFormGroup(pv, index));
			});
		}
	}

	get possibleValuesArray(): FormArray {
		return this.form.get('possibleValues') as FormArray;
	}

	createPossibleValueFormGroup(pv?: PossibleValue, index?: number): FormGroup {
		const controls: any = {
			code: [pv?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			exportLabel: [pv?.exportLabel || ''],
			specify: [pv?.specify || false],
			sortOrder: [pv?.sortOrder ?? index ?? 0]
		};

		this.availableLanguages.forEach(lang => {
			if(lang.languageCode) {
				const isDefault = lang.isDefault || false;
				controls[`shortname_${lang.languageCode}`] = [
					pv?.shortname?.[lang.languageCode] || '',
					isDefault ? [Validators.required] : []
				];
			}
		});

		return this.fb.group(controls);
	}

	addPossibleValue(): void {
		const maxSortOrder = this.possibleValuesArray.controls.reduce((max, control) => {
			const sortOrder = control.get('sortOrder')?.value ?? 0;
			return Math.max(max, sortOrder);
		}, -1);

		this.possibleValuesArray.push(this.createPossibleValueFormGroup(undefined, maxSortOrder + 1));
	}

	removePossibleValue(index: number): void {
		this.possibleValuesArray.removeAt(index);
	}

	movePossibleValueUp(index: number): void {
		if(index > 0) {
			const array = this.possibleValuesArray;
			const controls = array.controls.slice();

			const currentSort = controls[index].get('sortOrder')?.value;
			const previousSort = controls[index - 1].get('sortOrder')?.value;
			controls[index].get('sortOrder')?.setValue(previousSort);
			controls[index - 1].get('sortOrder')?.setValue(currentSort);

			[controls[index - 1], controls[index]] = [controls[index], controls[index - 1]];

			array.clear();
			controls.forEach(control => array.push(control));
		}
	}

	movePossibleValueDown(index: number): void {
		if(index < this.possibleValuesArray.length - 1) {
			const array = this.possibleValuesArray;
			const controls = array.controls.slice();

			const currentSort = controls[index].get('sortOrder')?.value;
			const nextSort = controls[index + 1].get('sortOrder')?.value;
			controls[index].get('sortOrder')?.setValue(nextSort);
			controls[index + 1].get('sortOrder')?.setValue(currentSort);

			[controls[index], controls[index + 1]] = [controls[index + 1], controls[index]];

			array.clear();
			controls.forEach(control => array.push(control));
		}
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

	hasMultipleValues(): boolean {
		const fieldType = this.data.fieldModel.type;
		return fieldType === 'CHECKBOX_GROUP';
	}

	onCodeInput(event: Event, index: number): void {
		const input = event.target as HTMLInputElement;
		const uppercased = input.value.toUpperCase();
		const control = this.possibleValuesArray.at(index).get('code');
		if(control && control.value !== uppercased) {
			control.setValue(uppercased, {emitEvent: false});
		}
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}

	onSave(): void {
		if(this.form.invalid) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const formValue = this.form.getRawValue();

		const possibleValues: PossibleValue[] = formValue.possibleValues.map((pv: any) => {
			const shortname: Record<string, string> = {};
			this.availableLanguages.forEach(lang => {
				if(lang.languageCode) {
					const value = pv[`shortname_${lang.languageCode}`];
					if(value) {
						shortname[lang.languageCode] = value;
					}
				}
			});

			return {
				id: pv.code,
				shortname,
				exportLabel: pv.exportLabel,
				specify: pv.specify,
				sortOrder: pv.sortOrder
			} as PossibleValue;
		});

		const result = {
			dictionary: formValue.dictionary,
			possibleValuesProvider: formValue.possibleValuesProvider,
			possibleValuesProviderDescription: formValue.possibleValuesProviderDescription,
			possibleValues
		};

		this.dialogRef.close(result);
	}
}
