import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {WorkflowSummary} from '@core/model/workflow-summary';

@Injectable({
	providedIn: 'root'
})
export class WorkflowSummaryService {
	constructor(private http: HttpClient) {}

	getWorkflowSummaries(projectId: string): Observable<WorkflowSummary[]> {
		return this.http.get<WorkflowSummary[]>(`/api/superuser/configurator/projects/${projectId}/config/summaries`);
	}

	getWorkflowSummary(projectId: string, workflowSummaryId: string): Observable<WorkflowSummary> {
		return this.http.get<WorkflowSummary>(`/api/superuser/configurator/projects/${projectId}/config/summaries/${workflowSummaryId}`);
	}

	createWorkflowSummary(projectId: string, workflowSummary: WorkflowSummary): Observable<WorkflowSummary> {
		return this.http.post<WorkflowSummary>(`/api/superuser/configurator/projects/${projectId}/config/summaries`, workflowSummary);
	}

	updateWorkflowSummary(projectId: string, workflowSummaryId: string, workflowSummary: WorkflowSummary): Observable<WorkflowSummary> {
		return this.http.put<WorkflowSummary>(`/api/superuser/configurator/projects/${projectId}/config/summaries/${workflowSummaryId}`, workflowSummary);
	}

	deleteWorkflowSummary(projectId: string, workflowSummaryId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/summaries/${workflowSummaryId}`);
	}
}
