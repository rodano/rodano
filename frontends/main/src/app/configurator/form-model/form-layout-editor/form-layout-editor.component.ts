import {ChangeDetectorRef, Component, EventEmitter, HostListener, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatSnackBar} from '@angular/material/snack-bar';
import {CdkDragDrop, DragDropModule, moveItemInArray} from '@angular/cdk/drag-drop';
import {Layout} from '@core/model/layout';
import {LayoutLine} from '@core/model/layout-line';
import {Cell} from '@core/model/cell';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {LanguageService} from '../../services/language.service';
import {FieldModelManagerService} from '../../services/manager/field-model-manager.service';
import {FieldModel} from '@core/model/field-model';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';
import {ProjectLanguage} from '@core/model/project-language';
import {MatDialog} from '@angular/material/dialog';
import {FormLayoutCellDialogComponent} from '../../dialogs/form-model/form-layout-cell-dialog/form-layout-cell-dialog.component';
import {FormLayoutCreateDialogComponent} from '../../dialogs/form-model/form-layout-create-dialog/form-layout-create-dialog.component';
import {FormLayoutService} from '../../services/api/form-layout.service';
import {FormLayoutManagerService} from '../../services/manager/form-layout-manager.service';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {switchMap, of, Observable} from 'rxjs';

@Component({
	selector: 'app-form-layout-editor',
	standalone: true,
	templateUrl: './form-layout-editor.component.html',
	styleUrls: ['./form-layout-editor.component.css'],
	imports: [CommonModule, ReactiveFormsModule, MatIconModule, MatButtonModule, MatTooltipModule, DragDropModule]
})
export class FormLayoutEditorComponent implements OnInit, OnChanges {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() formModelId = '';

	@Output() closed = new EventEmitter<void>();
	@Output() layoutCreated = new EventEmitter<Layout>();
	@Output() layoutDeleted = new EventEmitter<string>();
	@Output() unsavedChanges = new EventEmitter<boolean>();
	@Output() editLayoutConstraint = new EventEmitter<{layout: Layout}>();
	@Output() editCellConstraint = new EventEmitter<{layout: Layout; cell: Cell}>();

	workingLayouts: Layout[] = [];
	selectedCell: Cell | null = null;
	activeLayoutId: string | null = null;
	selectedLineIndex: number | null = null;
	paletteFilter = '';
	paletteCollapsed = false;
	collapsedDatasets = new Set<string>();
	hoveredSlotId: string | null = null;
	splitEditMode = false;

	readonly columnPresets = [
		{label: 'S', px: 250},
		{label: 'M', px: 500},
		{label: 'L', px: 750},
		{label: 'XL', px: 1000}
	];

	readonly TEXT_BLOCK_SENTINEL = '__TEXT_BLOCK__';

	private resizing = false;
	private resizeMoved = false;
	private resizeCell: Cell | null = null;
	private resizeLayout: Layout | null = null;
	private resizeLineIndex: number | null = null;
	private resizeStartX = 0;
	private resizeStartColspan = 1;
	private resizeDirection: 'left' | 'right' = 'right';

	private splitResizing = false;
	private splitResizeCell: Cell | null = null;
	private splitResizeLayout: Layout | null = null;
	private splitResizeLineIndex: number | null = null;
	private splitResizeStartX = 0;
	private splitResizeStartLabelPx = 0;
	private splitResizeCellWidth = 0;
	private splitResizeScale = 1;

	snapIndicatorX: number | null = null;
	private gridCellsScreenLeft = 0;
	private readonly SNAP_THRESHOLD = 8;
	currentDragAbsoluteX: number | null = null;
	currentDragPercent: number | null = null;

	constructor(
		public languageService: LanguageService,
		private datasetModelManager: DatasetModelManagerService,
		private fieldModelManager: FieldModelManagerService,
		private formLayoutService: FormLayoutService,
		public formLayoutManager: FormLayoutManagerService,
		private snackBar: MatSnackBar,
		private dialog: MatDialog,
		private cdr: ChangeDetectorRef
	) {}

