import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {EventModel} from '@core/model/event-model';

@Injectable({
	providedIn: 'root'
})
export class EventModelService {
	constructor(private http: HttpClient) {}

	getEventModels(projectId: string): Observable<EventModel[]> {
		return this.http.get<EventModel[]>(`/api/superuser/configurator/projects/${projectId}/config/event-models`, {
			params: {view: 'summary'}
		});
	}

	getEventModelsFull(projectId: string): Observable<EventModel[]> {
		return this.http.get<EventModel[]>(`/api/superuser/configurator/projects/${projectId}/config/event-models`, {
			params: {view: 'full'}
		});
	}

	getEventModel(projectId: string, eventModelId: string): Observable<EventModel> {
		return this.http.get<EventModel>(`/api/superuser/configurator/projects/${projectId}/config/event-models/${eventModelId}`);
	}

	createEventModel(projectId: string, eventModel: EventModel): Observable<EventModel> {
		return this.http.post<EventModel>(`/api/superuser/configurator/projects/${projectId}/config/event-models`, eventModel);
	}

	updateEventModel(projectId: string, eventModelId: string, eventModel: EventModel): Observable<EventModel> {
		return this.http.put<EventModel>(`/api/superuser/configurator/projects/${projectId}/config/event-models/${eventModelId}`, eventModel);
	}

	deleteEventModel(projectId: string, eventModelId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/event-models/${eventModelId}`);
	}
}
