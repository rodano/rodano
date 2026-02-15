import {Injectable} from '@angular/core';
import {ScopeModelService} from './api/scope-model.service';
import {EventModelService} from './api/event-model.service';
import {EventGroupService} from './api/event-group.service';
import {DatasetModelService} from './api/dataset-model.service';
import {FieldModelService} from './api/field-model.service';
import {ScopeModel} from '@core/model/scope-model';
import {forkJoin, map, Observable, of} from 'rxjs';
import {EventModel} from '@core/model/event-model';
import {EventGroup} from '@core/model/event-group';
import {DatasetModel} from '@core/model/dataset-model';
import {FieldModel} from '@core/model/field-model';
import {Validator} from '@core/model/validator';
import {ValidatorService} from './api/validator.service';

@Injectable({providedIn: 'root'})
export class DraftSaveService {
	constructor(
		private scopeModelService: ScopeModelService,
		private eventModelService: EventModelService,
		private eventGroupService: EventGroupService,
		private datasetModelService: DatasetModelService,
		private fieldModelService: FieldModelService,
		private validatorService: ValidatorService
	) {}

	saveScopeModels(
		projectId: string,
		modifiedIds: Set<string>,
		scopeModels: ScopeModel[],
		originalScopeModels: ScopeModel[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			if(id.endsWith('-deleted')) {
				const originalId = id.replace('-deleted', '');
				const original = originalScopeModels.find(sm => sm.scopeModelId === originalId);
				if(original) {
					saveObservables.push(this.scopeModelService.deleteScopeModel(projectId, originalId));
				}
			}
			else if(id.startsWith('temp-')) {
				const scopeModel = scopeModels.find(sm => sm.scopeModelId === id);
				if(scopeModel) {
					saveObservables.push(this.scopeModelService.createScopeModel(projectId, scopeModel));
				}
			}
			else {
				const scopeModel = scopeModels.find(sm => sm.scopeModelId === id);
				if(scopeModel) {
					saveObservables.push(this.scopeModelService.updateScopeModel(projectId, id, scopeModel));
				}
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	saveEventModels(
		projectId: string,
		modifiedIds: Set<string>,
		eventModels: EventModel[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			const eventModel = eventModels.find(em => em.eventModelId === id);
			if(eventModel) {
				saveObservables.push(this.eventModelService.updateEventModel(projectId, id, eventModel));
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	saveEventGroups(
		projectId: string,
		modifiedIds: Set<string>,
		eventGroups: EventGroup[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			const eventGroup = eventGroups.find(eg => eg.eventGroupId === id);
			if(eventGroup) {
				saveObservables.push(this.eventGroupService.updateEventGroup(projectId, id, eventGroup));
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	saveDatasetModels(
		projectId: string,
		modifiedIds: Set<string>,
		datasetModels: DatasetModel[],
		originalDatasetModels: DatasetModel[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			if(id.endsWith('-deleted')) {
				const originalId = id.replace('-deleted', '');
				const original = originalDatasetModels.find(dm => dm.datasetModelId === originalId);
				if(original) {
					saveObservables.push(this.datasetModelService.deleteDatasetModel(projectId, originalId));
				}
			}
			else if(id.startsWith('temp-')) {
				const datasetModel = datasetModels.find(dm => dm.datasetModelId === id);
				if(datasetModel) {
					saveObservables.push(this.datasetModelService.createDatasetModel(projectId, datasetModel));
				}
			}
			else {
				const datasetModel = datasetModels.find(dm => dm.datasetModelId === id);
				if(datasetModel) {
					saveObservables.push(this.datasetModelService.updateDatasetModel(projectId, id, datasetModel));
				}
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	saveFieldModels(
		projectId: string,
		modifiedIds: Set<string>,
		fieldModels: FieldModel[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			const fieldModel = fieldModels.find(fm => fm.fieldModelId === id);
			if(fieldModel) {
				saveObservables.push(this.fieldModelService.updateFieldModel(projectId, id, fieldModel));
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	saveValidators(
		projectId: string,
		modifiedIds: Set<string>,
		validators: Validator[],
		originalValidators: Validator[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			if(id.endsWith('-deleted')) {
				const originalId = id.replace('-deleted', '');
				const original = originalValidators.find(v => v.validatorId === originalId);
				if(original) {
					saveObservables.push(this.validatorService.deleteValidator(projectId, originalId));
				}
			}
			else if(id.startsWith('temp-')) {
				const validator = validators.find(v => v.validatorId === id);
				if(validator) {
					saveObservables.push(this.validatorService.createValidator(projectId, validator));
				}
			}
			else {
				const validator = validators.find(v => v.validatorId === id);
				if(validator) {
					saveObservables.push(this.validatorService.updateValidator(projectId, id, validator));
				}
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}
}
