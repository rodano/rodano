import {ProjectLanguage} from '@core/model/project-language';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';
import {LanguageService} from '../../../services/language.service';
import {TimelineGraph} from '@core/model/timeline-graph';
import {TimelineGraphManagerService} from '../../../services/manager/timeline-graph-manager.service';
import {MatSelectModule} from '@angular/material/select';
import {ScopeModel} from '@core/model/scope-model';

export interface TimelineGraphBasicInfoDialogData {
	projectId: string;
	timelineGraph: TimelineGraph | null;
	languages: ProjectLanguage[];
	scopeModels: ScopeModel[];
}

@Component({
	selector: 'app-timeline-graph-basic-info-dialog',
	standalone: true,
	templateUrl: './timeline-graph-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatTabsModule, MatCheckboxModule, MatSelectModule]
})
export class TimelineGraphBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	scopeModels: ScopeModel[] = [];
	isEditMode: boolean;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<TimelineGraphBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: TimelineGraphBasicInfoDialogData,
		private timelineGraphManager: TimelineGraphManagerService,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
		this.isEditMode = !!data.timelineGraph;
		this.scopeModels = data.scopeModels || [];
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
			const tg = this.data.timelineGraph;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [tg?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [tg?.longname?.[lang.languageCode] || ''],
				description: [tg?.description?.[lang.languageCode] || ''],
				footnote: [tg?.footnote?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		const tg = this.data.timelineGraph;
		this.form = this.fb.group({
			id: [tg?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			scopeModelId: [tg?.scopeModelId || null]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentTimelineGraphId = this.data.timelineGraph?.timelineGraphId;
		return this.timelineGraphManager.getAll().some(tg =>
			tg.id.toUpperCase() === code.toUpperCase() && tg.timelineGraphId !== currentTimelineGraphId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.showError();
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.showError(`A timeline graph with code "${code}" already exists`);
			return;
		}

		const {shortname, longname, description} = this.collectTranslations();
		const footnote: Record<string, string> = {};

		this.languageForms.forEach((langForm, langCode) => {
			const v = langForm.value;
			if(v.footnote) {
				footnote[langCode] = v.footnote;
			}
		});

		this.dialogRef.close({
			...this.form.value,
			id: code,
			shortname,
			longname,
			description,
			footnote
		});
	}
}
