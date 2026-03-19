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
import {Cron} from '@core/model/cron';
import {CronManagerService} from '../../../services/manager/cron-manager.service';
import {MatSelectModule} from '@angular/material/select';

export interface CronDialogData {
	projectId: string;
	cron: Cron | null;
	languages: ProjectLanguage[];
}

interface UnitOption {
	value: string;
	label: string;
}

@Component({
	selector: 'app-cron-dialog',
	standalone: true,
	templateUrl: './cron-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, ReactiveFormsModule, MatInputModule,
		MatTabsModule, MatSelectModule]
})
export class CronDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode = false;

	unitOptions: UnitOption[] = [
		{value: 'SECONDS', label: 'Seconds'},
		{value: 'MINUTES', label: 'Minutes'},
		{value: 'HOURS', label: 'Hours'},
		{value: 'DAYS', label: 'Days'},
		{value: 'WEEKS', label: 'Weeks'},
		{value: 'MONTHS', label: 'Months'},
		{value: 'YEARS', label: 'Years'}
	];

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<CronDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: CronDialogData,
		private cronManager: CronManagerService,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
		this.isEditMode = !!data.cron;
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
			const c = this.data.cron;
			this.languageForms.set(lang.languageCode, this.fb.group({
				description: [c?.description?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []]
			}));
		});
	}

	initializeForm(): void {
		const c = this.data.cron;
		this.form = this.fb.group({
			id: [c?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			intervalUnit: [c?.intervalUnit || null],
			intervalValue: [c?.intervalValue || null]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentCronId = this.data.cron?.cronId;
		return this.cronManager.getAll().some(c =>
			c.id.toUpperCase() === code.toUpperCase() && c.cronId !== currentCronId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.showError();
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.showError(`A cron with code "${code}" already exists`);
		}

		const {description} = this.collectTranslations();
		this.dialogRef.close({
			...this.form.value,
			id: code,
			description
		});
	}
}