	ngOnInit(): void {
		if(this.projectId) {
			this.fieldModelManager.loadFull(this.projectId).subscribe();
		}
		if(this.formModelId) {
			this.formLayoutManager.load(this.projectId, this.formModelId).subscribe(() => {
				this.initWorkingLayouts();
			});
		}
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['projectId'] && this.projectId) {
			this.fieldModelManager.loadFull(this.projectId).subscribe();
		}
		if(changes['formModelId'] && this.formModelId && !changes['formModelId'].firstChange) {
			this.formLayoutManager.load(this.projectId, this.formModelId).subscribe(() => {
				this.initWorkingLayouts();
			});
		}
	}

	private initWorkingLayouts(): void {
		this.workingLayouts = this.formLayoutManager.getAll().map(l => JSON.parse(JSON.stringify(l)));
		this.selectedCell = null;
		this.selectedLineIndex = null;
		this.activeLayoutId = this.workingLayouts[0]?.formLayoutId ?? null;
		this.paletteFilter = '';
		for(const wl of this.workingLayouts) {
			for(const line of wl.lines) {
				this.normalizeLineCells(line, wl.columns.length);
			}
		}
	}

	discardChanges(): void {
		this.formLayoutManager.resetToOriginals();
		this.initWorkingLayouts();
		this.unsavedChanges.emit(false);
	}

	isLayoutModified(layoutId: string): boolean {
		return this.formLayoutManager.isModified(layoutId);
	}

	get globalMaxColumnWidth(): number {
		if(this.workingLayouts.length === 0) {
			return 250;
		}
		return Math.max(...this.workingLayouts.map(l => this.getTotalColumnWidth(l)));
	}

	getGridTemplateColumns(layout: Layout): string {
		return layout.columns
			.map((_, i) => `${this.getRenderedColumnWidth(layout, i)}fr`)
			.join(' ');
	}

	getColumnCount(layout: Layout): number {
		return layout.columns.length || 1;
	}

	getTotalColumnWidth(layout: Layout): number {
		return layout.columns.reduce((sum, col) => sum + (this.parseWidthPx(col.cssCode) ?? 250), 0);
	}

	getActiveColumnPreset(layout: Layout, colIndex: number): number | null {
		return this.parseWidthPx(layout.columns[colIndex]?.cssCode);
	}

	isColumnPresetDisabled(layout: Layout, colIndex: number, presetPx: number): boolean {
		const otherColsWidth = layout.columns
			.reduce((sum, col, i) => i === colIndex ? sum : sum + (this.parseWidthPx(col.cssCode) ?? 250), 0);
		return otherColsWidth + presetPx > 1250;
	}

	isLastColumnOccupied(layout: Layout): boolean {
		const colCount = this.getColumnCount(layout);
		for(const line of layout.lines) {
			let col = 0;
			for(const cell of line.cells) {
				const span = cell.colspan ?? 1;
				if(!this.isSpacerCell(cell) && col + span >= colCount) {
					return true;
				}
				col += span;
			}
		}
		return false;
	}

	addColumn(layout: Layout): void {
		if(this.getColumnCount(layout) >= 6) {
			return;
		}
		if(this.getTotalColumnWidth(layout) + 250 > 1250) {
			this.snackBar.open('Total column width would exceed 1250px', 'Close', {duration: 3000});
			return;
		}
		layout.columns = [...layout.columns, {}];
		this.normalizeAllLines(layout);
		this.recalculateCellLabelWidths(layout);
		this.markChanged(layout);
	}

	removeLastColumn(layout: Layout): void {
		if(this.getColumnCount(layout) <= 1) {
			return;
		}
		layout.columns = layout.columns.slice(0, -1);
		this.normalizeAllLines(layout);
		this.recalculateCellLabelWidths(layout);
		this.markChanged(layout);
	}

	setColumnWidth(layout: Layout, colIndex: number, px: number | null): void {
		layout.columns[colIndex].cssCode = px !== null ? `width: ${px}px` : undefined;
		this.recalculateCellLabelWidths(layout);
		this.markChanged(layout);
	}

	private recalculateCellLabelWidths(layout: Layout): void {
		for(let lineIndex = 0; lineIndex < layout.lines.length; lineIndex++) {
			for(const cell of layout.lines[lineIndex].cells) {
				if(!cell.cssCodeForLabel || !cell.cssCodeForInput) {
					continue;
				}
				const labelPx = this.parseWidthPx(cell.cssCodeForLabel);
				const inputPx = this.parseWidthPx(cell.cssCodeForInput);
				if(labelPx === null || inputPx === null || labelPx + inputPx === 0) {
					continue;
				}
				const ratio = labelPx / (labelPx + inputPx);
				const newWidth = this.getNetCellWidth(layout, cell, lineIndex);
				const newLabelPx = Math.round(newWidth * ratio);
				cell.cssCodeForLabel = `width: ${newLabelPx}px`;
				cell.cssCodeForInput = `width: ${newWidth - newLabelPx}px`;
			}
		}
	}

	get datasetGroups(): {datasetModelId: string; label: string; fields: FieldModel[]}[] {
		const allFields = this.fieldModelManager.getAll();
		const datasetMap = new Map<string, FieldModel[]>();
		for(const fm of allFields) {
			if(!fm.datasetModelId) {
				continue;
			}
			if(!datasetMap.has(fm.datasetModelId)) {
				datasetMap.set(fm.datasetModelId, []);
			}
			datasetMap.get(fm.datasetModelId)!.push(fm);
		}
		const groups: {datasetModelId: string; label: string; fields: FieldModel[]}[] = [];
		datasetMap.forEach((fields, datasetModelId) => {
			const filtered = this.paletteFilter.trim()
				? fields.filter(fm =>
					fm.id.toLowerCase().includes(this.paletteFilter.toLowerCase())
					|| (this.languageService.getTranslatedName(fm.shortname) || '').toLowerCase().includes(this.paletteFilter.toLowerCase())
				)
				: fields;
			if(filtered.length > 0) {
				const ds = this.datasetModelManager.getById(datasetModelId);
				groups.push({
					datasetModelId,
					label: ds ? (this.languageService.getTranslatedName(ds.shortname) || ds.id) : datasetModelId,
					fields: filtered
				});
			}
		});
		return groups.sort((a, b) => a.label.localeCompare(b.label));
	}

	get totalFieldCount(): number {
		return this.datasetGroups.reduce((sum, g) => sum + g.fields.length, 0);
	}

	toggleDataset(datasetModelId: string): void {
		if(this.collapsedDatasets.has(datasetModelId)) {
			this.collapsedDatasets.delete(datasetModelId);
		}
		else {
			this.collapsedDatasets.add(datasetModelId);
		}
	}

	isDatasetCollapsed(datasetModelId: string): boolean {
		return this.collapsedDatasets.has(datasetModelId);
	}

	get allSlotDropListIds(): string[] {
		const ids: string[] = [];
		for(const wl of this.workingLayouts) {
			wl.lines.forEach((_, li) => {
				wl.lines[li].cells.forEach((_, ci) => {
					ids.push(this.getSlotDropListId(wl.formLayoutId, li, ci));
				});
			});
		}
		return ids;
	}

	getSlotDropListId(layoutId: string, lineIndex: number, slotIndex: number): string {
		return `slot-${layoutId}-${lineIndex}-${slotIndex}`;
	}

	getLinesDropListId(layoutId: string): string {
		return `lines-${layoutId}`;
	}

	getSlotConnectedLists(): string[] {
		return ['field-palette', ...this.allSlotDropListIds];
	}

	isTextCell(cell: Cell): boolean {
		return !cell.fieldModelId;
	}

	isSpacerCell(cell: Cell): boolean {
		return cell.fieldModelId === '__SPACER__';
	}

	getTextCellPreview(cell: Cell): string {
		const raw = this.languageService.getTranslatedName(cell.textBefore as any) || '';
		return raw.replace(/<[^>]*>/g, '').trim() || 'Text block';
	}

	getFieldModelById(fieldModelId: string): FieldModel | undefined {
		return this.fieldModelManager.getById(fieldModelId);
	}

	getFieldLabel(fieldModelId: string): string {
		return this.languageService.getLabelById(fieldModelId, id => this.fieldModelManager.getById(id));
	}

	getFieldTypeBadge(fm: FieldModel): {icon: string; css: string; tooltip: string} {
		switch(fm.type) {
			case 'STRING': return {icon: 'short_text', css: 'badge-text', tooltip: 'Text'};
			case 'AUTO_COMPLETION': return {icon: 'hdr_auto', css: 'badge-text', tooltip: 'Auto completion'};
			case 'TEXTAREA': return {icon: 'notes', css: 'badge-text', tooltip: 'Textarea'};
			case 'DATE': return {icon: 'calendar_today', css: 'badge-date', tooltip: 'Date'};
			case 'DATE_SELECT': return {icon: 'edit_calendar', css: 'badge-date', tooltip: 'Date select'};
			case 'NUMBER': return {icon: 'pin', css: 'badge-number', tooltip: 'Number'};
			case 'SELECT': return {icon: 'arrow_drop_down_circle', css: 'badge-select', tooltip: 'Select'};
			case 'RADIO': return {icon: 'radio_button_checked', css: 'badge-radio', tooltip: 'Radio'};
			case 'CHECKBOX': return {icon: 'check_box', css: 'badge-check', tooltip: 'Checkbox'};
			case 'CHECKBOX_GROUP': return {icon: 'library_add_check', css: 'badge-check', tooltip: 'Checkbox group'};
			case 'FILE': return {icon: 'attach_file', css: 'badge-bool', tooltip: 'File'};
			default: return {icon: 'help_outline', css: 'badge-default', tooltip: 'Unknown'};
		}
	}

	getCellGridColumn(layout: Layout, cell: Cell, lineIndex: number): string {
		const start = this.getCellStartColumn(layout, cell, lineIndex) + 1;
		return `${start} / span ${cell.colspan ?? 1}`;
	}

	private getCellStartColumn(layout: Layout, cell: Cell, lineIndex: number): number {
		const line = layout.lines[lineIndex];
		let col = 0;
		for(const c of line.cells) {
			if(c === cell) {
				return col;
			}
			col += c.colspan ?? 1;
		}
		return 0;
	}

	private normalizeLineCells(line: LayoutLine, colCount: number): void {
		const usedSpans = line.cells.reduce((sum, c) => sum + (c.colspan ?? 1), 0);
		const missing = colCount - usedSpans;
		for(let i = 0; i < missing; i++) {
			line.cells.push(this.buildSpacerCell());
		}
	}

	private normalizeAllLines(layout: Layout): void {
		const colCount = this.getColumnCount(layout);
		for(const line of layout.lines) {
			while(line.cells.length > 0) {
				const last = line.cells[line.cells.length - 1];
				const total = line.cells.reduce((s, c) => s + (c.colspan ?? 1), 0);
				if(this.isSpacerCell(last) && total > colCount) {
					line.cells.pop();
				}
				else {
					break;
				}
			}
			this.normalizeLineCells(line, colCount);
		}
	}

	private rebalanceLine(layout: Layout, line: LayoutLine): void {
		const colCount = this.getColumnCount(layout);
		while(line.cells.length > 0) {
			const total = line.cells.reduce((s, c) => s + (c.colspan ?? 1), 0);
			const last = line.cells[line.cells.length - 1];
			if(total > colCount && this.isSpacerCell(last)) {
				line.cells.pop();
			}
			else {
				break;
			}
		}

		let total = line.cells.reduce((s, c) => s + (c.colspan ?? 1), 0);
		if(total > colCount) {
			for(let i = line.cells.length - 1; i >= 0 && total > colCount; i--) {
				if(this.isSpacerCell(line.cells[i])) {
					const span = line.cells[i].colspan ?? 1;
					line.cells.splice(i, 1);
					total -= span;
				}
			}
		}

		this.normalizeLineCells(line, colCount);
		line.cells = [...line.cells];
	}

	isLineFull(layout: Layout, lineIndex: number): boolean {
		return layout.lines[lineIndex].cells.every(c => !this.isSpacerCell(c));
	}

	addLine(layout: Layout): void {
		const colCount = this.getColumnCount(layout);
		const spacers: Cell[] = Array.from({length: colCount}, () => this.buildSpacerCell());
		const newLine: LayoutLine = {formLayoutLineId: '', cells: spacers};
		layout.lines = [...layout.lines, newLine];
		this.activeLayoutId = layout.formLayoutId;
		this.selectedLineIndex = layout.lines.length - 1;
		this.markChanged(layout);
	}

	deleteLine(layout: Layout, lineIndex: number): void {
		if(this.activeLayoutId === layout.formLayoutId && this.selectedLineIndex === lineIndex) {
			this.selectedCell = null;
			this.selectedLineIndex = null;
		}
		layout.lines = layout.lines.filter((_, i) => i !== lineIndex);
		this.markChanged(layout);
	}

	deleteCell(layout: Layout, lineIndex: number, cellIndex: number, event: Event): void {
		event.stopPropagation();
		const line = layout.lines[lineIndex];
		if(this.selectedCell === line.cells[cellIndex]) {
			this.selectedCell = null;
			this.selectedLineIndex = null;
		}
		line.cells[cellIndex] = this.buildSpacerCell();
		line.cells = [...line.cells];
		this.markChanged(layout);
	}

	private buildCell(layout: Layout, fieldModel: FieldModel): Cell {
		const baseCode = `${layout.id}_${fieldModel.id}`.toUpperCase();
		return {
			formLayoutCellId: '',
			id: this.generateUniqueCellCode(layout, baseCode),
			datasetModelId: fieldModel.datasetModelId,
			fieldModelId: fieldModel.fieldModelId,
			visibilityCriteria: [],
			displayLabel: true,
			displayPossibleValueLabels: false,
			colspan: 1,
			hasPrintButton: false
		};
	}

	private buildTextCell(layout: Layout): Cell {
		return {
			formLayoutCellId: '',
			id: this.generateUniqueCellCode(layout, `${layout.id}_TEXT`.toUpperCase()),
			datasetModelId: '',
			fieldModelId: '',
			visibilityCriteria: [],
			displayLabel: false,
			displayPossibleValueLabels: false,
			colspan: 1,
			hasPrintButton: false,
			possibleValuesColumnNumber: undefined,
			possibleValuesColumnWidth: undefined,
			textBefore: {},
			textAfter: {}
		};
	}

	private buildSpacerCell(): Cell {
		return {
			formLayoutCellId: '',
			id: crypto.randomUUID(),
			datasetModelId: '',
			fieldModelId: '__SPACER__',
			visibilityCriteria: [],
			displayLabel: false,
			displayPossibleValueLabels: false,
			colspan: 1,
			hasPrintButton: false
		};
	}

	private generateUniqueCellCode(layout: Layout, baseCode: string): string {
		const existing = new Set<string>();
		for(const line of layout.lines) {
			for(const cell of line.cells) {
				if(cell.id) {
					existing.add(cell.id);
				}
			}
		}
		if(!existing.has(baseCode)) {
			return baseCode;
		}
		let i = 2;
		while(existing.has(`${baseCode}_${i}`)) {
			i++;
		}
		return `${baseCode}_${i}`;
	}

	onDropLine(layout: Layout, event: CdkDragDrop<LayoutLine[]>): void {
		moveItemInArray(layout.lines, event.previousIndex, event.currentIndex);
		this.markChanged(layout);
	}

	onDropLayout(event: CdkDragDrop<Layout[]>): void {
		if(event.previousIndex === event.currentIndex) {
			return;
		}
		moveItemInArray(this.workingLayouts, event.previousIndex, event.currentIndex);
		this.workingLayouts.forEach((l, i) => l.sortOrder = i);
		const saves = this.workingLayouts.map(l =>
			this.formLayoutService.updateLayout(this.projectId, this.formModelId, l.formLayoutId, l)
		);
		saves.reduce<Observable<Layout | null>>(
			(chain, save) => chain.pipe(switchMap(() => save)),
			of(null)
		).subscribe({
			next: () => this.snackBar.open('Layout order saved', 'Close', {duration: 2000}),
			error: () => this.snackBar.open('Failed to save layout order', 'Close', {duration: 3000})
		});
	}

	onDropOnSlot(layout: Layout, event: CdkDragDrop<any>, lineIndex: number, slotIndex: number): void {
		this.hoveredSlotId = null;
		const line = layout.lines[lineIndex];
		const data = event.item.data;
		const isExistingCell = data && 'formLayoutCellId' in data;

		if(isExistingCell) {
			const draggedCell = data as Cell;
			let sourceLine: LayoutLine | undefined;
			for(const wl of this.workingLayouts) {
				sourceLine = wl.lines.find(l => l.cells.includes(draggedCell));
				if(sourceLine) {
					break;
				}
			}
			if(!sourceLine) {
				return;
			}
			const sourceIndex = sourceLine.cells.indexOf(draggedCell);
			const targetCell = line.cells[slotIndex];
			const draggedColspan = draggedCell.colspan ?? 1;

			sourceLine.cells[sourceIndex] = targetCell;
			targetCell.colspan = 1;
			line.cells[slotIndex] = draggedCell;
			draggedCell.colspan = 1;

			const extraSpacers = draggedColspan - 1;
			for(let s = 0; s < extraSpacers; s++) {
				sourceLine.cells.splice(sourceIndex + 1, 0, this.buildSpacerCell());
			}
			sourceLine.cells = [...sourceLine.cells];
			line.cells = [...line.cells];
		}
		else if(data?.fieldModelId === this.TEXT_BLOCK_SENTINEL) {
			const newCell = this.buildTextCell(layout);
			const targetCell = line.cells[slotIndex];
			newCell.colspan = targetCell.colspan ?? 1;
			line.cells[slotIndex] = newCell;
			line.cells = [...line.cells];
		}
		else {
			line.cells[slotIndex] = this.buildCell(layout, data as FieldModel);
			line.cells = [...line.cells];
		}
		this.rebalanceLine(layout, layout.lines[lineIndex]);
		this.markChanged(layout);
	}

	private getActiveLayout(): Layout | null {
		return this.workingLayouts.find(l => l.formLayoutId === this.activeLayoutId) ?? this.workingLayouts[0] ?? null;
	}

	setActiveLayout(layoutId: string): void {
		if(this.activeLayoutId !== layoutId) {
			this.activeLayoutId = layoutId;
			this.selectedCell = null;
			this.selectedLineIndex = null;
		}
	}

	togglePalette(): void {
		this.paletteCollapsed = !this.paletteCollapsed;
	}

	onClickFieldInPalette(fieldModel: FieldModel): void {
		const layout = this.getActiveLayout();
		if(!layout) {
			return;
		}
		let targetIndex = this.selectedLineIndex ?? layout.lines.length - 1;
		if(targetIndex < 0) {
			this.addLine(layout);
			targetIndex = 0;
		}
		if(this.isLineFull(layout, targetIndex)) {
			this.snackBar.open('Line is full — add a new line or increase column count', 'Close', {duration: 3000});
			return;
		}
		const line = layout.lines[targetIndex];
		const spacerIndex = line.cells.findIndex(c => this.isSpacerCell(c));
		if(spacerIndex === -1) {
			return;
		}
		const newCell = this.buildCell(layout, fieldModel);
		line.cells[spacerIndex] = newCell;
		line.cells = [...line.cells];
		this.selectedCell = newCell;
		this.selectedLineIndex = targetIndex;
		this.markChanged(layout);
	}

	onClickTextBlockInPalette(): void {
		const layout = this.getActiveLayout();
		if(!layout) {
			return;
		}
		let targetIndex = this.selectedLineIndex ?? layout.lines.length - 1;
		if(targetIndex < 0) {
			this.addLine(layout);
			targetIndex = 0;
		}
		if(this.isLineFull(layout, targetIndex)) {
			this.snackBar.open('Line is full — add a new line or increase column count', 'Close', {duration: 3000});
			return;
		}
		const line = layout.lines[targetIndex];
		const spacerIndex = line.cells.findIndex(c => this.isSpacerCell(c));
		if(spacerIndex === -1) {
			return;
		}
		const newCell = this.buildTextCell(layout);
		newCell.colspan = line.cells[spacerIndex].colspan ?? 1;
		line.cells[spacerIndex] = newCell;
		line.cells = [...line.cells];
		this.selectedCell = newCell;
		this.selectedLineIndex = targetIndex;
		this.markChanged(layout);
	}

	onCellClick(layout: Layout, cell: Cell, lineIndex: number): void {
		if(this.resizeMoved) {
			this.resizeMoved = false;
			return;
		}
		if(this.isSplitEditActive()) {
			return;
		}
		if(!this.isSpacerCell(cell)) {
			this.activeLayoutId = layout.formLayoutId;
			this.selectCell(layout, cell, lineIndex);
		}
	}

	selectCell(layout: Layout, cell: Cell, lineIndex: number): void {
		this.selectedCell = cell;
		this.selectedLineIndex = lineIndex;
		this.openCellDialog(layout, cell, lineIndex);
	}

	isCellSelected(cell: Cell): boolean {
		return this.selectedCell === cell;
	}

	isLineSelected(layout: Layout, lineIndex: number): boolean {
		return this.activeLayoutId === layout.formLayoutId && this.selectedLineIndex === lineIndex;
	}

	private openCellDialog(layout: Layout, cell: Cell, lineIndex: number): void {
		const ref = this.dialog.open(FormLayoutCellDialogComponent, {
			data: {
				cell,
				columnCount: this.getColumnCount(layout),
				allLayouts: this.workingLayouts,
				projectLanguages: this.projectLanguages,
				effectiveCellWidth: this.getNetCellWidth(layout, cell, lineIndex)
			},
			panelClass: 'rodano-dialog'
		});
		ref.afterClosed().subscribe((result: Cell | null) => {
			this.selectedCell = null;
			this.selectedLineIndex = null;
			if(!result) {
				return;
			}
			const prevColspan = cell.colspan ?? 1;
			Object.assign(cell, result);
			if((result.colspan ?? 1) !== prevColspan) {
				this.rebalanceLine(layout, layout.lines[lineIndex]);
				this.recalculateCellLabelWidths(layout);
			}
			this.markChanged(layout);
		});
	}

	isResizingCell(cell: Cell): boolean {
		return this.resizing && this.resizeCell === cell;
	}

	onResizeStart(event: MouseEvent, layout: Layout, cell: Cell, lineIndex: number, direction: 'left' | 'right'): void {
		event.stopPropagation();
		event.preventDefault();
		this.resizing = true;
		this.resizeMoved = false;
		this.resizeCell = cell;
		this.resizeLayout = layout;
		this.resizeLineIndex = lineIndex;
		this.resizeStartX = event.clientX;
		this.resizeStartColspan = cell.colspan ?? 1;
		this.resizeDirection = direction;
	}

	@HostListener('document:mousemove', ['$event'])
	onResizeMove(event: MouseEvent): void {
		if(this.resizing && this.resizeCell && this.resizeLayout && this.resizeLineIndex !== null) {
			const colWidth = this.getEffectiveCellWidth(this.resizeLayout, this.resizeCell, this.resizeLineIndex) / (this.resizeCell.colspan ?? 1);
			const deltaX = event.clientX - this.resizeStartX;
			const sign = this.resizeDirection === 'right' ? 1 : -1;
			const deltaCols = Math.round((deltaX * sign) / colWidth);
			const colCount = this.getColumnCount(this.resizeLayout);
			const newColspan = Math.min(Math.max(1, this.resizeStartColspan + deltaCols), colCount);

			if(newColspan !== this.resizeCell.colspan) {
				this.resizeMoved = true;
				this.resizeCell.colspan = newColspan;
				const line = this.resizeLayout.lines[this.resizeLineIndex];
				this.rebalanceLine(this.resizeLayout, line);
				this.markChanged(this.resizeLayout);
			}
		}

		if(this.splitResizing && this.splitResizeCell && this.splitResizeLayout && this.splitResizeLineIndex !== null) {
			const delta = (event.clientX - this.splitResizeStartX) * this.splitResizeScale;
			const rawPx = this.splitResizeStartLabelPx + delta;
			const clampedPx = Math.min(Math.max(40, rawPx), this.splitResizeCellWidth - 40);
			const steppedPx = Math.round(clampedPx / 5) * 5;
			const snapped = this.snapLabelPx(this.splitResizeLayout, this.splitResizeCell, this.splitResizeLineIndex, steppedPx);
			const labelPx = Math.round(snapped.px);
			this.splitResizeCell.cssCodeForLabel = `width: ${labelPx}px`;
			this.splitResizeCell.cssCodeForInput = `width: ${this.splitResizeCellWidth - labelPx}px`;
			this.currentDragAbsoluteX = snapped.absoluteX;
			this.currentDragPercent = this.getRulerPercent(this.splitResizeLayout, snapped.absoluteX);
			this.cdr.detectChanges();
		}
	}

	@HostListener('document:mouseup')
	onResizeEnd(): void {
		this.resizing = false;
		this.resizeCell = null;
		this.resizeLayout = null;
		this.resizeLineIndex = null;

		if(this.splitResizing && this.splitResizeLayout && this.splitResizeCell) {
			this.markChanged(this.splitResizeLayout);
		}
		this.splitResizing = false;
		this.splitResizeCell = null;
		this.splitResizeLayout = null;
		this.splitResizeLineIndex = null;
		this.snapIndicatorX = null;
		this.currentDragAbsoluteX = null;
		this.currentDragPercent = null;
	}

	getMaxLayoutWidth(): number {
		return Math.max(...this.workingLayouts.map(l => this.getTotalColumnWidth(l)), 250);
	}

	getRenderedColumnWidth(layout: Layout, colIndex: number): number {
		const explicit = this.parseWidthPx(layout.columns[colIndex]?.cssCode);
		if(explicit !== null) {
			return explicit;
		}
		const maxWidth = this.getMaxLayoutWidth();
		const fixedWidth = layout.columns.reduce((sum, col) => {
			const w = this.parseWidthPx(col.cssCode);
			return sum + (w ?? 0);
		}, 0);
		const autoCount = layout.columns.filter(col => this.parseWidthPx(col.cssCode) === null).length;
		return autoCount > 0 ? Math.floor((maxWidth - fixedWidth) / autoCount) : 250;
	}

	getEffectiveCellWidth(layout: Layout, cell: Cell, lineIndex?: number): number {
		const GAP = 6;
		const idx = lineIndex ?? this.selectedLineIndex;
		if(idx === null) {
			return 250;
		}
		const startCol = this.getCellStartColumn(layout, cell, idx);
		const colspan = cell.colspan ?? 1;
		let total = 0;
		for(let i = startCol; i < startCol + colspan && i < layout.columns.length; i++) {
			total += this.getRenderedColumnWidth(layout, i);
			if(i < startCol + colspan - 1) {
				total += GAP;
			}
		}
		return total || 250;
	}

	openSettings(layout: Layout): void {
		const ref = this.dialog.open(FormLayoutCreateDialogComponent, {
			data: {
				projectId: this.projectId,
				formModelId: this.formModelId,
				generateFromDataset: false,
				layout,
				languages: this.projectLanguages
			},
			panelClass: 'rodano-dialog'
		});
		ref.afterClosed().subscribe(result => {
			if(!result) {
				return;
			}
			Object.assign(layout, result);
			this.markChanged(layout);
		});
	}

	onCreateLayout(generateFromDataset: boolean): void {
		const ref = this.dialog.open(FormLayoutCreateDialogComponent, {
			width: '560px',
			data: {
				projectId: this.projectId,
				formModelId: this.formModelId,
				project: this.project,
				generateFromDataset,
				languages: this.projectLanguages
			}
		});
		ref.afterClosed().subscribe((result: Layout | null) => {
			if(!result) {
				return;
			}
			result.sortOrder = this.workingLayouts.length;
			this.formLayoutService.createLayout(this.projectId, this.formModelId, result).subscribe({
				next: created => {
					this.formLayoutManager.addLayout(created);
					const wl: Layout = JSON.parse(JSON.stringify(created));
					for(const line of wl.lines) {
						this.normalizeLineCells(line, wl.columns.length);
					}
					this.workingLayouts = [...this.workingLayouts, wl];
					this.activeLayoutId = wl.formLayoutId;
					this.snackBar.open('Layout created', 'Close', {duration: 2000});
					this.layoutCreated.emit(created);
				},
				error: () => {
					this.snackBar.open('Failed to create layout', 'Close', {duration: 3000});
				}
			});
		});
	}

	onDeleteLayout(layout: Layout): void {
		const ref = this.dialog.open(ConfirmationDialogComponent, {
			width: '450px',
			data: {
				title: 'Delete Layout',
				message: `Are you sure you want to delete layout "${layout.id}"?`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		ref.afterClosed().subscribe((confirmed: boolean) => {
			if(!confirmed) {
				return;
			}
			this.formLayoutService.deleteLayout(this.projectId, this.formModelId, layout.formLayoutId).subscribe({
				next: () => {
					this.formLayoutManager.removeLayout(layout.formLayoutId);
					this.workingLayouts = this.workingLayouts.filter(l => l.formLayoutId !== layout.formLayoutId);
					if(this.activeLayoutId === layout.formLayoutId) {
						this.activeLayoutId = this.workingLayouts[0]?.formLayoutId ?? null;
					}
					this.snackBar.open('Layout deleted', 'Close', {duration: 2000});
					this.layoutDeleted.emit(layout.formLayoutId);
				},
				error: () => this.snackBar.open('Failed to delete layout', 'Close', {duration: 3000})
			});
		});
	}

	onPaletteFilterInput(event: Event): void {
		this.paletteFilter = (event.target as HTMLInputElement).value;
	}

	get projectLanguages(): ProjectLanguage[] {
		return this.languageService.projectLanguages;
	}

	onClose(): void {
		this.closed.emit();
	}

	onSlotEntered(slotId: string): void {
		this.hoveredSlotId = slotId;
	}

	onSlotExited(slotId: string): void {
		if(this.hoveredSlotId === slotId) {
			this.hoveredSlotId = null;
		}
	}

	parseWidthPx(cssCode: string | null | undefined): number | null {
		const match = cssCode?.match(/width:\s*(\d+)px/);
		return match ? parseInt(match[1]) : null;
	}

	protected markChanged(layout: Layout): void {
		const cleaned = JSON.parse(JSON.stringify(layout));
		for(const line of cleaned.lines) {
			while(line.cells.length > 0 && line.cells[line.cells.length - 1].fieldModelId === '__SPACER__') {
				line.cells.pop();
			}
		}
		this.formLayoutManager.update(cleaned);
		this.unsavedChanges.emit(this.formLayoutManager.getModificationCount() > 0);
	}

	toggleSplitEditMode(event?: Event): void {
		event?.stopPropagation();
		this.splitEditMode = !this.splitEditMode;
	}

	isSplitEditActive(): boolean {
		return this.splitEditMode;
	}

	onSplitResizeStart(event: MouseEvent, layout: Layout, cell: Cell, lineIndex: number): void {
		event.stopPropagation();
		event.preventDefault();
		this.splitResizing = true;
		this.splitResizeCell = cell;
		this.splitResizeLayout = layout;
		this.splitResizeLineIndex = lineIndex;
		this.splitResizeStartX = event.clientX;

		const GAP = 6;
		const colspan = cell.colspan ?? 1;
		this.splitResizeCellWidth = this.getEffectiveCellWidth(layout, cell, lineIndex) - (colspan - 1) * GAP;

		const nominalGridWidth = layout.columns.reduce((sum, _, i) =>
			sum + this.getRenderedColumnWidth(layout, i), 0) + (layout.columns.length - 1) * GAP;

		const lineCells = (event.target as HTMLElement).closest('.grid-line')
			?.querySelector('.line-cells') as HTMLElement | null;
		this.gridCellsScreenLeft = lineCells ? lineCells.getBoundingClientRect().left : 0;

		const PADDING = 20;
		const domContentWidth = lineCells ? lineCells.getBoundingClientRect().width - PADDING : nominalGridWidth;
		this.splitResizeScale = domContentWidth > 0 ? nominalGridWidth / domContentWidth : 1;

		this.splitResizeStartLabelPx = this.parseWidthPx(cell.cssCodeForLabel) ?? Math.round(Math.round(this.splitResizeCellWidth * 0.5 / 5) * 5);
	}

	isSplitResizingCell(cell: Cell): boolean {
		return this.splitResizing && this.splitResizeCell === cell;
	}

	getSplitLabelWidth(layout: Layout, cell: Cell, lineIndex: number): string {
		const labelPx = this.parseWidthPx(cell.cssCodeForLabel);
		if(labelPx !== null) {
			const nominalWidth = this.getNetCellWidth(layout, cell, lineIndex);
			return `${(labelPx / nominalWidth * 100).toFixed(2)}%`;
		}
		return '50%';
	}

	getSplitInputWidth(layout: Layout, cell: Cell, lineIndex: number): string {
		const inputPx = this.parseWidthPx(cell.cssCodeForInput);
		if(inputPx !== null) {
			const nominalWidth = this.getNetCellWidth(layout, cell, lineIndex);
			return `${(inputPx / nominalWidth * 100).toFixed(2)}%`;
		}
		return '50%';
	}

	getMiniRulerTicks(layout: Layout, cell: Cell, lineIndex: number): {pct: number; major: boolean; label: string | null}[] {
		const width = this.getNetCellWidth(layout, cell, lineIndex);
		const ticks: {pct: number; major: boolean; label: string | null}[] = [];
		for(let x = 0; x <= width; x += 50) {
			const major = x % 100 === 0;
			ticks.push({pct: x / width * 100, major, label: major ? `${x}` : null});
		}
		return ticks;
	}

	getSplitLabelPct(layout: Layout, cell: Cell, lineIndex: number): number {
		const width = this.getNetCellWidth(layout, cell, lineIndex);
		const labelPx = this.parseWidthPx(cell.cssCodeForLabel) ?? Math.round(width * 0.5);
		return labelPx / width * 100;
	}

	getLabelPxValue(cell: Cell): number {
		return this.parseWidthPx(cell.cssCodeForLabel) ?? 0;
	}

	getCellStartAbsoluteX(layout: Layout, cell: Cell, lineIndex: number): number {
		const startCol = this.getCellStartColumn(layout, cell, lineIndex);
		let x = 0;
		for(let i = 0; i < startCol; i++) {
			x += this.getRenderedColumnWidth(layout, i);
		}
		return x;
	}

	getNetCellWidth(layout: Layout, cell: Cell, lineIndex: number): number {
		const GAP = 6;
		const colspan = cell.colspan ?? 1;
		return this.getEffectiveCellWidth(layout, cell, lineIndex) - (colspan - 1) * GAP;
	}

	getAllSnapCandidates(excludeCell: Cell): number[] {
		const candidates = new Set<number>();
		for(const wl of this.workingLayouts) {
			for(let li = 0; li < wl.lines.length; li++) {
				for(const cell of wl.lines[li].cells) {
					if(cell === excludeCell) {
						continue;
					}
					if(this.isSpacerCell(cell) || this.isTextCell(cell)) {
						continue;
					}
					const cellStartX = this.getCellStartAbsoluteX(wl, cell, li);
					const cellWidth = this.getEffectiveCellWidth(wl, cell, li);
					const labelPx = this.parseWidthPx(cell.cssCodeForLabel) ?? Math.round(cellWidth * 0.5);
					candidates.add(cellStartX + labelPx);
				}
			}
		}
		return Array.from(candidates);
	}

	snapLabelPx(layout: Layout, cell: Cell, lineIndex: number, labelPx: number): {px: number; snapped: boolean; absoluteX: number} {
		const cellStartX = this.getCellStartAbsoluteX(layout, cell, lineIndex);
		const absoluteX = cellStartX + labelPx;
		const candidates = this.getAllSnapCandidates(cell);
		let bestDist = this.SNAP_THRESHOLD;
		let bestAbsolute = absoluteX;
		for(const candidate of candidates) {
			const dist = Math.abs(candidate - absoluteX);
			if(dist < bestDist) {
				bestDist = dist;
				bestAbsolute = candidate;
			}
		}
		const snapped = bestAbsolute !== absoluteX;
		return {px: bestAbsolute - cellStartX, snapped, absoluteX: bestAbsolute};
	}

	getRulerPercent(layout: Layout, nominalPx: number): number {
		const total = layout.columns.reduce((sum, _, i) =>
			sum + this.getRenderedColumnWidth(layout, i), 0);
		return total > 0 ? (nominalPx / total * 100) : 0;
	}
}
