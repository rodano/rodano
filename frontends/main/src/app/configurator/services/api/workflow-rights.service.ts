import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {WorkflowRights} from '@core/model/workflow-rights';

@Injectable({providedIn: 'root'})
export class WorkflowRightsService {
	constructor(private http: HttpClient) {}

	getRights(projectId: string): Observable<WorkflowRights> {
		return this.http.get<WorkflowRights>(`/api/superuser/configurator/projects/${projectId}/config/workflow-rights`);
	}

	saveRights(projectId: string, dto: WorkflowRights): Observable<void> {
		return this.http.put<void>(`/api/superuser/configurator/projects/${projectId}/config/workflow-rights`, dto);
	}
}
