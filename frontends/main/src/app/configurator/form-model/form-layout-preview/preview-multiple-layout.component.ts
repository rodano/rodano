import {Component} from '@angular/core';
import {MultipleLayoutComponent} from '../../../crf/multiple-layout/multiple-layout.component';
import {CRFDataset} from '../../../crf/models/crf-dataset';
import {CRFField} from '../../../crf/models/crf-field';
import {MatTableModule} from '@angular/material/table';
import {MatSortModule} from '@angular/material/sort';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatTooltip} from '@angular/material/tooltip';
import {MatIcon} from '@angular/material/icon';
import {LayoutComponent} from '../../../crf/layout/layout.component';
import {LocalizeMapPipe} from '../../../pipes/localize-map.pipe';
import {SafeHtmlPipe} from '../../../pipes/safe-html.pipe';
import {AuditTrailButtonComponent} from '../../../audit-trail-button/audit-trail-button.component';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {FieldModel} from '@core/model/field-model';

@Component({
	selector: 'app-preview-multiple-layout',
	standalone: true,
	templateUrl: '../../../crf/multiple-layout/multiple-layout.component.html',
	styleUrls: ['../../../crf/multiple-layout/multiple-layout.component.css'],
	animations: [
		trigger('layoutExpand', [
			state('collapsed', style({height: '0', minHeight: '0', marginTop: '0', marginBottom: '0'})),
			state('expanded', style({height: '*', marginTop: '1rem', marginBottom: '1rem'})),
			transition('expanded <=> collapsed', animate('200ms cubic-bezier(0.4, 0.0, 0.2, 1)'))
		])
	],
	imports: [
		MatTableModule,
		MatSortModule,
		MatButton,
		MatIconButton,
		MatTooltip,
		MatIcon,
		SafeHtmlPipe,
		LayoutComponent,
		LocalizeMapPipe,
		AuditTrailButtonComponent
	]
})
export class PreviewMultipleLayoutComponent extends MultipleLayoutComponent {
	override addDataset(): void {
		const newDataset = {
			pk: undefined,
			id: crypto.randomUUID(),
			modelId: this.layout.datasetModel.datasetModelId,
			fields: this.layout.datasetModel.fieldModels.map((fm: FieldModel) => ({
				pk: undefined,
				modelId: fm.fieldModelId,
				datasetModelId: this.layout.datasetModel.datasetModelId,
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
			} as unknown as CRFField)),
			canWrite: true,
			show: true,
			expanded: true,
			rationale: undefined
		} as unknown as CRFDataset;

		this.datasets.push(newDataset);
		this.multipleDatasets.push(newDataset);
		this.dataSource.data = [...this.multipleDatasets];
		this.cellLoadingService.registerLayoutCells(this.layout);
	}

	override getFieldValue(dataset: CRFDataset, fieldModelId: string): string {
		const field = dataset.fields.find(f => f.modelId === fieldModelId);
		return field?.valueLabel ?? '';
	}

	override ngOnChanges(): void {
		if(this.layout.datasetModel && !this.layout.datasetModel.collapsedLabelPattern && this.layout.datasetModel.fieldModels?.length > 0) {
			const cellFieldModelIds = this.layout.lines
				.flatMap(l => l.cells)
				.filter(c => c.fieldModelId)
				.map(c => c.fieldModelId);

			const relevantFieldModels = this.layout.datasetModel.fieldModels
				.filter((fm: FieldModel) => cellFieldModelIds.includes(fm.fieldModelId));

			this.layout = {
				...this.layout,
				datasetModel: {
					...this.layout.datasetModel,
					collapsedLabelPattern: relevantFieldModels
						.map((fm: FieldModel) => `\${fieldModelId:${fm.fieldModelId}}`)
						.join(' ')
				}
			};
		}
		super.ngOnChanges();
	}

	override removeDataset(dataset: CRFDataset): void {
		const datasetsIdx = this.datasets.indexOf(dataset);
		if(datasetsIdx !== -1) {
			this.datasets.splice(datasetsIdx, 1);
		}
		const multipleIdx = this.multipleDatasets.indexOf(dataset);
		if(multipleIdx !== -1) {
			this.multipleDatasets.splice(multipleIdx, 1);
		}
		this.dataSource.data = [...this.multipleDatasets];
	}
}
