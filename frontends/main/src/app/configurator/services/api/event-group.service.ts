import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {EventGroup} from '@core/model/event-group';

@Injectable({
	providedIn: 'root'
})
export class EventGroupService {
	constructor(private http: HttpClient) {}

	getEventGroups(projectId: string): Observable<EventGroup[]> {
		return this.http.get<EventGroup[]>(`/api/superuser/configurator/projects/${projectId}/config/event-groups`, {
			params: {view: 'full'}
		});
	}

	getEventGroup(projectId: string, eventGroupId: string): Observable<EventGroup> {
		return this.http.get<EventGroup>(`/api/superuser/configurator/projects/${projectId}/config/event-groups/${eventGroupId}`);
	}

	createEventGroup(projectId: string, eventGroup: EventGroup): Observable<EventGroup> {
		return this.http.post<EventGroup>(`/api/superuser/configurator/projects/${projectId}/config/event-groups`, eventGroup);
	}

	updateEventGroup(projectId: string, eventGroupId: string, eventGroup: EventGroup): Observable<EventGroup> {
		return this.http.put<EventGroup>(`/api/superuser/configurator/projects/${projectId}/config/event-groups/${eventGroupId}`, eventGroup);
	}

	deleteEventGroup(projectId: string, eventGroupId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/event-groups/${eventGroupId}`);
	}
}
