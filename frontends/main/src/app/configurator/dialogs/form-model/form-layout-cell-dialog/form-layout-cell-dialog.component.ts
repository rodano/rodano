import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatSelectModule} from '@angular/material/select';
import {MatTooltipModule} from '@angular/material/tooltip';
import {Subscription} from 'rxjs';
import {Cell} from '@core/model/cell';
import {Layout} from '@core/model/layout';
import {FieldModel} from '@core/model/field-model';
import {ProjectLanguage} from '@core/model/project-language';
import {FieldModelType} from '@core/model/field-model-type';
import {Operator} from '@core/model/operator';
import {VisibilityCriteria} from '@core/model/visibility-criteria';
import {VisibilityCriterionAction} from '@core/model/visibility-criterion-action';
import {LanguageService} from '../../../services/language.service';
import {FieldModelManagerService} from '../../../services/manager/field-model-manager.service';
import {DatasetModelManagerService} from '../../../services/manager/dataset-model-manager.service';
import {MatTabsModule} from '@angular/material/tabs';
import {DualListBoxComponent} from '../../dual-list-box/dual-list-box.component';

export interface FormLayoutCellDialogData {
	cell: Cell;
	columnCount: number;
	allLayouts: Layout[];
	projectLanguages: ProjectLanguage[];
	effectiveCellWidth: number;
}

@Component({
	selector: 'app-form-layout-cell-dialog',
	standalone: true,
	templateUrl: './form-layout-cell-dialog.component.html',
	styleUrls: ['./form-layout-cell-dialog.component.css'],
	imports: [
		CommonModule, ReactiveFormsModule, FormsModule, MatDialogModule, MatIconModule, MatButtonModule, MatCheckboxModule,
		MatSelectModule, MatTooltipModule, MatTabsModule, DualListBoxComponent]
})
export class FormLayoutCellDialogComponent implements OnInit, OnDestroy {
	cell: Cell;
	form!: FormGroup;
	availableLanguages: ProjectLanguage[] = [];

	cellTextBefore: Record<string, string> = {};
	cellTextAfter: Record<string, string> = {};

	private formSub: Subscription | null = null;

	constructor(
		private fb: FormBuilder,
		public languageService: LanguageService,
		private fieldModelManager: FieldModelManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private dialogRef: MatDialogRef<FormLayoutCellDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: FormLayoutCellDialogData
	) {
		this.cell = JSON.parse(JSON.stringify(data.cell));
	}

	ngOnInit(): void {
		this.availableLanguages = this.data.projectLanguages?.length
			? this.data.projectLanguages
			: [{languageCode: 'en', isDefault: true}];

		this.cellTextBefore = this.cell.textBefore ? {...this.cell.textBefore} : {};
		this.cellTextAfter = this.cell.textAfter ? {...this.cell.textAfter} : {};
		this.initForm();
	}

	ngOnDestroy(): void {
		this.formSub?.unsubscribe();
	}

	private initForm(): void {
		if(this.isTextCell(this.cell)) {
			this.form = this.fb.group({colspan: [this.cell.colspan ?? 1]});
		}
		else {
			this.form = this.fb.group({
				fieldModelId: [this.cell.fieldModelId || ''],
				colspan: [this.cell.colspan ?? 1],
				displayLabel: [this.cell.displayLabel ?? false],
				displayPossibleValueLabels: [this.cell.displayPossibleValueLabels ?? false],
				hasPrintButton: [this.cell.hasPrintButton ?? false],
				possibleValuesColumnNumber: [this.cell.possibleValuesColumnNumber ?? null],
				possibleValuesColumnWidth: [this.cell.possibleValuesColumnWidth ?? null]
			});
		}

		this.formSub = this.form.valueChanges.subscribe(v => {
			if(v.fieldModelId && v.fieldModelId !== this.cell.fieldModelId) {
				const fm = this.fieldModelManager.getById(v.fieldModelId);
				if(fm) {
					this.cell.datasetModelId = fm.datasetModelId;
				}
			}
			Object.assign(this.cell, v);
		});
	}

