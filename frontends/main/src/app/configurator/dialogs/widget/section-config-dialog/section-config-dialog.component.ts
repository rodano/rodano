import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';
import {LanguageService} from '../../../services/language.service';
import {ProjectLanguage} from '@core/model/project-language';
import {Section} from '@core/model/section';
import {Feature} from '@core/model/feature';
import {MatSelectModule} from '@angular/material/select';
import {ScopeModel} from '@core/model/scope-model';
import {EventModel} from '@core/model/event-model';
import {DatasetModel} from '@core/model/dataset-model';
import {FormModel} from '@core/model/form-model';
import {Profile} from '@core/model/profile';

export interface SectionConfigDialogData {
	section: Section;
	languages: ProjectLanguage[];
	features: Feature[];
	scopeModels: ScopeModel[];
	eventModels: EventModel[];
	datasetModels: DatasetModel[];
	formModels: FormModel[];
	profiles: Profile[];
}

@Component({
	selector: 'app-section-config-dialog',
	standalone: true,
	templateUrl: './section-config-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatIconModule, MatInputModule,
		MatTabsModule, MatSelectModule]
})
export class SectionConfigDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	targetOptions: {id: string; label: string}[] = [];

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<SectionConfigDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: SectionConfigDialogData,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
	}

	ngOnInit(): void {
		this.loadProjectLanguages(this.data.languages);
		this.form = this.fb.group({
			id: [this.data.section.id ?? '', Validators.required],
			requiredFeatureId: [this.data.section.requiredFeatureId ?? ''],
			rightEntity: [this.data.section.rightEntity ?? ''],
			rightValue: [this.data.section.rightValue ?? ''],
			rightTargetId: [this.data.section.rightTargetId ?? '']
		});

		if(this.data.section.rightEntity) {
			this.updateTargetOptions(this.data.section.rightEntity);
		}

		this.form.get('rightEntity')!.valueChanges.subscribe(entity => {
			this.form.get('rightTargetId')!.setValue('');
			this.updateTargetOptions(entity);
		});
	}

	get availableFeatures(): Feature[] {
		return this.data.features ?? [];
	}

	updateTargetOptions(entity: string): void {
		switch(entity) {
			case 'SCOPE_MODEL':
				this.targetOptions = (this.data.scopeModels ?? []).map((s: ScopeModel) => ({
					id: s.scopeModelId,
					label: this.languageService.getLabel(s)
				}));
				break;
			case 'EVENT_MODEL':
				this.targetOptions = (this.data.eventModels ?? []).map((e: EventModel) => ({
					id: e.eventModelId,
					label: this.languageService.getLabel(e)
				}));
				break;
			case 'DATASET_MODEL':
				this.targetOptions = (this.data.datasetModels ?? []).map((d: DatasetModel) => ({
					id: d.datasetModelId,
					label: this.languageService.getLabel(d)
				}));
				break;
			case 'FORM_MODEL':
				this.targetOptions = (this.data.formModels ?? []).map((f: FormModel) => ({
					id: f.formModelId,
					label: this.languageService.getLabel(f)
				}));
				break;
			case 'PROFILE':
				this.targetOptions = (this.data.profiles ?? []).map((p: Profile) => ({
					id: p.profileId,
					label: this.languageService.getLabel(p)
				}));
				break;
			default:
				this.targetOptions = [];
		}
	}

	initializeLanguageForms(): void {
		this.availableLanguages.forEach(lang => {
			if(!lang.languageCode) {
				return;
			}
			this.languageForms.set(lang.languageCode, this.fb.group({
				label: [
					this.data.section.label?.[lang.languageCode] ?? '',
					lang.isDefault ? Validators.required : []
				]
			}));
		});
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.showError();
			return;
		}
		const label: Record<string, string> = {};
		this.languageForms.forEach((form, lang) => {
			const value = form.getRawValue().label;
			if(value) {
				label[lang] = value;
			}
		});
		const v = this.form.getRawValue();
		this.dialogRef.close({
			id: v.id,
			label,
			requiredFeatureId: v.requiredFeatureId || null,
			rightEntity: v.rightEntity || null,
			rightValue: v.rightValue || null,
			rightTargetId: v.rightTargetId || null
		});
	}
}
