import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {Layout} from '@core/model/layout';
import {LanguageService} from '../../services/language.service';
import {FieldModelManagerService} from '../../services/manager/field-model-manager.service';
import {FormLayoutManagerService} from '../../services/manager/form-layout-manager.service';
import {LayoutType} from '@core/model/layout-type';
import {CellLoadingService} from '../../../crf/services/cell-loading.service';
import {VisibilityService} from '../../../crf/services/visibility.service';
import {FieldUpdateService} from '../../../crf/services/field-update.service';
import {LayoutComponent} from '../../../crf/layout/layout.component';
import {CRFDataset} from '../../../crf/models/crf-dataset';
import {CRFField} from '../../../crf/models/crf-field';
import {FieldModel} from '@core/model/field-model';
import {PreviewMultipleLayoutComponent} from './preview-multiple-layout.component';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';

@Component({
	selector: 'app-form-layout-preview',
	standalone: true,
	templateUrl: './form-layout-preview.component.html',
	styleUrls: ['./form-layout-preview.component.css'],
	imports: [CommonModule, MatIconModule, MatTooltipModule, LayoutComponent, PreviewMultipleLayoutComponent],
	providers: [CellLoadingService, FieldUpdateService, VisibilityService]
})
export class FormLayoutPreviewComponent implements OnInit, OnChanges {
	@Input() projectId = '';
	@Input() formModelId = '';
	@Output() closed = new EventEmitter<void>();

	datasets: CRFDataset[] = [];
	readonly layoutType = LayoutType;
	readonly rulerTicks = [0, 250, 500, 750, 1000, 1250];

	applyVisibilityRules = false;
	rebuilding = false;

	enrichedLayouts: Layout[] = [];

	constructor(
		public languageService: LanguageService,
		public formLayoutManager: FormLayoutManagerService,
		public fieldModelManager: FieldModelManagerService,
		public datasetModelManager: DatasetModelManagerService,
		private cellLoadingService: CellLoadingService,
		private visibilityService: VisibilityService
	) {}