	get hasMultipleLayouts(): boolean {
		return this.data.allLayouts.length > 1;
	}

	get hasMultipleCells(): boolean {
		return this.getCellsInSameLayout().length > 0;
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
			const ds = this.datasetModelManager.getById(datasetModelId);
			groups.push({
				datasetModelId,
				label: ds ? (this.languageService.getTranslatedName(ds.shortname) || ds.id) : datasetModelId,
				fields
			});
		});
		return groups.sort((a, b) => a.label.localeCompare(b.label));
	}

	getCellsInSameLayout(): Cell[] {
		const currentLayout = this.data.allLayouts.find(l =>
			l.lines.some(line => line.cells.some(c => c.formLayoutCellId === this.data.cell.formLayoutCellId))
		);
		if(!currentLayout) {
			return [];
		}
		return currentLayout.lines
			.flatMap(l => l.cells)
			.filter(c =>
				c.formLayoutCellId !== this.data.cell.formLayoutCellId
				&& c.fieldModelId
				&& c.fieldModelId !== '__SPACER__'
				&& c.fieldModelId !== ''
			);
	}

	getLayoutLabel(layout: Layout): string {
		return layout.id;
	}

	getCellLabel(cell: Cell): string {
		if(cell.id) {
			return cell.id;
		}
		const fm = this.fieldModelManager.getById(cell.fieldModelId ?? '');
		return fm ? this.languageService.getLabel(fm) : cell.formLayoutCellId;
	}

	private getCurrentLayoutId(): string | null {
		return this.data.allLayouts.find(l =>
			l.lines.some(line => line.cells.some(c => c.formLayoutCellId === this.data.cell.formLayoutCellId))
		)?.formLayoutId ?? null;
	}

	getAvailableTargetLayouts(criterion: VisibilityCriteria): Layout[] {
		const currentLayoutId = this.getCurrentLayoutId();
		return this.data.allLayouts.filter(l =>
			l.formLayoutId !== currentLayoutId
			&& !criterion.targetLayoutIds.includes(l.formLayoutId)
		);
	}

	getSelectedTargetLayouts(criterion: VisibilityCriteria): Layout[] {
		const currentLayoutId = this.getCurrentLayoutId();
		return this.data.allLayouts.filter(l =>
			l.formLayoutId !== currentLayoutId
			&& criterion.targetLayoutIds.includes(l.formLayoutId)
		);
	}

	onAddTargetLayout(criterion: VisibilityCriteria, layout: Layout): void {
		criterion.targetLayoutIds = [...criterion.targetLayoutIds, layout.formLayoutId];
	}

	onRemoveTargetLayout(criterion: VisibilityCriteria, layout: Layout): void {
		criterion.targetLayoutIds = criterion.targetLayoutIds.filter(id => id !== layout.formLayoutId);
	}

	getAvailableTargetCells(criterion: VisibilityCriteria): Cell[] {
		return this.getCellsInSameLayout().filter(c =>
			!criterion.targetCellIds.includes(c.formLayoutCellId)
		);
	}

	getSelectedTargetCells(criterion: VisibilityCriteria): Cell[] {
		return this.getCellsInSameLayout().filter(c =>
			criterion.targetCellIds.includes(c.formLayoutCellId)
		);
	}

	onAddTargetCell(criterion: VisibilityCriteria, cell: Cell): void {
		criterion.targetCellIds = [...criterion.targetCellIds, cell.formLayoutCellId];
	}

	onRemoveTargetCell(criterion: VisibilityCriteria, cell: Cell): void {
		criterion.targetCellIds = criterion.targetCellIds.filter(id => id !== cell.formLayoutCellId);
	}

	isTextCell(cell: Cell): boolean {
		return !cell.fieldModelId || cell.fieldModelId === '';
	}

	getFieldModelById(fieldModelId: string): FieldModel | undefined {
		return this.fieldModelManager.getById(fieldModelId);
	}

	getText(map: Record<string, string>, langCode: string): string {
		return map[langCode] || '';
	}

	onTextBeforeChange(event: Event, langCode: string): void {
		this.cellTextBefore[langCode] = (event.target as HTMLTextAreaElement).value;
		this.cell.textBefore = {...this.cellTextBefore};
	}

	onTextAfterChange(event: Event, langCode: string): void {
		this.cellTextAfter[langCode] = (event.target as HTMLTextAreaElement).value;
		this.cell.textAfter = {...this.cellTextAfter};
	}

	canHaveVisibilityCriteria(cell: Cell): boolean {
		const fm = this.fieldModelManager.getById(cell.fieldModelId ?? '');
		if(!fm) {
			return false;
		}
		const supported = [
			FieldModelType.SELECT, FieldModelType.RADIO,
			FieldModelType.CHECKBOX_GROUP, FieldModelType.AUTO_COMPLETION,
			FieldModelType.CHECKBOX, FieldModelType.NUMBER
		];
		return supported.includes(fm.type as FieldModelType);
	}

	fieldUsesValueDropdown(fm: FieldModel): boolean {
		return [FieldModelType.SELECT, FieldModelType.RADIO,
			FieldModelType.CHECKBOX_GROUP, FieldModelType.AUTO_COMPLETION
		].includes(fm.type as FieldModelType);
	}

	fieldUsesCheckboxBoolean(fm: FieldModel): boolean {
		return fm.type === FieldModelType.CHECKBOX;
	}

	fieldUsesOperator(fm: FieldModel): boolean {
		return fm.type === FieldModelType.NUMBER;
	}

	getOperatorsForField(): {value: Operator; label: string}[] {
		return [
			{value: Operator.EQUALS, label: 'Equals'},
			{value: Operator.NOT_EQUALS, label: 'Not equals'},
			{value: Operator.GREATER, label: 'Greater than'},
			{value: Operator.GREATER_EQUALS, label: 'Greater or equal'},
			{value: Operator.LOWER, label: 'Lower than'},
			{value: Operator.LOWER_EQUALS, label: 'Lower or equal'},
			{value: Operator.NULL, label: 'Is null'},
			{value: Operator.NOT_NULL, label: 'Is not null'}
		];
	}

	operatorNeedsValue(operator: Operator): boolean {
		return ![Operator.NULL, Operator.NOT_NULL].includes(operator);
	}

	addCriterion(): void {
		const fm = this.fieldModelManager.getById(this.cell.fieldModelId ?? '');
		const criterion: VisibilityCriteria = {
			formCellVisibilityCriteriaId: '',
			operator: fm && this.fieldUsesOperator(fm) ? Operator.EQUALS : null as any,
			values: [],
			action: VisibilityCriterionAction.SHOW,
			targetLayoutIds: [],
			targetCellIds: []
		};
		this.cell.visibilityCriteria = [...(this.cell.visibilityCriteria ?? []), criterion];
	}

	removeCriterion(index: number): void {
		this.cell.visibilityCriteria = this.cell.visibilityCriteria.filter((_, i) => i !== index);
	}

	getCriterionValue(criterion: VisibilityCriteria): string {
		return criterion.values[0] ?? '';
	}

	setCriterionValue(criterion: VisibilityCriteria, value: string): void {
		criterion.values = value ? [value] : [];
	}

	setCriterionOperator(criterion: VisibilityCriteria, operator: Operator): void {
		criterion.operator = operator;
		if(!this.operatorNeedsValue(operator)) {
			criterion.values = [];
		}
	}

	getLanguageLabel(code: string, isDefault: boolean): string {
		try {
			const name = new Intl.DisplayNames(['en'], {type: 'language'}).of(code) || code.toUpperCase();
			return isDefault ? `${name} ☆` : name;
		}
		catch {
			return code.toUpperCase();
		}
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}

	onSave(): void {
		this.cell.textBefore = {...this.cellTextBefore};
		this.cell.textAfter = {...this.cellTextAfter};
		this.dialogRef.close(this.cell);
	}
}
