import {Component, DestroyRef, OnInit, computed, input, model, signal} from '@angular/core';
import {Layout} from '@core/model/layout';
import {MatSortModule, Sort} from '@angular/material/sort';
import {MatTableModule} from '@angular/material/table';
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

interface DatasetSort {
	field?: FieldModel;
	ascending: boolean;
}

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
	//this component must have access to the whole list of datasets and be able to push new datasets in it
	//if only a filtered list is provided, new datasets will not be visible by the parent component
	readonly datasets = model.required<CRFDataset[]>();
	readonly disabled = input<boolean>(false);

	readonly shown = signal(true);
	private readonly sort = signal<DatasetSort>({ascending: true});

	readonly fieldModelsToDisplay = computed(() => {
		const layout = this.layout();
		return (layout.datasetModel?.meaningfulFieldModelIds ?? [])
			.map(fieldModelId => layout.datasetModel.fieldModels.find(f => f.id === fieldModelId) as FieldModel);
	});

	readonly columnsToDisplay = computed(() => [...this.fieldModelsToDisplay().map(f => f.id), 'actions']);

	//datasets relevant to this layout, filtered and sorted
	//fully derived from the datasets model and the sort state, so any addition is reflected
	readonly multipleDatasets = computed(() => {
		const layout = this.layout();
		const {field, ascending} = this.sort();
		const sortField = field ?? this.fieldModelsToDisplay()[0];
		return this.datasets()
			.filter(d => d.modelId === layout.datasetModel.id)
			.sort((d1, d2) => {
				const comparison = this.compareDatasets(sortField, d1, d2);
				return ascending ? comparison : -comparison;
			});
	});

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
		this.visibilityService.layoutVisibilityEvents$(this.layout().id).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(shown => {
			this.loggingService.info(`Multiple layout ${this.layout().id} receiving visibility event containing ${shown}`);
			this.shown.set(shown);
			//mark the datasets belonging to this layout, replacing them instead of mutating them in place
			const modelId = this.layout().datasetModel.id;
			this.datasets.update(datasets => datasets.map(d => d.modelId === modelId ? {...d, show: shown} : d));
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
			this.datasets.update(datasets => [...datasets, {...newDataset, expanded: true}]);
			this.cellLoadingService.registerLayoutCells(layout);
		});
	}

	removeDataset(dataset: CRFDataset) {
		//if the dataset does not have a pk yet, that means that it has not been uploaded yet
		//we can thus delete it permanently
		if(!dataset.pk) {
			this.datasets.update(datasets => datasets.filter(d => d !== dataset));
		}
		else {
			this.openRationaleDialog(true).subscribe((rationale?: string) => {
				if(rationale) {
					this.updateDataset(dataset, {rationale, removed: true});
					this.notificationService.showSuccess('Dataset marked for deletion');
				}
			});
		}
	}

	restoreDataset(dataset: CRFDataset) {
		this.openRationaleDialog(false).subscribe((rationale?: string) => {
			if(rationale) {
				this.updateDataset(dataset, {rationale, removed: false});
				this.notificationService.showSuccess('Dataset marked for restoration');
			}
		});
	}

	//replace a dataset with an updated copy instead of mutating it in place, so the change flows through the datasets signal
	private updateDataset(dataset: CRFDataset, changes: Partial<CRFDataset>) {
		this.datasets.update(datasets => datasets.map(d => d === dataset ? {...d, ...changes} : d));
	}

	private openRationaleDialog(deletion: boolean): Observable<string | undefined> {
		const entityName = new LocalizeMapPipe().transform(this.layout().datasetModel.shortname);
		return this.dialog
			.open(DeleteRestoreComponent, {data: {deletion, entityName}})
			.afterClosed();
	}

	private compareDatasets(field: FieldModel | undefined, d1: CRFDataset, d2: CRFDataset): number {
		if(!field) {
			return d1.id.localeCompare(d2.id);
		}
		const v1 = d1.fields.find(f => f.modelId === field.id)?.value;
		const v2 = d2.fields.find(f => f.modelId === field.id)?.value;
		return this.compareFieldValues(field, v1, v2);
	}

	sortChange(sort: Sort) {
		if(!sort.active && sort.direction === '') {
			return;
		}
		const field = this.layout().datasetModel.fieldModels.find(f => f.id === sort.active);
		this.sort.set({field, ascending: sort.direction === 'asc'});
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
