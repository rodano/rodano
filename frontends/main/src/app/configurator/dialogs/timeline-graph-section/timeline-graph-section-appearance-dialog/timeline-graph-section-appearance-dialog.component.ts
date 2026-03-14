import {Component, ElementRef, Inject, OnInit, QueryList, ViewChildren} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatSelectModule} from '@angular/material/select';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {ProjectLanguage} from '@core/model/project-language';
import {LanguageService} from '../../../services/language.service';
import {BaseDialogComponent} from '../../base-dialog.component';
import {TimelineGraphSection} from '@core/model/timeline-graph-section';
import {MatTabsModule} from '@angular/material/tabs';

export interface TimelineGraphSectionAppearanceDialogData {
	section: TimelineGraphSection;
	languages: ProjectLanguage[];
}

interface PatternOption {
	pattern: string;
	description: string;
}

interface MarkOption {
	value: string;
	label: string;
}

@Component({
	selector: 'app-timeline-graph-section-appearance-dialog',
	standalone: true,
	templateUrl: './timeline-graph-section-appearance-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatIconModule, MatSelectModule,
		MatCheckboxModule, FormsModule, MatTabsModule]
})
export class TimelineGraphSectionAppearanceDialogComponent
	extends BaseDialogComponent<TimelineGraphSectionAppearanceDialogData>
	implements OnInit {
	@ViewChildren('tooltipInput') tooltipInputs!: QueryList<ElementRef<HTMLTextAreaElement>>;

	form: FormGroup;
	tooltipForms = new Map<string, FormGroup>();
	availableLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';

	mainPatternOptions: PatternOption[] = [
		{pattern: '${section.label.en}', description: 'Section label'},
		{pattern: '${entry.label}', description: 'Entry label'},
		{pattern: '${entry.value}', description: 'Entry value'},
		{pattern: '${entry.date.toDisplay()}', description: 'Entry date'},
		{pattern: '${entry.date.toFullDisplay()}', description: 'Entry full date'},
		{pattern: '${entry.end_date.toDisplay()}', description: 'Entry end date'},
		{pattern: '${entry.toFullDisplay()}', description: 'Entry full display'},
		{pattern: '${entry.link}', description: 'Entry link'}
	];

	metadataPatternOptions: PatternOption[] = [
		{pattern: '${entry.metadata.FIELD_MODEL_ID}', description: 'Meta-data field value'}
	];

	jsPatternOptions: PatternOption[] = [
		{pattern: '${entry.label || section.label.en || ""}', description: 'Section label with or statements'},
		{pattern: '${entry.ongoing ? "-" : entry.end_date.toDisplay()}', description: 'Ongoing check example'}
	];

	markOptions: MarkOption[] = [
		{value: 'CIRCLE', label: 'Circle'},
		{value: 'SQUARE', label: 'Square'},
		{value: 'CROSS', label: 'Cross'},
		{value: 'TRIANGLE', label: 'Triangle'},
		{value: 'DIAMOND', label: 'Diamond'}
	];

	constructor(
		private fb: FormBuilder,
		public languageService: LanguageService,
		dialogRef: MatDialogRef<TimelineGraphSectionAppearanceDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: TimelineGraphSectionAppearanceDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		const s = this.data.section;

		this.availableLanguages = this.data.languages?.length
			? this.data.languages
			: [{languageCode: 'en', isDefault: true}];

		this.selectedLanguage = this.availableLanguages.find(l => l.isDefault)?.languageCode ?? this.availableLanguages[0].languageCode!;

		this.availableLanguages.forEach(lang => {
			if(!lang.languageCode) {
				return;
			}
			this.tooltipForms.set(lang.languageCode, this.fb.group({
				tooltip: [s.tooltip?.[lang.languageCode] || '']
			}));
		});

		this.form = this.fb.group({
			unit: [s.unit || ''],
			color: [s.color || ''],
			strokeColor: [s.strokeColor || ''],
			opacity: [s.opacity ?? 1],
			dashed: [s.dashed ?? false],
			mark: [s.mark || null],
			hiddenLegend: [s.hiddenLegend ?? false],
			hidden: [s.hidden ?? false]
		});
	}

	getLanguageLabel(code: string, isDefault: boolean): string {
		return this.languageService.getLanguageName(code) + (isDefault ? ' ☆' : '');
	}

	onColorInput(event: Event, controlName: 'color' | 'strokeColor'): void {
		const input = event.target as HTMLInputElement;
		const value = input.value;
		if(/^#[0-9A-F]{6}$/i.test(value)) {
			this.form.patchValue({[controlName]: value});
		}
	}

	insertPattern(pattern: string): void {
		const tooltipForm = this.tooltipForms.get(this.selectedLanguage);
		if(!tooltipForm) {
			return;
		}

		const control = tooltipForm.get('tooltip');
		const inputEl = this.tooltipInputs.find((_, i) =>
			Array.from(this.tooltipForms.keys())[i] === this.selectedLanguage
		)?.nativeElement;

		if(control && inputEl) {
			const current = control.value || '';
			const start = inputEl.selectionStart ?? current.length;
			const end = inputEl.selectionEnd ?? current.length;
			const newValue = current.substring(0, start) + pattern + current.substring(end);
			control.setValue(newValue);
			setTimeout(() => {
				inputEl.focus();
				const pos = start + pattern.length;
				inputEl.setSelectionRange(pos, pos);
			}, 0);
		}
	}

	onSave(): void {
		const s = this.data.section;
		const fv = this.form.value;
		const result: any = {};

		const normalize = (v: any) => (v === '' || v === undefined) ? null : v;

		if(normalize(fv.unit) !== normalize(s.unit)) {
			result.unit = normalize(fv.unit);
		}
		if(normalize(fv.color) !== normalize(s.color)) {
			result.color = normalize(fv.color);
		}
		if(normalize(fv.strokeColor) !== normalize(s.strokeColor)) {
			result.strokeColor = normalize(fv.strokeColor);
		}
		if(fv.opacity !== s.opacity) {
			result.opacity = fv.opacity;
		}
		if(fv.dashed !== s.dashed) {
			result.dashed = fv.dashed;
		}
		if(normalize(fv.mark) !== normalize(s.mark)) {
			result.mark = normalize(fv.mark);
		}
		if(fv.hiddenLegend !== s.hiddenLegend) {
			result.hiddenLegend = fv.hiddenLegend;
		}
		if(fv.hidden !== s.hidden) {
			result.hidden = fv.hidden;
		}

		const tooltip: Record<string, string> = {};
		this.tooltipForms.forEach((langForm, langCode) => {
			const v = langForm.value.tooltip;
			if(v) {
				tooltip[langCode] = v;
			}
		});
		if(JSON.stringify(tooltip) !== JSON.stringify(s.tooltip || {})) {
			result.tooltip = tooltip;
		}

		this.dialogRef.close(result);
	}
}
