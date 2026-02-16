import {Injectable} from '@angular/core';
import {EventGroup} from '@core/model/event-group';
import {EntityModificationTracker} from '../entity-modification-tracker';
import {EventGroupService} from '../api/event-group.service';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';

@Injectable({
	providedIn: 'root'
})
export class EventGroupManagerService {
	private tracker: EntityModificationTracker<EventGroup>;
	private loaded = false;

	constructor(private eventGroupService: EventGroupService) {
		this.tracker = new EntityModificationTracker<EventGroup>(
			eg => eg.eventGroupId,
			['id'],
			['shortname', 'longname', 'description'],
			[]
		);
	}

	load(projectId: string): Observable<EventGroup[]> {
		if(this.loaded) {
			return of(this.tracker.getCurrent());
		}
		return this.eventGroupService.getEventGroups(projectId).pipe(
			map(groups => {
				this.tracker.initialize(groups);
				this.loaded = true;
				return groups;
			})
		);
	}

	invalidate(): void {
		this.loaded = false;
	}

	getAllForScope(scopeModelId: string): EventGroup[] {
		return this.tracker.getCurrent().filter(eg => eg.scopeModelId === scopeModelId);
	}

	getOriginalsForScope(scopeModelId: string): EventGroup[] {
		return this.tracker.getOriginals().filter(eg => eg.scopeModelId === scopeModelId);
	}

	create(projectId: string, eventGroup: EventGroup): Observable<EventGroup> {
		return this.eventGroupService.createEventGroup(projectId, eventGroup).pipe(
			map(created => {
				this.tracker.addEntity(created);
				return created;
			})
		);
	}

	update(eventGroup: EventGroup): void {
		this.tracker.updateEntity(eventGroup);
	}

	delete(projectId: string, eventGroupId: string): Observable<void> {
		return this.eventGroupService.deleteEventGroup(projectId, eventGroupId).pipe(
			map(() => {
				this.tracker.removeEntity(eventGroupId);
			})
		);
	}

	getModifiedIds(): Set<string> {
		return this.tracker.getModifiedIds();
	}

	getOriginals(): EventGroup[] {
		return this.tracker.getOriginals();
	}

	clearModifications(): void {
		this.tracker.clearModifications();
	}

	resetToOriginals(): void {
		this.tracker.resetToOriginals();
	}

	setAll(eventGroups: EventGroup[]): void {
		this.tracker.initialize(eventGroups);
	}

	getAll(): EventGroup[] {
		return this.tracker.getCurrent();
	}

	getById(id: string): EventGroup | undefined {
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
