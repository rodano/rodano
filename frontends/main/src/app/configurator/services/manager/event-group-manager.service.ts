import {Injectable} from '@angular/core';
import {EventGroup} from '@core/model/event-group';
import {EventGroupService} from '../api/event-group.service';
import {Observable} from 'rxjs';
import {BaseManagerService} from './base-manager.service';

@Injectable({providedIn: 'root'})
export class EventGroupManagerService extends BaseManagerService<EventGroup> {
	constructor(private eventGroupService: EventGroupService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (eg: EventGroup) => eg.eventGroupId;}
	protected getSimpleFields(): (keyof EventGroup)[] {
		return ['id'];
	}

	protected getTranslationFields(): (keyof EventGroup)[] {
		return ['shortname', 'longname', 'description'];
	}

	protected getArrayFields(): (keyof EventGroup)[] {
		return [];
	}

	protected fetchAll(projectId: string): Observable<EventGroup[]> {
		return this.eventGroupService.getEventGroups(projectId);
	}

	protected createEntity(projectId: string, entity: EventGroup): Observable<EventGroup> {
		return this.eventGroupService.createEventGroup(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.eventGroupService.deleteEventGroup(projectId, id);
	}

	getAllForScope(scopeModelId: string): EventGroup[] {
		return this.tracker.getCurrent().filter(eg => eg.scopeModelId === scopeModelId);
	}
}
