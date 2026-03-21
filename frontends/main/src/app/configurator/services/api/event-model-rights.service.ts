import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {EntityRight} from '@core/model/entity-right';

@Injectable({providedIn: 'root'})
export class EventModelRightsService {
	constructor(private http: HttpClient) {}

	getRights(projectId: string): Observable<Record<string, Record<string, EntityRight>>> {
		return this.http.get<Record<string, Record<string, EntityRight>>>(
			`/api/superuser/configurator/projects/${projectId}/config/event-model-rights`
		);
	}

	saveRights(projectId: string, rights: Record<string, Record<string, EntityRight>>): Observable<void> {
		return this.http.put<void>(
			`/api/superuser/configurator/projects/${projectId}/config/event-model-rights`,
			rights
		);
	}
}
