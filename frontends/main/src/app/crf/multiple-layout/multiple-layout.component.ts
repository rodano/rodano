import {Component, DestroyRef, Input, OnChanges, OnInit, ViewChild} from '@angular/core';
import {LayoutDTO} from '@core/model/layout-dto';
import {trigger, state, style, transition, animate} from '@angular/animations';
import {MatSortModule, Sort} from '@angular/material/sort';
import {MatTable, MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatIcon} from '@angular/material/icon';
import {MatTooltip} from '@angular/material/tooltip';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatDialog} from '@angular/material/dialog';
import {NotificationService} from 'src/app/services/notification.service';
import {CellLoadingService} from '../services/cell-loading.service';
import {VisibilityService} from '../services/visibility.service';
import {CRFService} from '../services/crf.service';
import {FieldModelDTO} from '@core/model/field-model-dto';
import {CRFDataset} from '../models/crf-dataset';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {Observable} from 'rxjs';
import {DeleteRestoreComponent} from '../dialogs/delete-restore/delete-restore.component';
import {NoDateFormatError} from '../errors/NoDateFormatError';
import {LayoutComponent} from '../layout/layout.component';
import {LocalizeMapPipe} from 'src/app/pipes/localize-map.pipe';
import {CRFField} from '../models/crf-field';
import {LoggingService} from '@core/services/logging.service';
import {parse} from 'date-fns';
import {AuditTrailButtonComponent} from 'src/app/audit-trail-button/audit-trail-button.component';
import {FieldModelType} from '@core/model/field-model-type';
import {FieldService} from '@core/services/field.service';
import {EmptyObjectCheck} from 'src/app/utils/empty-object-check';
import {SafeHtmlPipe} from 'src/app/pipes/safe-html.pipe';

