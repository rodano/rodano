import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatTabsModule} from '@angular/material/tabs';
import {ProjectLanguage} from '@core/model/project-language';
import {LanguageService} from '../../../services/language.service';
import {BaseDialogComponent} from '../../base-dialog.component';
import {TimelineGraphSection} from '@core/model/timeline-graph-section';
import {TimelineGraphSectionReference} from '@core/model/timeline-graph-section-reference';
import {TimelineGraphSectionReferenceEntry} from '@core/model/timeline-graph-section-reference-entry';
import {MatSelectModule} from '@angular/material/select';

export interface TimelineGraphSectionReferencesDialogData {
	section: TimelineGraphSection;
	allSections: TimelineGraphSection[];
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-timeline-graph-section-references-dialog',
	standalone: true,
	templateUrl: './timeline-graph-section-references-dialog.component.html',
	styleUrls: ['../../dialog-shared.css', '../../chart/chart-statistics-dialog/chart-statistics-dialog.component.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatIconModule, MatCheckboxModule,
		MatTabsModule, MatSelectModule]
})
export class TimelineGraphSectionReferencesDialogComponent extends BaseDialogComponent<TimelineGraphSectionReferencesDialogData> implements OnInit {
	references: TimelineGraphSectionReference[] = [];
	selectedLanguage = '';
	availableLanguages: ProjectLanguage[] = [];

	constructor(
		private fb: FormBuilder,
		public languageService: LanguageService,
		dialogRef: MatDialogRef<TimelineGraphSectionReferencesDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: TimelineGraphSectionReferencesDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.availableLanguages = this.data.languages?.length
			? this.data.languages
			: [{languageCode: 'en', isDefault: true}];

		this.selectedLanguage = this.availableLanguages.find(l => l.isDefault)?.languageCode ?? this.availableLanguages[0].languageCode!;

		this.references = this.data.section.references
			? JSON.parse(JSON.stringify(this.data.section.references))
			: [];
	}

	getLanguageLabel(lang: ProjectLanguage): string {
		return this.languageService.getLanguageName(lang.languageCode) + (lang.isDefault ? ' ☆' : '');
	}

	onLanguageChange(index: number): void {
		this.selectedLanguage = this.availableLanguages[index]?.languageCode || this.selectedLanguage;
	}

	parseDecimal(value: string): number | undefined {
		if(value === '' || value === null) {
			return undefined;
		}
		if(value === '-' || value.endsWith('.')) {
			return undefined;
		}
		const parsed = parseFloat(value);
		return isNaN(parsed) ? undefined : parsed;
	}

	updateReferenceTranslation(refIndex: number, field: 'label' | 'tooltip', langCode: string, value: string): void {
		const updated = {...(this.references[refIndex][field] as Record<string, string> || {}), [langCode]: value};
		this.updateReferenceField(refIndex, field, updated);
	}

	addReference(): void {
		this.references = [...this.references, {
			graphReferenceId: crypto.randomUUID(),
			color: '',
			dashed: false,
			referenceSectionId: undefined,
			label: {},
			tooltip: {},
			entries: []
		}];
	}

	removeReference(index: number): void {
		this.references = this.references.filter((_, i) => i !== index);
	}

	updateReferenceField(index: number, field: keyof TimelineGraphSectionReference, value: any): void {
		this.references = this.references.map((r, i) =>
			i === index ? {...r, [field]: value} : r
		);
	}

	onColorInput(event: Event, index: number): void {
		const value = (event.target as HTMLInputElement).value;
		if(/^#[0-9A-F]{6}$/i.test(value)) {
			this.updateReferenceField(index, 'color', value);
		}
	}

	addEntry(refIndex: number): void {
		const ref = this.references[refIndex];
		const newEntry: TimelineGraphSectionReferenceEntry = {
			timepoint: '',
			value: undefined,
			label: '',
			sortOrder: ref.entries?.length ?? 0
		};
		this.updateReferenceField(refIndex, 'entries', [...(ref.entries || []), newEntry]);
	}

	removeEntry(refIndex: number, entryIndex: number): void {
		const entries = this.references[refIndex].entries?.filter((_, i) => i !== entryIndex) || [];
		this.updateReferenceField(refIndex, 'entries', entries);
	}

	updateEntryField(refIndex: number, entryIndex: number, field: keyof TimelineGraphSectionReferenceEntry, value: any): void {
		const entries = (this.references[refIndex].entries || []).map((e, i) =>
			i === entryIndex ? {...e, [field]: value} : e
		);
		this.updateReferenceField(refIndex, 'entries', entries);
	}

	onSave(): void {
		const normalized = this.references.map(r => ({
			...r,
			entries: (r.entries || []).map((e, j) => ({...e, sortOrder: j}))
		}));
		this.dialogRef.close(normalized);
	}

	protected readonly isNaN = isNaN;
}
