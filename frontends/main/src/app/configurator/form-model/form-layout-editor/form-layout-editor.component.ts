import {Component, EventEmitter, HostListener, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
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
import {
	FormLayoutCellDialogComponent
} from '../../dialogs/form-model/form-layout-cell-dialog/form-layout-cell-dialog.component';
import {
	FormLayoutCreateDialogComponent
} from '../../dialogs/form-model/form-layout-create-dialog/form-layout-create-dialog.component';

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
	@Input() layout!: Layout;
	@Input() allLayouts: Layout[] = [];
	@Output() closed = new EventEmitter<void>();
	@Output() layoutChanged = new EventEmitter<Layout>();

	workingLayout: Layout | null = null;
	selectedCell: Cell | null = null;
	selectedLineIndex: number | null = null;
	hasChanges = false;
	paletteFilter = '';
	collapsedDatasets = new Set<string>();

	readonly columnPresets = [
		{label: 'S', px: 150},
		{label: 'M', px: 250},
		{label: 'L', px: 400},
		{label: 'XL', px: 600}
	];

	readonly TEXT_BLOCK_SENTINEL = '__TEXT_BLOCK__';

	hoveredSlotId: string | null = null;

	private resizing = false;
	private resizeMoved = false;
	private resizeCell: Cell | null = null;
	private resizeLineIndex: number | null = null;
	private resizeStartX = 0;
	private resizeStartColspan = 1;
	private resizeDirection: 'left' | 'right' = 'right';

	constructor(
		public languageService: LanguageService,
		private datasetModelManager: DatasetModelManagerService,
		private fieldModelManager: FieldModelManagerService,
		private snackBar: MatSnackBar,
		private dialog: MatDialog
	) {}

	ngOnInit(): void {
		this.initWorkingLayout();
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['layout'] && this.layout) {
			this.initWorkingLayout();
		}
	}

	private initWorkingLayout(): void {
		this.workingLayout = JSON.parse(JSON.stringify(this.layout));
		this.selectedCell = null;
		this.selectedLineIndex = null;
		this.hasChanges = false;
		this.paletteFilter = '';
		if(this.workingLayout) {
			for(const line of this.workingLayout.lines) {
				this.normalizeLineCells(line);
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
		else {this.collapsedDatasets.add(datasetModelId);}
	}

	isDatasetCollapsed(datasetModelId: string): boolean {
		return this.collapsedDatasets.has(datasetModelId);
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

	get columnCount(): number {
		return this.workingLayout?.columns.length ?? 1;
	}

	getActiveColumnPreset(colIndex: number): number | null {
		return this.parseWidthPx(this.workingLayout?.columns[colIndex]?.cssCode);
	}

	isColumnPresetDisabled(colIndex: number, presetPx: number): boolean {
		const otherColsWidth = this.workingLayout!.columns
			.reduce((sum, col, i) => i === colIndex ? sum : sum + (this.parseWidthPx(col.cssCode) ?? 250), 0);
		return otherColsWidth + presetPx > 1250;
	}

	isLastColumnOccupied(): boolean {
		if(!this.workingLayout) {
			return false;
		}
		for(const line of this.workingLayout.lines) {
			let col = 0;
			for(const cell of line.cells) {
				const span = cell.colspan ?? 1;
				if(!this.isSpacerCell(cell) && col + span >= this.columnCount) {
					return true;
				}
				col += span;
			}
		}
		return false;
	}

	getTotalColumnWidth(): number {
		return this.workingLayout?.columns
			.reduce((sum, col) => sum + (this.parseWidthPx(col.cssCode) ?? 0), 0) ?? 0;
	}

	addColumn(): void {
		if(!this.workingLayout || this.columnCount >= 6) {
			return;
		}
		if(this.getTotalColumnWidth() + 250 > 1250) {
			this.snackBar.open('Total column width would exceed 1250px', 'Close', {duration: 3000});
			return;
		}
		this.workingLayout.columns = [...this.workingLayout.columns, {}];
		this.normalizeAllLines();
		this.markChanged();
	}

	removeLastColumn(): void {
		if(!this.workingLayout || this.columnCount <= 1) {
			return;
		}
		this.workingLayout.columns = this.workingLayout.columns.slice(0, -1);
		this.normalizeAllLines();
		this.markChanged();
	}

	setColumnWidth(colIndex: number, px: number | null): void {
		if(!this.workingLayout) {
			return;
		}
		this.workingLayout.columns[colIndex].cssCode = px !== null ? `width: ${px}px` : undefined;
		this.markChanged();
	}

	getColumnWidth(colIndex: number): number {
		const col = this.workingLayout?.columns[colIndex];
		return this.parseWidthPx(col?.cssCode) ?? 250;
	}

	get allSlotDropListIds(): string[] {
		if(!this.workingLayout) {
			return [];
		}
		const ids: string[] = [];
		this.workingLayout.lines.forEach((line, li) => {
			line.cells.forEach((_, ci) => {
				ids.push(this.getSlotDropListId(li, ci));
			});
		});
		return ids;
	}

	getSlotDropListId(lineIndex: number, slotIndex: number): string {
		return `slot-${this.workingLayout!.formLayoutId}-${lineIndex}-${slotIndex}`;
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

	private rebalanceLine(line: LayoutLine): void {
		while(line.cells.length > 0) {
			const total = line.cells.reduce((s, c) => s + (c.colspan ?? 1), 0);
			const last = line.cells[line.cells.length - 1];
			if(total > this.columnCount && this.isSpacerCell(last)) {
				line.cells.pop();
			}
			else {
				break;
			}
		}

		let total = line.cells.reduce((s, c) => s + (c.colspan ?? 1), 0);
		if(total > this.columnCount) {
			for(let i = line.cells.length - 1; i >= 0 && total > this.columnCount; i--) {
				if(this.isSpacerCell(line.cells[i])) {
					const span = line.cells[i].colspan ?? 1;
					line.cells.splice(i, 1);
					total -= span;
				}
			}
		}

		this.normalizeLineCells(line);
		line.cells = [...line.cells];
	}

	getCellGridColumn(cell: Cell, lineIndex: number): string {
		const start = this.getCellStartColumn(cell, lineIndex) + 1;
		return `${start} / span ${cell.colspan ?? 1}`;
	}

	private getCellStartColumn(cell: Cell, lineIndex: number): number {
		if(!this.workingLayout) {
			return 0;
		}
		const line = this.workingLayout.lines[lineIndex];
		let col = 0;
		for(const c of line.cells) {
			if(c === cell) {
				return col;
			}
			col += c.colspan ?? 1;
		}
		return 0;
	}

	private normalizeLineCells(line: LayoutLine): void {
		const usedSpans = line.cells.reduce((sum, c) => sum + (c.colspan ?? 1), 0);
		const missing = this.columnCount - usedSpans;
		for(let i = 0; i < missing; i++) {
			line.cells.push(this.buildSpacerCell());
		}
	}

	private normalizeAllLines(): void {
		if(!this.workingLayout) {
			return;
		}
		for(const line of this.workingLayout.lines) {
			while(line.cells.length > 0) {
				const last = line.cells[line.cells.length - 1];
				const total = line.cells.reduce((s, c) => s + (c.colspan ?? 1), 0);
				if(this.isSpacerCell(last) && total > this.columnCount) {
					line.cells.pop();
				}
				else {break;}
			}
			this.normalizeLineCells(line);
		}
	}

	isLineFull(lineIndex: number): boolean {
		if(!this.workingLayout) {
			return false;
		}
		return this.workingLayout.lines[lineIndex].cells.every(c => !this.isSpacerCell(c));
	}

	addLine(): void {
		if(!this.workingLayout) {
			return;
		}
		const spacers: Cell[] = Array.from({length: this.columnCount}, () => this.buildSpacerCell());
		const newLine: LayoutLine = {formLayoutLineId: '', cells: spacers};
		this.workingLayout.lines = [...this.workingLayout.lines, newLine];
		this.markChanged();
	}

	deleteLine(lineIndex: number): void {
		if(!this.workingLayout) {
			return;
		}
		if(this.selectedLineIndex === lineIndex) {
			this.selectedCell = null;
			this.selectedLineIndex = null;
		}
		this.workingLayout.lines = this.workingLayout.lines.filter((_, i) => i !== lineIndex);
		this.markChanged();
	}

	deleteCell(lineIndex: number, cellIndex: number, event: Event): void {
		event.stopPropagation();
		if(!this.workingLayout) {
			return;
		}
		const line = this.workingLayout.lines[lineIndex];
		if(this.selectedCell === line.cells[cellIndex]) {
			this.selectedCell = null;
			this.selectedLineIndex = null;
		}
		line.cells[cellIndex] = this.buildSpacerCell();
		line.cells = [...line.cells];
		this.markChanged();
	}

	private buildCell(fieldModel: FieldModel): Cell {
		const baseCode = `${this.workingLayout!.id}_${fieldModel.id}`.toUpperCase();
		return {
			formLayoutCellId: '',
			id: this.generateUniqueCellCode(baseCode),
			datasetModelId: fieldModel.datasetModelId,
			fieldModelId: fieldModel.fieldModelId,
			visibilityCriteria: [],
			displayLabel: true,
			displayPossibleValueLabels: false,
			colspan: 1,
			hasPrintButton: false
		};
	}

	private buildTextCell(): Cell {
		return {
			formLayoutCellId: '',
			id: this.generateUniqueCellCode(`${this.workingLayout!.id}_TEXT`.toUpperCase()),
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
			id: this.generateUniqueCellCode(`${this.workingLayout!.id}_SPACER`.toUpperCase()),
			datasetModelId: '',
			fieldModelId: '__SPACER__',
			visibilityCriteria: [],
			displayLabel: false,
			displayPossibleValueLabels: false,
			colspan: 1,
			hasPrintButton: false
		};
	}

	private generateUniqueCellCode(baseCode: string): string {
		const existing = new Set<string>();
		for(const line of this.workingLayout!.lines) {
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

	onDropLine(event: CdkDragDrop<LayoutLine[]>): void {
		if(!this.workingLayout) {
			return;
		}
		moveItemInArray(this.workingLayout.lines, event.previousIndex, event.currentIndex);
		this.markChanged();
	}

	onDropOnSlot(event: CdkDragDrop<any>, lineIndex: number, slotIndex: number): void {
		this.hoveredSlotId = null;

		if(!this.workingLayout) {
			return;
		}
		const line = this.workingLayout.lines[lineIndex];
		const data = event.item.data;
		const isExistingCell = data && 'formLayoutCellId' in data;

		if(isExistingCell) {
			const draggedCell = data as Cell;
			const sourceLine = this.workingLayout.lines.find(l => l.cells.includes(draggedCell));
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
			const newCell = this.buildTextCell();
			const targetCell = line.cells[slotIndex];
			newCell.colspan = targetCell.colspan ?? 1;
			line.cells[slotIndex] = newCell;
			line.cells = [...line.cells];
		}
		else {
			line.cells[slotIndex] = this.buildCell(data as FieldModel);
			line.cells = [...line.cells];
		}
		this.rebalanceLine(this.workingLayout!.lines[lineIndex]);
		this.markChanged();
	}

	onClickFieldInPalette(fieldModel: FieldModel): void {
		if(!this.workingLayout) {
			return;
		}
		let targetIndex = this.selectedLineIndex ?? this.workingLayout.lines.length - 1;
		if(targetIndex < 0) {
			this.addLine();
			targetIndex = 0;
		}
		if(this.isLineFull(targetIndex)) {
			this.snackBar.open('Line is full — add a new line or increase column count', 'Close', {duration: 3000});
			return;
		}
		const line = this.workingLayout.lines[targetIndex];
		const spacerIndex = line.cells.findIndex(c => this.isSpacerCell(c));
		if(spacerIndex === -1) {
			return;
		}
		const newCell = this.buildCell(fieldModel);
		line.cells[spacerIndex] = newCell;
		line.cells = [...line.cells];
		this.selectedCell = newCell;
		this.selectedLineIndex = targetIndex;
		this.markChanged();
	}

	onClickTextBlockInPalette(): void {
		if(!this.workingLayout) {
			return;
		}
		let targetIndex = this.selectedLineIndex ?? this.workingLayout.lines.length - 1;
		if(targetIndex < 0) {
			this.addLine();
			targetIndex = 0;
		}
		if(this.isLineFull(targetIndex)) {
			this.snackBar.open('Line is full — add a new line or increase column count', 'Close', {duration: 3000});
			return;
		}
		const line = this.workingLayout.lines[targetIndex];
		const spacerIndex = line.cells.findIndex(c => this.isSpacerCell(c));
		if(spacerIndex === -1) {
			return;
		}
		const newCell = this.buildTextCell();
		newCell.colspan = line.cells[spacerIndex].colspan ?? 1;
		line.cells[spacerIndex] = newCell;
		line.cells = [...line.cells];
		this.selectedCell = newCell;
		this.selectedLineIndex = targetIndex;
		this.markChanged();
	}

	onCellClick(cell: Cell, lineIndex: number): void {
		if(this.resizeMoved) {
			this.resizeMoved = false;
			return;
		}
		if(!this.isSpacerCell(cell)) {
			this.selectCell(cell, lineIndex);
		}
	}

	isResizingCell(cell: Cell): boolean {
		return this.resizing && this.resizeCell === cell;
	}

	onResizeStart(event: MouseEvent, cell: Cell, lineIndex: number, direction: 'left' | 'right'): void {
		event.stopPropagation();
		event.preventDefault();
		this.resizing = true;
		this.resizeMoved = false;
		this.resizeCell = cell;
		this.resizeLineIndex = lineIndex;
		this.resizeStartX = event.clientX;
		this.resizeStartColspan = cell.colspan ?? 1;
		this.resizeDirection = direction;
	}

	@HostListener('document:mousemove', ['$event'])
	onResizeMove(event: MouseEvent): void {
		if(!this.resizing || !this.resizeCell || this.resizeLineIndex === null) {
			return;
		}

		const colWidth = this.getEffectiveCellWidth(this.resizeCell, this.resizeLineIndex) / (this.resizeCell.colspan ?? 1);
		const deltaX = event.clientX - this.resizeStartX;
		const sign = this.resizeDirection === 'right' ? 1 : -1;
		const deltaCols = Math.round((deltaX * sign) / colWidth);
		const newColspan = Math.min(Math.max(1, this.resizeStartColspan + deltaCols), this.columnCount);

		if(newColspan !== this.resizeCell.colspan) {
			this.resizeMoved = true;
			this.resizeCell.colspan = newColspan;
			const line = this.workingLayout!.lines[this.resizeLineIndex];
			this.rebalanceLine(line);
			this.markChanged();
		}
	}

	@HostListener('document:mouseup')
	onResizeEnd(): void {
		this.resizing = false;
		this.resizeCell = null;
		this.resizeLineIndex = null;
	}

	selectCell(cell: Cell, lineIndex: number): void {
		this.selectedCell = cell;
		this.selectedLineIndex = lineIndex;
		this.openCellDialog(cell, lineIndex);
	}

	private openCellDialog(cell: Cell, lineIndex: number): void {
		const ref = this.dialog.open(FormLayoutCellDialogComponent, {
			data: {
				cell,
				columnCount: this.columnCount,
				allLayouts: this.allLayouts,
				projectLanguages: this.projectLanguages,
				effectiveCellWidth: this.getEffectiveCellWidth(cell, lineIndex)
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
				const line = this.workingLayout!.lines[lineIndex];
				this.rebalanceLine(line);
			}
			this.markChanged();
		});
	}

	isCellSelected(cell: Cell): boolean {
		return this.selectedCell === cell;
	}

	isLineSelected(lineIndex: number): boolean {
		return this.selectedLineIndex === lineIndex;
	}

	parseWidthPx(cssCode: string | null | undefined): number | null {
		const match = cssCode?.match(/width:\s*(\d+)px/);
		return match ? parseInt(match[1]) : null;
	}

	getEffectiveCellWidth(cell: Cell, lineIndex?: number): number {
		const idx = lineIndex ?? this.selectedLineIndex;
		if(!this.workingLayout || idx === null) {
			return 250;
		}
		const startCol = this.getCellStartColumn(cell, idx);
		const colspan = cell.colspan ?? 1;
		let total = 0;
		for(let i = startCol; i < startCol + colspan && i < this.workingLayout.columns.length; i++) {
			total += this.getColumnWidth(i);
		}
		return total || 250;
	}

	openSettings(): void {
		if(!this.workingLayout) {
			return;
		}
		const ref = this.dialog.open(FormLayoutCreateDialogComponent, {
			data: {
				projectId: this.projectId,
				formModelId: this.formModelId,
				generateFromDataset: false,
				layout: this.workingLayout
			},
			panelClass: 'rodano-dialog'
		});
		ref.afterClosed().subscribe(result => {
			if(!result || !this.workingLayout) {
				return;
			}
			Object.assign(this.workingLayout, result);
			this.markChanged();
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

	protected markChanged(): void {
		this.hasChanges = true;
		if(this.workingLayout) {
			const cleaned = JSON.parse(JSON.stringify(this.workingLayout));
			for(const line of cleaned.lines) {
				while(line.cells.length > 0 && line.cells[line.cells.length - 1].fieldModelId === '__SPACER__') {
					line.cells.pop();
				}
			}
			this.layoutChanged.emit(cleaned);
		}
	}

	onSlotEntered(slotId: string): void {
		this.hoveredSlotId = slotId;
	}

	onSlotExited(slotId: string): void {
		if(this.hoveredSlotId === slotId) {
			this.hoveredSlotId = null;
		}
	}
}