@Component({
	selector: 'app-multiple-layout',
	templateUrl: './multiple-layout.component.html',
	styleUrls: ['./multiple-layout.component.css'],
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
export class MultipleLayoutComponent implements OnInit, OnChanges {
	@Input() layout: LayoutDTO;
	//this component must have a reference to the reference list of datasets to be able to push new datasets in it
	//if a filtered list is provided, new datasets will not be visible by the parent component
	@Input() datasets: CRFDataset[];
	@Input() disabled: boolean;

	multipleDatasets: CRFDataset[] = [];

	@ViewChild(MatTable) table: MatTable<any>;
	fieldModelsToDisplay: FieldModelDTO[] = [];
	columnsToDisplay: string[] = [];
	//handle datasource manually to be able to refresh it properly
	dataSource = new MatTableDataSource<CRFDataset>([]);

	shown = true;

	constructor(
		private crfService: CRFService,
		private visibilityService: VisibilityService,
		private cellLoadingService: CellLoadingService,
		private notificationService: NotificationService,
		private fieldService: FieldService,
		private loggingService: LoggingService,
		private dialog: MatDialog,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		//visibility criteria
		/*this.visibilityService.layoutCriterionEvents$(this.layout.id).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(criterion => {
			this.loggingService.info(`Multiple layout ${this.layout.id} receiving criterion`, criterion);
			const show = criterion.action.toLocaleLowerCase() === VisibilityCriteriaDTO.ActionEnum.SHOW.toLocaleLowerCase();
			this.shown = criterion.reverse ? !show : show;

			//mark the datasets
			this.datasets.forEach(d => d.show = this.shown);
			//this.crfService.mergeCurrentDatasets(this.datasets);
		});*/

		this.visibilityService.layoutVisibilityEvents$(this.layout.id).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(shown => {
			this.loggingService.info(`Multiple layout ${this.layout.id} receiving visibility event containing ${shown}`);
			this.shown = shown;
			//mark the datasets
			this.multipleDatasets.forEach(d => d.show = shown);
		});

		/*this.fieldUpdateService.datasetFieldUpdated$(this.layout.datasetModel.id).subscribe(() => {
			console.log(this.multipleDatasets.flatMap(d => d.fields));
			this.table.renderRows();
		});*/
	}

	ngOnChanges() {
		this.fieldModelsToDisplay = [];
		this.layout.datasetModel?.collapsedLabelPattern?.split('${').forEach(element => {
			if(element !== '') {
				element = element.trim();
				if(element.startsWith('fieldModelId:')) {
					const fieldModelId = element.slice(13, -1);
					const fieldModel = this.layout.datasetModel.fieldModels.find(a => a.id === fieldModelId) as FieldModelDTO;
					this.fieldModelsToDisplay.push(fieldModel);
				}
			}
		});
		this.columnsToDisplay = [...this.fieldModelsToDisplay.map(f => f.id), 'actions'];
		this.multipleDatasets = this.datasets.filter(d => d.modelId === this.layout.datasetModel.id);
		if(this.fieldModelsToDisplay.length > 0) {
			this.sortDatasets(this.fieldModelsToDisplay[0], true);
		}
		this.dataSource = new MatTableDataSource<CRFDataset>(this.multipleDatasets);
	}

	trackBy(_: number, dataset: CRFDataset) {
		return dataset.id;
	}

	hasError(dataset: CRFDataset): boolean {
		return dataset.fields.some(f => f.error || f.workflowStatuses.some(s => s.state.important));
	}

	getFieldValue(dataset: CRFDataset, fieldModelId: string): string {
		const field = dataset.fields.find(f => f.modelId === fieldModelId) as CRFField;
		return field.valueLabel ?? '';
	}

	addDataset() {
		this.crfService.getCandidateCRFDataset(this.layout.scopePk, this.layout.eventPk, this.layout.datasetModel.id).subscribe(newDataset => {
			newDataset.expanded = true;
			this.datasets.push(newDataset);
			this.multipleDatasets.push(newDataset);
			this.dataSource._updateChangeSubscription();
			//this.crfService.addDataset(newDataset);
			this.cellLoadingService.registerLayoutCells(this.layout);
		});
	}

	removeDataset(dataset: CRFDataset) {
		//if the dataset does not have a pk yet, that means that it has not been uploaded yet
		//we can thus delete it permanently.
		if(!dataset.pk) {
			this.datasets.splice(this.datasets.indexOf(dataset), 1);
			//this.crfService.removeUncommitedDataset(dataset.id);
		}
		else {
			this.openRationaleDialog(true).subscribe((rationale?: string) => {
				if(rationale) {
					dataset.rationale = rationale;
					dataset.removed = true;
					//this.crfService.mergeCurrentDatasets([dataset]);
					this.notificationService.showSuccess('Dataset marked for deletion');
				}
			});
		}
	}

	restoreDataset(dataset: CRFDataset) {
		this.openRationaleDialog(false).subscribe((rationale?: string) => {
			if(rationale) {
				dataset.rationale = rationale;
				dataset.removed = false;
				//this.crfService.mergeCurrentDatasets([dataset]);
				this.notificationService.showSuccess('Dataset marked for restoration');
			}
		});
	}

	private openRationaleDialog(deletion: boolean): Observable<string | undefined> {
		return this.dialog
			.open(DeleteRestoreComponent, {data: deletion})
			.afterClosed();
	}

	sortDatasets(sortField: FieldModelDTO, direction: boolean) {
		let comparator: (d1: CRFDataset, d2: CRFDataset) => number;
		if(sortField) {
			comparator = (d1, d2) => {
				const v1 = d1.fields.find(f => f.modelId === sortField.id)?.value;
				const v2 = d2.fields.find(f => f.modelId === sortField.id)?.value;
				const comparison = this.compareFieldValues(sortField, v1, v2);
				return direction ? comparison : -comparison;
			};
		}
		else {
			comparator = (d1, d2) => d1.id.localeCompare(d2.id);
		}
		this.multipleDatasets.sort(comparator);
	}

	sortChange(sort: Sort) {
		if(!sort.active && sort.direction === '') {
			return;
		}
		const sortField = this.layout.datasetModel.fieldModels.find(f => f.id === sort.active) as FieldModelDTO;
		this.sortDatasets(sortField, sort.direction === 'asc');

		//re-render the rows after the sorting is done
		this.table.renderRows();
	}

	compareFieldValues(field: FieldModelDTO, v1: string | undefined, v2: string | undefined) {
		//manage undefined values
		if(!v1) {
			return 1;
		}
		else if(!v2) {
			return -1;
		}
		//compare based on types
		switch(field.type) {
			case FieldModelType.DATE:
			case FieldModelType.DATE_SELECT: {
				const dateFormat = this.fieldService.generateFormat(field);
				if(!dateFormat) {
					throw new NoDateFormatError();
				}

				const date1 = parse(v1, dateFormat, new Date());
				const date2 = parse(v2, dateFormat, new Date());
				return date1.getTime() - date2.getTime();
			}
			default:
				return v1.localeCompare(v2);
		}
	}

	isEmptyObject(object: any): boolean {
		return EmptyObjectCheck.isEmptyObject(object);
	}
}
