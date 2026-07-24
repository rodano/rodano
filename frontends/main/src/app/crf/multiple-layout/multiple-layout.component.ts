import {Component, DestroyRef, OnInit, ViewChild, effect, input, signal} from '@angular/core';
import {Layout} from '@core/model/layout';
import {MatSortModule, Sort} from '@angular/material/sort';
import {MatTable, MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatIcon} from '@angular/material/icon';
import {MatTooltip} from '@angular/material/tooltip';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatDialog} from '@angular/material/dialog';
import {NotificationService} from '../../services/notification.service';
import {CellLoadingService} from '../services/cell-loading.service';
import {VisibilityService} from '../services/visibility.service';
import {CRFService} from '../services/crf.service';
import {FieldModel} from '@core/model/field-model';
import {CRFDataset} from '../models/crf-dataset';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {Observable} from 'rxjs';
import {DeleteRestoreComponent} from '../dialogs/delete-restore/delete-restore.component';
import {NoDateFormatError} from '../errors/NoDateFormatError';
import {LayoutComponent} from '../layout/layout.component';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {CRFField} from '../models/crf-field';
import {LoggingService} from '@core/services/logging.service';
import {parse} from 'date-fns';
import {AuditTrailButtonComponent} from '../../audit-trail-button/audit-trail-button.component';
import {FieldModelType} from '@core/model/field-model-type';
import {FieldService} from '@core/services/field.service';
import {EmptyObjectCheck} from '../../utils/empty-object-check';
import {SafeHtmlPipe} from '../../pipes/safe-html.pipe';

@Component({
	selector: 'app-multiple-layout',
	templateUrl: './multiple-layout.component.html',
	styleUrls: ['./multiple-layout.component.css'],
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
export class MultipleLayoutComponent implements OnInit {
	readonly layout = input.required<Layout>();
	//this component must have a reference to the reference list of datasets to be able to push new datasets in it
	//if a filtered list is provided, new datasets will not be visible by the parent component
	readonly datasets = input.required<CRFDataset[]>();
	readonly disabled = input<boolean>(false);

	multipleDatasets: CRFDataset[] = [];

	@ViewChild(MatTable) table: MatTable<any>;
	readonly fieldModelsToDisplay = signal<FieldModel[]>([]);
	readonly columnsToDisplay = signal<string[]>([]);
	//handle datasource manually to be able to refresh it properly
	readonly dataSource = signal(new MatTableDataSource<CRFDataset>([]));

	readonly shown = signal(true);

	constructor(
		private crfService: CRFService,
		private visibilityService: VisibilityService,
		private cellLoadingService: CellLoadingService,
		private notificationService: NotificationService,
		private fieldService: FieldService,
		private loggingService: LoggingService,
		private dialog: MatDialog,
		private destroyRef: DestroyRef
	) {
		effect(() => {
			const layout = this.layout();
			const datasets = this.datasets();
			const fieldModelsToDisplay: FieldModel[] = [];
			layout.datasetModel?.meaningfulFieldModelIds?.forEach(fieldModelId => {
				const fieldModel = layout.datasetModel.fieldModels.find(a => a.id === fieldModelId) as FieldModel;
				fieldModelsToDisplay.push(fieldModel);
			});
			this.fieldModelsToDisplay.set(fieldModelsToDisplay);
			this.columnsToDisplay.set([...fieldModelsToDisplay.map(f => f.id), 'actions']);
			this.multipleDatasets = datasets.filter(d => d.modelId === layout.datasetModel.id);
			if(fieldModelsToDisplay.length > 0) {
				this.sortDatasets(fieldModelsToDisplay[0], true);
			}
			this.dataSource.set(new MatTableDataSource<CRFDataset>(this.multipleDatasets));
		});
	}

	ngOnInit() {
		this.visibilityService.layoutVisibilityEvents$(this.layout().id).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(shown => {
			this.loggingService.info(`Multiple layout ${this.layout().id} receiving visibility event containing ${shown}`);
			this.shown.set(shown);
			//mark the datasets
			this.multipleDatasets.forEach(d => d.show = shown);
		});
	}

	trackBy(_: number, dataset: CRFDataset) {
		return dataset.id;
	}

	hasError(dataset: CRFDataset): boolean {
		return dataset.fields.some(f => f.error() || f.workflowStatuses.some(s => s.state.important));
	}

	getFieldValue(dataset: CRFDataset, fieldModelId: string): string {
		const field = dataset.fields.find(f => f.modelId === fieldModelId) as CRFField;
		return field.valueLabel ?? '';
	}

	addDataset() {
		const layout = this.layout();
		//remember that forms attached to events can display content from datasets attached to the scope
		//in that case, the candidate datasets should be "asked" for the scope, not for the event, even if the eventPk is available
		const eventPk = layout.datasetModel.scopeDocumentation ? undefined : layout.eventPk;
		this.crfService.getCandidateCRFDataset(layout.scopePk, eventPk, layout.datasetModel.id).subscribe(newDataset => {
			newDataset.expanded = true;
			this.datasets().push(newDataset);
			this.multipleDatasets.push(newDataset);
			this.dataSource()._updateChangeSubscription();
			//this.crfService.addDataset(newDataset);
			this.cellLoadingService.registerLayoutCells(layout);
		});
	}

	removeDataset(dataset: CRFDataset) {
		//if the dataset does not have a pk yet, that means that it has not been uploaded yet
		//we can thus delete it permanently
		if(!dataset.pk) {
			const datasets = this.datasets();
			datasets.splice(datasets.indexOf(dataset), 1);
		}
		else {
			this.openRationaleDialog(true).subscribe((rationale?: string) => {
				if(rationale) {
					dataset.rationale = rationale;
					dataset.removed = true;
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
				this.notificationService.showSuccess('Dataset marked for restoration');
			}
		});
	}

	private openRationaleDialog(deletion: boolean): Observable<string | undefined> {
		const entityName = new LocalizeMapPipe().transform(this.layout().datasetModel.shortname);
		return this.dialog
			.open(DeleteRestoreComponent, {data: {deletion, entityName}})
			.afterClosed();
	}

	sortDatasets(sortField: FieldModel, direction: boolean) {
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
		const sortField = this.layout().datasetModel.fieldModels.find(f => f.id === sort.active) as FieldModel;
		this.sortDatasets(sortField, sort.direction === 'asc');

		//re-render the rows after the sorting is done
		this.table.renderRows();
	}

	compareFieldValues(field: FieldModel, v1: string | undefined, v2: string | undefined) {
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
