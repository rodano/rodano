import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';
import {MatIconModule} from '@angular/material/icon';
import {MatTabsModule} from '@angular/material/tabs';
import {FormsModule} from '@angular/forms';
import {ProjectLanguage} from '@core/model/project-language';
import {WorkflowWidgetColumnConfig} from '@core/model/workflow-widget-column-config';
import {LanguageService} from '../../../services/language.service';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface WorkflowWidgetColumnDialogData {
	columns: WorkflowWidgetColumnConfig[];
	languages: ProjectLanguage[];
	workflowEntity: string;
}

@Component({
	selector: 'app-workflow-widget-column-dialog',
	standalone: true,
	templateUrl: './workflow-widget-column-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, MatSelectModule, MatTabsModule, FormsModule]
})
export class WorkflowWidgetColumnDialogComponent
	extends BaseDialogComponent<WorkflowWidgetColumnDialogData>
	implements OnInit {
	columns: WorkflowWidgetColumnConfig[] = [];
	availableLanguages: ProjectLanguage[] = [];

	private static readonly ALL_COLUMN_TYPES = [
		{value: 'WORKFLOW_LABEL', label: 'Workflow label'},
		{value: 'WORKFLOW_TRIGGER_MESSAGE', label: 'Workflow trigger message'},
		{value: 'STATUS_LABEL', label: 'Status label'},
		{value: 'STATUS_DATE', label: 'Status date'},
		{value: 'PARENT_SCOPE_CODE', label: 'Parent scope code'},
		{value: 'SCOPE_CODE', label: 'Scope code'},
		{value: 'EVENT_LABEL', label: 'Event label'},
		{value: 'EVENT_DATE', label: 'Event date'},
		{value: 'FORM_LABEL', label: 'Form label'},
		{value: 'FORM_DATE', label: 'Form date'},
		{value: 'FIELD_LABEL', label: 'Field label'},
		{value: 'FIELD_DATE', label: 'Field date'}
	];

	private static readonly ENTITY_TYPE_COUNTS: Record<string, number> = {
		SCOPE: 6,
		EVENT: 8,
		FORM: 10,
		FIELD: 12
	};

	constructor(
		public languageService: LanguageService,
		dialogRef: MatDialogRef<WorkflowWidgetColumnDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: WorkflowWidgetColumnDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.columns = JSON.parse(JSON.stringify(this.data.columns ?? []));
		this.availableLanguages = this.data.languages?.length
			? this.data.languages
			: [{languageCode: 'en', isDefault: true}];
	}

	get columnTypes(): {value: string; label: string}[] {
		const count = WorkflowWidgetColumnDialogComponent.ENTITY_TYPE_COUNTS[
			this.data.workflowEntity?.toUpperCase()
		] ?? 6;
		return WorkflowWidgetColumnDialogComponent.ALL_COLUMN_TYPES.slice(0, count);
	}

	getLanguageLabel(lang: ProjectLanguage): string {
		return this.languageService.getLanguageName(lang.languageCode) + (lang.isDefault ? ' ☆' : '');
	}

	addColumn(): void {
		this.columns = [...this.columns, {
			workflowWidgetColumnId: crypto.randomUUID(),
			id: '',
			shortname: {},
			longname: {},
			description: {},
			type: '',
			width: undefined,
			sortOrder: this.columns.length
		}];
	}

	removeColumn(index: number): void {
		this.columns = this.columns.filter((_, i) => i !== index);
		this.reindexSortOrders();
	}

	moveUp(index: number): void {
		if(index === 0) {
			return;
		}
		const cols = [...this.columns];
		[cols[index - 1], cols[index]] = [cols[index], cols[index - 1]];
		this.columns = cols;
		this.reindexSortOrders();
	}

	moveDown(index: number): void {
		if(index === this.columns.length - 1) {
			return;
		}
		const cols = [...this.columns];
		[cols[index], cols[index + 1]] = [cols[index + 1], cols[index]];
		this.columns = cols;
		this.reindexSortOrders();
	}

	private reindexSortOrders(): void {
		this.columns.forEach((col, i) => col.sortOrder = i);
	}

	updateTranslation(index: number, field: 'shortname' | 'longname' | 'description', langCode: string, value: string): void {
		this.columns[index] = {
			...this.columns[index],
			[field]: {...(this.columns[index][field] as Record<string, string> ?? {}), [langCode]: value}
		};
	}

	updateField(index: number, field: keyof WorkflowWidgetColumnConfig, value: any): void {
		this.columns[index] = {...this.columns[index], [field]: value};
	}

	getTypeLabel(type: string): string {
		return this.columnTypes.find(t => t.value === type)?.label ?? type;
	}

	isValid(): boolean {
		const defaultLang = this.availableLanguages.find(l => l.isDefault)?.languageCode ?? this.availableLanguages[0]?.languageCode;
		return this.columns.every(col => !!col.shortname[defaultLang!]?.trim());
	}

	onSave(): void {
		this.dialogRef.close(this.columns);
	}
}
