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
import {TimelineGraphSection} from '@core/model/timeline-graph-section';
import {TimelineGraphSectionManagerService} from '../../../services/manager/timeline-graph-section-manager.service';

export interface TimelineGraphSectionBasicInfoDialogData {
	projectId: string;
	timelineGraphId: string;
	section: TimelineGraphSection | null;
	languages: ProjectLanguage[];
}

interface TypeOption {
	value: string;
	label: string;
}

@Component({
	selector: 'app-timeline-graph-section-basic-info-dialog',
	standalone: true,
	templateUrl: './timeline-graph-section-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule,
		MatIconModule, MatCheckboxModule, MatTabsModule, MatSelectModule]
})
export class TimelineGraphSectionBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode: boolean;

	typeOptions: TypeOption[] = [
		{value: 'LINE', label: 'Line'},
		{value: 'DOT', label: 'Dot'},
		{value: 'BAR', label: 'Bar'},
		{value: 'PERIOD', label: 'Period'},
		{value: 'ACTION', label: 'Action'},
		{value: 'DATE', label: 'Date'}
	];

	typeHints: Record<string, string> = {
		DATE: '"Date" displays a symbol at the specified date.',
		ACTION: '"Action" displays a vertical line at the specified date.',
		PERIOD: '"Period" displays a horizontal rectangle that begins and ends at the specified dates.',
		DOT: '"Dot", used with a scale, displays a symbol at the specified date and at the correct vertical position.',
		LINE: '"Line" is the same as "Dot" but links all dots with a line.',
		BAR: '"Bar" displays a vertical rectangle at the specified date and with the specified value.'
	};

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<TimelineGraphSectionBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: TimelineGraphSectionBasicInfoDialogData,
		private timelineGraphSectionManager: TimelineGraphSectionManagerService,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
		this.isEditMode = !!data.section;
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
			const tgs = this.data.section;
			this.languageForms.set(lang.languageCode, this.fb.group({
				label: [tgs?.label?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []]
			}));
		});
	}

	initializeForm(): void {
		const tgs = this.data.section;
		this.form = this.fb.group({
			id: [tgs?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			type: [tgs?.type || null, Validators.required]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentSectionId = this.data.section?.graphSectionId;
		return this.timelineGraphSectionManager.getAll().some(tgs =>
			tgs.id.toUpperCase() === code.toUpperCase() && tgs.graphSectionId !== currentSectionId
		);
	}

	get selectedTypeHint(): string | null {
		const type = this.form.get('type')?.value;
		return type ? this.typeHints[type] : null;
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.showError();
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.showError(`An timeline graph section with code "${code}" already exists`);
			return;
		}

		const label: Record<string, string> = {};

		this.languageForms.forEach((langForm, langCode) => {
			const v = langForm.value;
			if(v.label) {
				label[langCode] = v.label;
			}
		});

		this.dialogRef.close({
			...this.form.value,
			id: code,
			label
		});
	}
}
