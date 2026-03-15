import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatTabsModule} from '@angular/material/tabs';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {FormGroup, FormsModule} from '@angular/forms';
import {ProjectLanguage} from '@core/model/project-language';
import {WorkflowState} from '@core/model/workflow-state';
import {LanguageService} from '../../../services/language.service';
import {BaseDialogComponent} from '../../base-dialog.component';
import {DualListBoxComponent} from '../../dual-list-box/dual-list-box.component';
import {WorkflowSummaryColumn} from '@core/model/workflow-summary-column';

export interface WorkflowSummaryColumnDialogData {
	columns: WorkflowSummaryColumn[];
	languages: ProjectLanguage[];
	availableStates: WorkflowState[];
}

@Component({
	selector: 'app-workflow-summary-column-dialog',
	standalone: true,
	templateUrl: './workflow-summary-column-dialog.component.html',
	styleUrls: ['./workflow-summary-column-dialog.component.css'],
	imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, MatTabsModule, MatCheckboxModule, FormsModule, DualListBoxComponent]
})
export class WorkflowSummaryColumnDialogComponent
	extends BaseDialogComponent<WorkflowSummaryColumnDialogData>
	implements OnInit {
	form: FormGroup;
	columns: WorkflowSummaryColumn[] = [];
	availableLanguages: ProjectLanguage[] = [];

	columnStateLists: {available: WorkflowState[]; selected: WorkflowState[]}[] = [];

	constructor(
		public languageService: LanguageService,
		dialogRef: MatDialogRef<WorkflowSummaryColumnDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: WorkflowSummaryColumnDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.columns = JSON.parse(JSON.stringify(this.data.columns ?? []));
		this.availableLanguages = this.data.languages?.length
			? this.data.languages
			: [{languageCode: 'en', isDefault: true}];

		this.columnStateLists = this.columns.map(col => {
			const selectedIds = col.workflowStateIds ?? [];
			return {
				selected: this.data.availableStates.filter(s => selectedIds.includes(s.workflowStateId)),
				available: this.data.availableStates.filter(s => !selectedIds.includes(s.workflowStateId))
			};
		});
	}

	getLanguageLabel(lang: ProjectLanguage): string {
		return this.languageService.getLanguageName(lang.languageCode) + (lang.isDefault ? ' ☆' : '');
	}

	addColumn(): void {
		this.columns = [...this.columns, {
			summaryColumnId: crypto.randomUUID(),
			label: {},
			description: {},
			total: false,
			percent: false,
			nonNullColor: undefined,
			nonNullBgColor: undefined,
			workflowStateIds: [],
			sortOrder: this.columns.length
		}];
		this.columnStateLists = [...this.columnStateLists, {
			available: [...this.data.availableStates],
			selected: []
		}];
	}

	removeColumn(index: number): void {
		this.columns = this.columns.filter((_, i) => i !== index);
		this.columnStateLists = this.columnStateLists.filter((_, i) => i !== index);
		this.reindexSortOrders();
	}

	moveUp(index: number): void {
		if(index === 0) {
			return;
		}
		const cols = [...this.columns];
		const lists = [...this.columnStateLists];
		[cols[index - 1], cols[index]] = [cols[index], cols[index - 1]];
		[lists[index - 1], lists[index]] = [lists[index], lists[index - 1]];
		this.columns = cols;
		this.columnStateLists = lists;
		this.reindexSortOrders();
	}

	moveDown(index: number): void {
		if(index === this.columns.length - 1) {
			return;
		}
		const cols = [...this.columns];
		const lists = [...this.columnStateLists];
		[cols[index], cols[index + 1]] = [cols[index + 1], cols[index]];
		[lists[index], lists[index + 1]] = [lists[index + 1], lists[index]];
		this.columns = cols;
		this.columnStateLists = lists;
		this.reindexSortOrders();
	}

	private reindexSortOrders(): void {
		this.columns.forEach((col, i) => col.sortOrder = i);
	}

	updateTranslation(index: number, field: 'label' | 'description', langCode: string, value: string): void {
		this.columns[index] = {
			...this.columns[index],
			[field]: {...(this.columns[index][field] as Record<string, string> ?? {}), [langCode]: value}
		};
	}

	updateField(index: number, field: keyof WorkflowSummaryColumn, value: any): void {
		this.columns[index] = {...this.columns[index], [field]: value};
	}

	onAddState(index: number, state: WorkflowState): void {
		const list = this.columnStateLists[index];
		list.available = list.available.filter(s => s.workflowStateId !== state.workflowStateId);
		list.selected = [...list.selected, state];
		this.columns[index] = {...this.columns[index], workflowStateIds: list.selected.map(s => s.workflowStateId)};
	}

	onRemoveState(index: number, state: WorkflowState): void {
		const list = this.columnStateLists[index];
		list.selected = list.selected.filter(s => s.workflowStateId !== state.workflowStateId);
		list.available = [...list.available, state];
		this.columns[index] = {...this.columns[index], workflowStateIds: list.selected.map(s => s.workflowStateId)};
	}

	isValid(): boolean {
		const defaultLang = this.availableLanguages.find(l => l.isDefault)?.languageCode ?? this.availableLanguages[0]?.languageCode;
		return this.columns.every(col => !!col.label[defaultLang!]?.trim());
	}

	onSave(): void {
		this.dialogRef.close(this.columns);
	}
}
