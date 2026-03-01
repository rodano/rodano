import {Injectable} from '@angular/core';
import {EntityModificationTracker} from '../entity-modification-tracker';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {FieldModel} from '@core/model/field-model';
import {FieldModelService} from '../api/field-model.service';

@Injectable({
	providedIn: 'root'
})
export class FieldModelManagerService {
	private tracker: EntityModificationTracker<FieldModel>;
	private loaded = false;
	private fullLoaded = false;

	constructor(private fieldModelService: FieldModelService) {
		this.tracker = new EntityModificationTracker<FieldModel>(
			fm => fm.fieldModelId,
			[
				'id', 'type', 'dataType', 'datasetModelId', 'plugin', 'searchable',
				'readOnly', 'exportable', 'allowDateInFuture', 'exportOrder', 'maxLength',
				'maxIntegerDigits', 'maxDecimalDigits', 'minValue', 'maxValue', 'minYear',
				'dictionary', 'matcher', 'inlineHelp', 'withYears', 'withMonths', 'withDays',
				'withHours', 'withMinutes', 'withSeconds', 'yearsMandatory', 'monthsMandatory', 'daysMandatory',
				'hoursMandatory', 'minutesMandatory', 'secondsMandatory', 'valueFormula',
				'possibleValuesProvider', 'possibleValuesProviderDescription'
			],
			['shortname', 'longname', 'description', 'matcherMessage', 'advancedHelp'],
			['validatorIds', 'workflowIds', 'possibleValues']
		);
	}

	load(projectId: string): Observable<FieldModel[]> {
		if(this.loaded) {
			return of(this.tracker.getCurrent());
		}
		return this.fieldModelService.getFieldModels(projectId).pipe(
			map(models => {
				this.tracker.initialize(models);
				this.loaded = true;
				return models;
			})
		);
	}

	loadFull(projectId: string): Observable<FieldModel[]> {
		if(this.fullLoaded) {
			return of(this.tracker.getCurrent());
		}

		return this.fieldModelService.getFieldModelsFull(projectId).pipe(
			map(fullModels => {
				fullModels.forEach(fullModel => {
					const existing = this.tracker.getEntity(fullModel.fieldModelId);
					if(existing) {
						Object.assign(existing, fullModel);
					}
					else {
						this.tracker.addEntity(fullModel);
					}
				});
				this.loaded = true;
				this.fullLoaded = true;
				return this.tracker.getCurrent();
			})
		);
	}

	invalidate(): void {
		this.loaded = false;
		this.fullLoaded = false;
	}

	getAllForDataset(datasetModelId: string): FieldModel[] {
		return this.tracker.getCurrent().filter(fm => fm.datasetModelId === datasetModelId);
	}

	getOriginalsForDataset(datasetModelId: string): FieldModel[] {
		return this.tracker.getOriginals().filter(fm => fm.datasetModelId === datasetModelId);
	}

	create(projectId: string, fieldModel: FieldModel): Observable<FieldModel> {
		return this.fieldModelService.createFieldModel(projectId, fieldModel).pipe(
			map(created => {
				this.tracker.addEntity(created);
				return created;
			})
		);
	}

	update(fieldModel: FieldModel): void {
		this.tracker.updateEntity(fieldModel);
	}

	delete(projectId: string, fieldModelId: string): Observable<void> {
		return this.fieldModelService.deleteFieldModel(projectId, fieldModelId).pipe(
			map(() => {
				this.tracker.removeEntity(fieldModelId);
			})
		);
	}

	isLoaded(): boolean {
		return this.loaded;
	}

	getModifiedIds(): Set<string> {
		return this.tracker.getModifiedIds();
	}

	getOriginals(): FieldModel[] {
		return this.tracker.getOriginals();
	}

	clearModifications(): void {
		this.tracker.clearModifications();
	}

	resetToOriginals(): void {
		this.tracker.resetToOriginals();
	}

	setAll(fieldModels: FieldModel[]): void {
		this.tracker.initialize(fieldModels);
		this.fullLoaded = false;
	}

	getAll(): FieldModel[] {
		return this.tracker.getCurrent();
	}

	getById(id: string): FieldModel | undefined {
		return this.tracker.getEntity(id);
	}

	isModified(id: string): boolean {
		return this.tracker.isModified(id);
	}

	getModificationCount(): number {
		return this.tracker.getTotalModifiedFieldsCount();
	}

	syncOriginalsWithCurrent(): void {
		this.tracker.syncOriginalsWithCurrent();
	}
}
