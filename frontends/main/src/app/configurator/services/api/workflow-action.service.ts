import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {WorkflowAction} from '@core/model/workflow-action';

@Injectable({
	providedIn: 'root'
})
export class WorkflowActionService {
	constructor(private http: HttpClient) {}

	getWorkflowActions(projectId: string): Observable<WorkflowAction[]> {
		return this.http.get<WorkflowAction[]>(`/api/superuser/configurator/projects/${projectId}/config/workflow-actions`);
	}

	getWorkflowAction(projectId: string, workflowActionId: string): Observable<WorkflowAction> {
		return this.http.get<WorkflowAction>(`/api/superuser/configurator/projects/${projectId}/config/workflow-actions/${workflowActionId}`);
	}

	createWorkflowAction(projectId: string, workflowAction: WorkflowAction): Observable<WorkflowAction> {
		return this.http.post<WorkflowAction>(`/api/superuser/configurator/projects/${projectId}/config/workflow-actions`, workflowAction);
	}

	updateWorkflowAction(projectId: string, workflowActionId: string, workflowAction: WorkflowAction): Observable<WorkflowAction> {
		return this.http.put<WorkflowAction>(`/api/superuser/configurator/projects/${projectId}/config/workflow-actions/${workflowActionId}`, workflowAction);
	}

	deleteWorkflowAction(projectId: string, workflowActionId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/workflow-actions/${workflowActionId}`);
	}
}
