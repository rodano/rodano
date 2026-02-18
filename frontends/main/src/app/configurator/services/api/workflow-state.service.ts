import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {WorkflowState} from '@core/model/workflow-state';

@Injectable({
	providedIn: 'root'
})
export class WorkflowStateService {
	constructor(private http: HttpClient) {}

	getWorkflowStates(projectId: string): Observable<WorkflowState[]> {
		return this.http.get<WorkflowState[]>(`/api/superuser/configurator/projects/${projectId}/config/workflow-states`, {
			params: {view: 'summary'}
		});
	}

	getWorkflowStatesFull(projectId: string): Observable<WorkflowState[]> {
		return this.http.get<WorkflowState[]>(`/api/superuser/configurator/projects/${projectId}/config/workflow-states`, {
			params: {view: 'full'}
		});
	}

	getWorkflowState(projectId: string, workflowStateId: string): Observable<WorkflowState> {
		return this.http.get<WorkflowState>(`/api/superuser/configurator/projects/${projectId}/config/workflow-states/${workflowStateId}`);
	}

	createWorkflowState(projectId: string, workflowState: WorkflowState): Observable<WorkflowState> {
		return this.http.post<WorkflowState>(`/api/superuser/configurator/projects/${projectId}/config/workflow-states`, workflowState);
	}

	updateWorkflowState(projectId: string, workflowStateId: string, workflowState: WorkflowState): Observable<WorkflowState> {
		return this.http.put<WorkflowState>(`/api/superuser/configurator/projects/${projectId}/config/workflow-states/${workflowStateId}`, workflowState);
	}

	deleteWorkflowState(projectId: string, workflowStateId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/workflow-states/${workflowStateId}`);
	}
}
