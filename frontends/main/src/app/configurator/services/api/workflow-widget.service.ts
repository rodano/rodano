import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {WorkflowWidgetConfig} from '@core/model/workflow-widget-config';

@Injectable({
	providedIn: 'root'
})
export class WorkflowWidgetService {
	constructor(private http: HttpClient) {}

	getWorkflowWidgets(projectId: string): Observable<WorkflowWidgetConfig[]> {
		return this.http.get<WorkflowWidgetConfig[]>(`/api/superuser/configurator/projects/${projectId}/config/widgets`);
	}

	getWorkflowWidget(projectId: string, workflowWidgetId: string): Observable<WorkflowWidgetConfig> {
		return this.http.get<WorkflowWidgetConfig>(`/api/superuser/configurator/projects/${projectId}/config/widgets/${workflowWidgetId}`);
	}

	createWorkflowWidget(projectId: string, workflowWidget: WorkflowWidgetConfig): Observable<WorkflowWidgetConfig> {
		return this.http.post<WorkflowWidgetConfig>(`/api/superuser/configurator/projects/${projectId}/config/widgets`, workflowWidget);
	}

	updateWorkflowWidget(projectId: string, workflowWidgetId: string, workflowWidget: WorkflowWidgetConfig): Observable<WorkflowWidgetConfig> {
		return this.http.put<WorkflowWidgetConfig>(`/api/superuser/configurator/projects/${projectId}/config/widgets/${workflowWidgetId}`, workflowWidget);
	}

	deleteWorkflowWidget(projectId: string, workflowWidgetId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/widgets/${workflowWidgetId}`);
	}
}
