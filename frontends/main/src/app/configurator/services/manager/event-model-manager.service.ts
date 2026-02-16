import {Injectable} from '@angular/core';
import {EntityModificationTracker} from '../entity-modification-tracker';
import {EventModel} from '@core/model/event-model';
import {EventModelService} from '../api/event-model.service';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';

@Injectable({
	providedIn: 'root'
})
export class EventModelManagerService {
	private tracker: EntityModificationTracker<EventModel>;
	private loaded = false;
	private fullLoaded = false;

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

	load(projectId: string): Observable<EventModel[]> {
		if(this.loaded) {
			return of(this.tracker.getCurrent());
		}
		return this.eventModelService.getEventModels(projectId).pipe(
			map(models => {
				this.tracker.initialize(models);
				this.loaded = true;
				return models;
			})
		);
	}

	loadFull(projectId: string): Observable<EventModel[]> {
		if(this.fullLoaded) {
			return of(this.tracker.getCurrent());
		}

		return this.eventModelService.getEventModelsFull(projectId).pipe(
			map(fullModels => {
				fullModels.forEach(fullModel => {
					const existing = this.tracker.getEntity(fullModel.eventModelId);
					if(existing) {
						Object.assign(existing, fullModel);
					}

					const original = this.tracker.getOriginals().find(o =>
						(o as any).eventModelId === fullModel.eventModelId
					);
					if(original) {
						Object.assign(original, fullModel);
					}
				});
				this.fullLoaded = true;
				return this.tracker.getCurrent();
			})
		);
	}

	invalidate(): void {
		this.loaded = false;
		this.fullLoaded = false;
	}

	getAllForScope(scopeModelId: string): EventModel[] {
		return this.tracker.getCurrent().filter(em => em.scopeModelId === scopeModelId);
	}

	getOriginalsForScope(scopeModelId: string): EventModel[] {
		return this.tracker.getOriginals().filter(em => em.scopeModelId === scopeModelId);
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
		this.fullLoaded = false;
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
