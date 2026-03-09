import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {Layout} from '@core/model/layout';
import {Cell} from '@core/model/cell';
import {FieldModel} from '@core/model/field-model';
import {VisibilityCriteria} from '@core/model/visibility-criteria';
import {Operator} from '@core/model/operator';
import {LanguageService} from '../../services/language.service';
import {FieldModelManagerService} from '../../services/manager/field-model-manager.service';
import {FormLayoutManagerService} from '../../services/manager/form-layout-manager.service';
import {LayoutType} from '@core/model/layout-type';

@Component({
	selector: 'app-form-layout-preview',
	standalone: true,
	templateUrl: './form-layout-preview.component.html',
	styleUrls: ['./form-layout-preview.component.css'],
	imports: [CommonModule, FormsModule, MatIconModule, MatTooltipModule]
})
export class FormLayoutPreviewComponent implements OnInit, OnChanges {
	@Input() projectId = '';
	@Input() formModelId = '';
	@Output() closed = new EventEmitter<void>();

	private hiddenLayoutIds = new Set<string>();
	private hiddenCellIds = new Set<string>();

	values = new Map<string, string | string[]>();
	applyVisibilityRules = false;

	readonly rulerTicks = [0, 250, 500, 750, 1000, 1250];
	readonly MONTH_NAMES = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
		'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

	multipleLayoutRowIds = new Map<string, string[]>();

	private readonly AUTO_COLUMN_WIDTH = 250;

	constructor(
		public languageService: LanguageService,
		public formLayoutManager: FormLayoutManagerService,
		public fieldModelManager: FieldModelManagerService
	) {}

