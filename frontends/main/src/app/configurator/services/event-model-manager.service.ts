import {Injectable} from '@angular/core';
import {EntityModificationTracker} from './entity-modification-tracker';
import {EventModel} from '@core/model/event-model';
import {EventModelService} from './event-model.service';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';

@Injectable({
	providedIn: 'root'
})
export class EventModelManagerService {
	private tracker: EntityModificationTracker<EventModel>;

	constructor(private eventModelService: EventModelService) {
		this.tracker = new EntityModificationTracker<EventModel>(
			em => em.eventModelId,
			[
				'id', 'number', 'icon', 'eventGroupId', 'mandatory', 'inceptive',
				'deadline', 'deadlineUnit', 'deadlineAggregationFunction',
				'interval', 'intervalUnit', 'maxOccurrence', 'preventAdd', 'labelPattern'
			],
			['shortname', 'longname', 'description'],
			[
				'formModelIds', 'datasetModelIds', 'workflowIds',
				'deadlineReferenceEventModelIds', 'blockedEventModelIds', 'impliedEventModelIds'
			]
		);
	}

	loadForScope(projectId: string, scopeModelId: string): Observable<EventModel[]> {
		return this.eventModelService.getEventModels(projectId).pipe(
			map(models => {
				const filtered = models.filter(em => em.scopeModelId === scopeModelId);
				this.tracker.initialize(filtered);
				return filtered;
			})
		);
	}

	create(projectId: string, eventModel: EventModel): Observable<EventModel> {
		return this.eventModelService.createEventModel(projectId, eventModel).pipe(
			map(created => {
				this.tracker.addEntity(created);
				return created;
			})
		);
	}

	update(eventModel: EventModel): void {
		this.tracker.updateEntity(eventModel);
	}

	delete(projectId: string, eventModelId: string): Observable<void> {
		return this.eventModelService.deleteEventModel(projectId, eventModelId).pipe(
			map(() => {
				this.tracker.removeEntity(eventModelId);
			})
		);
	}

	getModifiedIds(): Set<string> {
		return this.tracker.getModifiedIds();
	}

	getModifiedFieldsMap(): Map<string, Set<string>> {
		return this.tracker.getModifiedFieldsMap();
	}

	getOriginals(): EventModel[] {
		return this.tracker.getOriginals();
	}

	clearModifications(): void {
		this.tracker.clearModifications();
	}

	resetToOriginals(): void {
		this.tracker.resetToOriginals();
	}

	setAll(eventModels: EventModel[]): void {
		this.tracker.initialize(eventModels);
	}

	getAll(): EventModel[] {
		return this.tracker.getCurrent();
	}

	getById(id: string): EventModel | undefined {
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
