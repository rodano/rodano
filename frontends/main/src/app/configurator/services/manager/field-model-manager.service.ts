import {Injectable} from '@angular/core';
import {Observable, of} from 'rxjs';
import {FieldModel} from '@core/model/field-model';
import {FieldModelService} from '../api/field-model.service';
import {BaseManagerService} from './base-manager.service';
import {map} from 'rxjs/operators';

@Injectable({providedIn: 'root'})
export class FieldModelManagerService extends BaseManagerService<FieldModel> {
	private fullLoaded = false;

	constructor(private fieldModelService: FieldModelService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (fm: FieldModel) => fm.fieldModelId;}
	protected getSimpleFields(): (keyof FieldModel)[] {
		return ['id', 'type', 'dataType', 'datasetModelId', 'plugin', 'searchable',
			'readOnly', 'exportable', 'allowDateInFuture', 'exportOrder', 'maxLength',
			'maxIntegerDigits', 'maxDecimalDigits', 'minValue', 'maxValue', 'minYear', 'maxYear',
			'dictionary', 'matcher', 'inlineHelp', 'withYears', 'withMonths', 'withDays',
			'withHours', 'withMinutes', 'withSeconds', 'yearsMandatory', 'monthsMandatory', 'daysMandatory',
			'hoursMandatory', 'minutesMandatory', 'secondsMandatory', 'valueFormula',
			'possibleValuesProvider', 'possibleValuesProviderDescription'];
	}

	protected getTranslationFields(): (keyof FieldModel)[] {
		return ['shortname', 'longname', 'description', 'matcherMessage', 'advancedHelp'];
	}

	protected getArrayFields(): (keyof FieldModel)[] {
		return ['validatorIds', 'workflowIds', 'possibleValues'];
	}

	protected fetchAll(projectId: string): Observable<FieldModel[]> {
		return this.fieldModelService.getFieldModels(projectId);
	}

	protected createEntity(projectId: string, entity: FieldModel): Observable<FieldModel> {
		return this.fieldModelService.createFieldModel(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.fieldModelService.deleteFieldModel(projectId, id);
	}

	getAllForDataset(datasetModelId: string): FieldModel[] {
		return this.tracker.getCurrent().filter(fm => fm.datasetModelId === datasetModelId);
	}

	override invalidate(): void {
		super.invalidate();
		this.fullLoaded = false;
	}

	loadFull(projectId: string): Observable<FieldModel[]> {
		if(this.fullLoaded) {
			return of(this.tracker.getCurrent());
		}
		return this.fieldModelService.getFieldModelsFull(projectId).pipe(
			map(fullModels => {
				fullModels.forEach(fullModel => {
					const existing = this.getById(fullModel.fieldModelId);
					if(existing) {
						Object.assign(existing, fullModel);
					}
					else {
						this.tracker.addEntity(fullModel);
					}
				});
				this.loaded = true;
				this.fullLoaded = true;
				this.syncOriginalsWithCurrent();
				return this.getAll();
			})
		);
	}
}