	ngOnInit(): void {
		this.enrichedLayouts = this.buildEnrichedLayouts();
		this.buildDatasets();

		this.cellLoadingService.allCellsLoaded$.subscribe(() => {
			if(!this.applyVisibilityRules) {
				setTimeout(() => this.forceShowAll());
			}
		});
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['formModelId'] && this.formModelId) {
			this.enrichedLayouts = this.buildEnrichedLayouts();
			this.rebuildDatasets();
		}
	}

	get layouts(): Layout[] {
		return this.enrichedLayouts;
	}

	private buildEnrichedLayouts(): Layout[] {
		return this.formLayoutManager.getAll().map(layout => this.enrichLayout(layout));
	}

	private enrichLayout(layout: Layout): Layout {
		if(!layout.datasetModel?.datasetModelId) {
			return layout;
		}
		const datasetModel = this.datasetModelManager.getById(layout.datasetModel.datasetModelId);
		if(!datasetModel) {
			return layout;
		}
		const fieldModels = this.fieldModelManager.getAll()
			.filter((fm: FieldModel) => fm.datasetModelId === layout.datasetModel.datasetModelId);

		return {
			...layout,
			datasetModel: {
				...layout.datasetModel,
				fieldModels,
				collapsedLabelPattern: layout.datasetModel.collapsedLabelPattern ?? '',
				canWrite: true
			}
		};
	}

	private forceShowAll(): void {
		for(const layout of this.enrichedLayouts) {
			this.visibilityService.triggerLayoutVisibilityEvent(layout.formLayoutId, true);
			for(const line of layout.lines) {
				for(const cell of line.cells) {
					this.visibilityService.triggerCellVisibilityEvent(
						cell.formLayoutCellId,
						layout.formLayoutId,
						true
					);
					this.datasets.forEach(ds => {
						this.visibilityService.triggerCellVisibilityEvent(
							cell.formLayoutCellId,
							`${layout.formLayoutId}_${ds.id}`,
							true
						);
					});
				}
			}
		}
	}

	private buildDatasets(): void {
		const datasetMap = new Map<string, CRFDataset>();

		for(const layout of this.layouts) {
			if(layout.datasetModel) {
				const modelId = layout.datasetModel.datasetModelId;
				if(!datasetMap.has(modelId)) {
					datasetMap.set(modelId, this.createEmptyDataset(
						modelId,
						layout.datasetModel.fieldModels
					));
				}
			}

			for(const line of layout.lines) {
				for(const cell of line.cells) {
					if(!cell.datasetModelId || !cell.fieldModelId) {
						continue;
					}

					if(!datasetMap.has(cell.datasetModelId)) {
						datasetMap.set(cell.datasetModelId, this.createEmptyDataset(cell.datasetModelId, []));
					}

					const dataset = datasetMap.get(cell.datasetModelId)!;
					if(!dataset.fields.find(f => f.modelId === cell.fieldModelId)) {
						const fm = this.fieldModelManager.getById(cell.fieldModelId);
						if(fm) {
							dataset.fields.push(this.createEmptyField(cell.fieldModelId, fm));
						}
					}
				}
			}
		}

		this.datasets = Array.from(datasetMap.values());
		this.cellLoadingService.registerFormCells(this.layouts, this.datasets);
	}

	private createEmptyDataset(modelId: string, fieldModels: FieldModel[]): CRFDataset {
		return {
			pk: undefined as any,
			id: crypto.randomUUID(),
			modelId,
			fields: fieldModels.map(fm => this.createEmptyField(fm.fieldModelId, fm)),
			canWrite: true,
			show: true,
			expanded: false,
			rationale: undefined
		} as CRFDataset;
	}

	private createEmptyField(fieldModelId: string, fm: FieldModel): CRFField {
		return {
			pk: undefined,
			modelId: fieldModelId,
			datasetModelId: '',
			datasetPk: undefined,
			datasetId: '',
			scopePk: undefined,
			scopeId: '',
			scopeCodeAndShortname: '',
			eventPk: undefined,
			value: undefined,
			filePk: undefined,
			valueLabel: '',
			workflowStatuses: [],
			possibleWorkflows: [],
			possibleValues: fm.possibleValues ?? [],
			model: fm,
			shown: true,
			error: undefined
		} as unknown as CRFField;
	}

	getLayoutWidth(): string {
		const max = Math.max(...this.enrichedLayouts.map(l =>
			l.columns.reduce((sum, col) => {
				const match = col.cssCode?.match(/width:\s*(\d+)px/);
				return sum + (match ? parseInt(match[1]) : 250);
			}, 0)
		), 0);
		return max > 0 ? `${max}px` : '100%';
	}

	onClose(): void {
		this.closed.emit();
	}

	toggleVisibility(): void {
		this.applyVisibilityRules = !this.applyVisibilityRules;
		if(!this.applyVisibilityRules) {
			this.forceShowAll();
		}
		else {
			for(const layout of this.enrichedLayouts) {
				for(const line of layout.lines) {
					for(const cell of line.cells) {
						if(cell.datasetModelId && cell.fieldModelId) {
							const dataset = this.datasets.find(d => d.modelId === cell.datasetModelId);
							const field = dataset?.fields.find(f => f.modelId === cell.fieldModelId);
							if(field) {
								this.visibilityService.triggerCriteria(cell, layout.formLayoutId, field);
							}
						}
					}
				}
			}
		}
	}

	resetValues(): void {
		this.applyVisibilityRules = false;
		this.rebuildDatasets();
	}

	private rebuildDatasets(): void {
		this.rebuilding = true;
		this.datasets = [];
		setTimeout(() => {
			this.buildDatasets();
			this.rebuilding = false;
		});
	}
}
