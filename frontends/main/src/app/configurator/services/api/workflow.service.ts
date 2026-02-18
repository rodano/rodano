import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Workflow} from '@core/model/workflow';

@Injectable({
	providedIn: 'root'
})
export class WorkflowService {
	constructor(private http: HttpClient) {}

	getWorkflows(projectId: string): Observable<Workflow[]> {
		return this.http.get<Workflow[]>(`/api/superuser/configurator/projects/${projectId}/config/workflows`);
	}

	getWorkflow(projectId: string, workflowId: string): Observable<Workflow> {
		return this.http.get<Workflow>(`/api/superuser/configurator/projects/${projectId}/config/workflows/${workflowId}`);
	}

	createWorkflow(projectId: string, workflow: Workflow): Observable<Workflow> {
		return this.http.post<Workflow>(`/api/superuser/configurator/projects/${projectId}/config/workflows`, workflow);
	}

	updateWorkflow(projectId: string, workflowId: string, workflow: Workflow): Observable<Workflow> {
		return this.http.put<Workflow>(`/api/superuser/configurator/projects/${projectId}/config/workflows/${workflowId}`, workflow);
	}

	deleteWorkflow(projectId: string, workflowId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/workflows/${workflowId}`);
	}
}
