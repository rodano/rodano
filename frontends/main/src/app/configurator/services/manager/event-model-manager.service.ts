import {Injectable} from '@angular/core';
import {EventModel} from '@core/model/event-model';
import {EventModelService} from '../api/event-model.service';
import {Observable, of} from 'rxjs';
import {BaseManagerService} from './base-manager.service';
import {map} from 'rxjs/operators';

@Injectable({providedIn: 'root'})
export class EventModelManagerService extends BaseManagerService<EventModel> {
	private fullLoaded = false;

	constructor(private eventModelService: EventModelService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (em: EventModel) => em.eventModelId;}
	protected getSimpleFields(): (keyof EventModel)[] {
		return ['id', 'number', 'icon', 'eventGroupId', 'mandatory', 'inceptive',
			'deadline', 'deadlineUnit', 'deadlineAggregationFunction',
			'interval', 'intervalUnit', 'maxOccurrence', 'preventAdd', 'labelPattern'];
	}

	protected getTranslationFields(): (keyof EventModel)[] {
		return ['shortname', 'longname', 'description'];
	}

	protected getArrayFields(): (keyof EventModel)[] {
		return ['formModelIds', 'datasetModelIds', 'workflowIds',
			'deadlineReferenceEventModelIds', 'blockedEventModelIds', 'impliedEventModelIds'];
	}

	protected fetchAll(projectId: string): Observable<EventModel[]> {
		return this.eventModelService.getEventModels(projectId);
	}

	protected createEntity(projectId: string, entity: EventModel): Observable<EventModel> {
		return this.eventModelService.createEventModel(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.eventModelService.deleteEventModel(projectId, id);
	}

	getAllForScope(scopeModelId: string): EventModel[] {
		return this.tracker.getCurrent().filter(em => em.scopeModelId === scopeModelId);
	}

	override invalidate(): void {
		super.invalidate();
		this.fullLoaded = false;
	}

	loadFull(projectId: string): Observable<EventModel[]> {
		if(this.fullLoaded) {
			return of(this.tracker.getCurrent());
		}
		return this.eventModelService.getEventModelsFull(projectId).pipe(
			map(fullModels => {
				fullModels.forEach(fullModel => {
					const existing = this.getById(fullModel.eventModelId);
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