	ngOnInit(): void {
		this.updateVisibility();
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['formModelId'] && this.formModelId) {
			this.values.clear();
			this.multipleLayoutRowIds.clear();
			this.applyVisibilityRules = false;
			this.updateVisibility();
		}
	}

	get layouts(): Layout[] {
		return this.formLayoutManager.getAll();
	}

	toggleVisibility(): void {
		this.applyVisibilityRules = !this.applyVisibilityRules;
		this.updateVisibility();
	}

	private updateVisibility(): void {
		const hiddenLayoutIds = new Set<string>();
		const hiddenCellIds = new Set<string>();

		if(this.applyVisibilityRules) {
			for(const layout of this.layouts) {
				for(const line of layout.lines) {
					for(const cell of line.cells) {
						if(this.isSpacerCell(cell) || this.isTextCell(cell)) {
							continue;
						}
						const fieldValue = this.values.get(cell.id);
						for(const criterion of (cell.visibilityCriteria || [])) {
							const met = this.evaluateCriterion(criterion, fieldValue);
							if(criterion.action === 'SHOW' && !met) {
								criterion.targetLayoutIds?.forEach(id => hiddenLayoutIds.add(id));
								criterion.targetCellIds?.forEach(id => hiddenCellIds.add(id));
							}
							if(criterion.action === 'HIDE' && met) {
								criterion.targetLayoutIds?.forEach(id => hiddenLayoutIds.add(id));
								criterion.targetCellIds?.forEach(id => hiddenCellIds.add(id));
							}
						}
					}
				}
			}
		}

		this.hiddenLayoutIds = hiddenLayoutIds;
		this.hiddenCellIds = hiddenCellIds;
	}

	isMultipleLayout(layout: Layout): boolean {
		return layout.type === LayoutType.MULTIPLE;
	}

	getRowIds(layoutId: string): string[] {
		if(!this.multipleLayoutRowIds.has(layoutId)) {
			this.multipleLayoutRowIds.set(layoutId, [crypto.randomUUID()]);
		}
		return this.multipleLayoutRowIds.get(layoutId)!;
	}

	addRow(layoutId: string): void {
		this.getRowIds(layoutId).push(crypto.randomUUID());
	}

	removeRow(layoutId: string, rowId: string): void {
		const rows = this.getRowIds(layoutId);
		if(rows.length <= 1) {
			return;
		}
		const idx = rows.indexOf(rowId);
		if(idx !== -1) {
			rows.splice(idx, 1);
		}
		for(const key of this.values.keys()) {
			if(key.endsWith(`__${rowId}`)) {
				this.values.delete(key);
			}
		}
		this.updateVisibility();
	}

	getRowKey(cellId: string, rowId: string): string {
		return `${cellId}__${rowId}`;
	}

	getRowValue(cellId: string, rowId: string): string {
		const v = this.values.get(this.getRowKey(cellId, rowId));
		return Array.isArray(v) ? '' : (v ?? '');
	}

	getRowArrayValue(cellId: string, rowId: string): string[] {
		const v = this.values.get(this.getRowKey(cellId, rowId));
		return Array.isArray(v) ? v : [];
	}

	onRowValueChange(cellId: string, rowId: string, value: string): void {
		this.values.set(this.getRowKey(cellId, rowId), value);
		this.updateVisibility();
	}

	onRowCheckboxChange(cellId: string, rowId: string, checked: boolean): void {
		this.values.set(this.getRowKey(cellId, rowId), checked ? 'true' : '');
		this.updateVisibility();
	}

	isRowCheckboxChecked(cellId: string, rowId: string): boolean {
		return this.getRowValue(cellId, rowId) === 'true';
	}

	onRowCheckboxGroupChange(cellId: string, rowId: string, pvId: string, checked: boolean): void {
		const key = this.getRowKey(cellId, rowId);
		const current = this.getRowArrayValue(cellId, rowId);
		this.values.set(key, checked ? [...current, pvId] : current.filter(v => v !== pvId));
		this.updateVisibility();
	}

	isRowCheckboxGroupChecked(cellId: string, rowId: string, pvId: string): boolean {
		return this.getRowArrayValue(cellId, rowId).includes(pvId);
	}

	getMultipleTableWidth(): string {
		const max = this.getMaxLayoutWidth();
		return max > 0 ? `${max + 34}px` : 'auto';
	}

	getMultipleCells(layout: Layout): Cell[] {
		const cells: Cell[] = [];
		for(const line of layout.lines) {
			for(const cell of line.cells) {
				if(!this.isSpacerCell(cell) && !this.isTextCell(cell)) {
					cells.push(cell);
				}
			}
		}
		return cells;
	}

	getAddRowLabel(layout: Layout): string {
		return this.getLabel(layout.datasetModel?.shortname as any) || 'Row';
	}

	private evaluateCriterion(criterion: VisibilityCriteria, value: string | string[] | undefined): boolean {
		const criterionValues = criterion.values ?? [];
		const isEmpty = value === undefined || value === null || value === '' || (Array.isArray(value) && value.length === 0);
		const strValue = Array.isArray(value) ? '' : (value ?? '');

		if(criterion.operator === null || criterion.operator === undefined) {
			if(criterionValues.length === 0) {
				return true;
			}
			return Array.isArray(value)
				? criterionValues.some(cv => (value as string[]).includes(cv))
				: criterionValues.includes(strValue);
		}

		switch(criterion.operator) {
			case Operator.EQUALS:
				return strValue === criterionValues[0];
			case Operator.NOT_EQUALS:
				return strValue !== criterionValues[0];
			case Operator.CONTAINS:
				return Array.isArray(value)
					? criterionValues.some(cv => (value as string[]).includes(cv))
					: criterionValues.includes(strValue);
			case Operator.NOT_CONTAINS:
				return Array.isArray(value)
					? !criterionValues.some(cv => (value as string[]).includes(cv))
					: !criterionValues.includes(strValue);
			case Operator.GREATER:
				return parseFloat(strValue) > parseFloat(criterionValues[0]);
			case Operator.GREATER_EQUALS:
				return parseFloat(strValue) >= parseFloat(criterionValues[0]);
			case Operator.LOWER:
				return parseFloat(strValue) < parseFloat(criterionValues[0]);
			case Operator.LOWER_EQUALS:
				return parseFloat(strValue) <= parseFloat(criterionValues[0]);
			case Operator.NULL:
				return isEmpty;
			case Operator.NOT_NULL:
				return !isEmpty;
			case Operator.BLANK:
				return strValue.trim() === '';
			case Operator.NOT_BLANK:
				return strValue.trim() !== '';
			default:
				return false;
		}
	}

	isLayoutVisible(layoutId: string): boolean {
		return !this.hiddenLayoutIds.has(layoutId);
	}

	isCellVisible(cell: Cell): boolean {
		return !this.hiddenCellIds.has(cell.formLayoutCellId);
	}

	getValue(cellId: string): string {
		const v = this.values.get(cellId);
		return Array.isArray(v) ? '' : (v ?? '');
	}

	getArrayValue(cellId: string): string[] {
		const v = this.values.get(cellId);
		return Array.isArray(v) ? v : [];
	}

	onValueChange(cellId: string, value: string): void {
		this.values.set(cellId, value);
		this.updateVisibility();
	}

	onCheckboxGroupChange(cellId: string, pvId: string, checked: boolean): void {
		const current = this.getArrayValue(cellId);
		this.values.set(cellId, checked ? [...current, pvId] : current.filter(v => v !== pvId));
		this.updateVisibility();
	}

	isCheckboxGroupChecked(cellId: string, pvId: string): boolean {
		return this.getArrayValue(cellId).includes(pvId);
	}

	onCheckboxChange(cellId: string, checked: boolean): void {
		this.values.set(cellId, checked ? 'true' : '');
		this.updateVisibility();
	}

	isCheckboxChecked(cellId: string): boolean {
		return this.getValue(cellId) === 'true';
	}

	resetValues(): void {
		this.values.clear();
		this.updateVisibility();
	}

	getFieldModel(fieldModelId: string): FieldModel | undefined {
		return this.fieldModelManager.getById(fieldModelId);
	}

	getLabel(translations: Record<string, string> | undefined): string {
		if(!translations) {
			return '';
		}
		return this.languageService.getTranslatedName(translations) || '';
	}

	getFieldLabel(cell: Cell): string {
		const fm = this.getFieldModel(cell.fieldModelId ?? '');
		if(!fm) {
			return cell.id || '';
		}
		return this.getLabel(fm.shortname) || fm.id;
	}

	isSpacerCell(cell: Cell): boolean {
		return cell.fieldModelId === '__SPACER__';
	}

	isTextCell(cell: Cell): boolean {
		return !cell.fieldModelId || cell.fieldModelId === '';
	}

	getTextContent(cell: Cell): string {
		return this.getLabel(cell.textBefore as any) || '';
	}

	parsePx(css: string | undefined): number | null {
		const match = css?.match(/width:\s*(\d+)px/);
		return match ? parseInt(match[1]) : null;
	}

	getLabelStyle(cell: Cell): string {
		const labelPx = this.parsePx(cell.cssCodeForLabel);
		if(labelPx === null) {
			return 'flex-shrink: 0';
		}
		return `width: ${labelPx}px; flex-shrink: 0`;
	}

	getInputStyle(cell: Cell): string {
		const inputPx = this.parsePx(cell.cssCodeForInput);
		if(inputPx === null) {
			return 'flex: 1; min-width: 0; overflow: hidden';
		}
		return `width: ${inputPx}px; max-width: 100%; min-width: 0; flex-shrink: 1; overflow: hidden`;
	}

	getGridTemplateColumns(layout: Layout): string {
		return layout.columns.map(col => {
			const match = col.cssCode?.match(/width:\s*(\d+)px/);
			return match ? `${match[1]}px` : '1fr';
		}).join(' ');
	}

	getCellSpan(cell: Cell): number {
		return cell.colspan ?? 1;
	}

	getColumnWidthPx(col: {cssCode?: string}): number {
		const match = col.cssCode?.match(/width:\s*(\d+)px/);
		return match ? parseInt(match[1]) : this.AUTO_COLUMN_WIDTH;
	}

	getLayoutTotalWidth(layout: Layout): number {
		return layout.columns.reduce((sum, col) => sum + this.getColumnWidthPx(col), 0);
	}

	getMaxLayoutWidth(): number {
		return Math.max(...this.layouts.map(l => this.getLayoutTotalWidth(l)), 0);
	}

	getLayoutWidth(): string {
		const max = this.getMaxLayoutWidth();
		return max > 0 ? `${max}px` : 'auto';
	}

	getDateParts(fm: FieldModel): string[] {
		const parts: string[] = [];
		if(fm.withYears) {
			parts.push('year');
		}
		if(fm.withMonths) {
			parts.push('month');
		}
		if(fm.withDays) {
			parts.push('day');
		}
		if(fm.withHours) {
			parts.push('hour');
		}
		if(fm.withMinutes) {
			parts.push('minute');
		}
		if(fm.withSeconds) {
			parts.push('second');
		}
		return parts;
	}

	getDatePartValue(cellId: string, part: string): string {
		const raw = this.getValue(cellId);
		if(!raw) {
			return '';
		}
		try {
			return JSON.parse(raw)[part] ?? '';
		}
		catch {
			return '';
		}
	}

	onDatePartChange(cellId: string, part: string, value: string): void {
		const raw = this.getValue(cellId);
		let obj;
		try {
			obj = raw ? JSON.parse(raw) : {};
		}
		catch {
			obj = {};
		}
		obj[part] = value;
		this.values.set(cellId, JSON.stringify(obj));
		this.updateVisibility();
	}

	getDatePartLabel(part: string): string {
		return ({year: 'YYYY', month: 'MM', day: 'DD', hour: 'HH', minute: 'mm', second: 'ss'} as any)[part] ?? part;
	}

	getDatePartMax(part: string): number {
		return ({year: 9999, month: 12, day: 31, hour: 23, minute: 59, second: 59} as any)[part] ?? 9999;
	}

	getYearOptions(fm: FieldModel): number[] {
		const currentYear = new Date().getFullYear();
		const min = fm.minYear !== null && fm.minYear !== undefined ? +fm.minYear : currentYear - 10;
		const options: number[] = [];
		for(let y = currentYear; y >= min; y--) {
			options.push(y);
		}
		return options;
	}

	getFullSpan(layout: Layout): number {
		return layout.columns.length;
	}

	isTextLine(line: {cells: Cell[]}): boolean {
		return line.cells.every(c => this.isTextCell(c));
	}

	getTextBefore(cell: Cell): string {
		return this.getLabel(cell.textBefore as any) || '';
	}

	getTextAfter(cell: Cell): string {
		return this.getLabel(cell.textAfter as any) || '';
	}

	onClose(): void {
		this.closed.emit();
	}
}
