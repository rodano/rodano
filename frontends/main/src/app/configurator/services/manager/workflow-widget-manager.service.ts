import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {BaseManagerService} from './base-manager.service';
import {WorkflowWidgetConfig} from '@core/model/workflow-widget-config';
import {WorkflowWidgetService} from '../api/workflow-widget.service';

@Injectable({providedIn: 'root'})
export class WorkflowWidgetManagerService extends BaseManagerService<WorkflowWidgetConfig> {
	constructor(private workflowWidgetService: WorkflowWidgetService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (ww: WorkflowWidgetConfig) => ww.workflowWidgetId;}
	protected getSimpleFields(): (keyof WorkflowWidgetConfig)[] {
		return ['id', 'workflowEntity', 'filterExpectedEvents'];
	}

	protected getTranslationFields(): (keyof WorkflowWidgetConfig)[] {
		return ['shortname', 'longname', 'description'];
	}

	protected getArrayFields(): (keyof WorkflowWidgetConfig)[] {
		return ['columns', 'workflowStateIds'];
	}

	protected fetchAll(projectId: string): Observable<WorkflowWidgetConfig[]> {
		return this.workflowWidgetService.getWorkflowWidgets(projectId);
	}

	protected createEntity(projectId: string, entity: WorkflowWidgetConfig): Observable<WorkflowWidgetConfig> {
		return this.workflowWidgetService.createWorkflowWidget(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.workflowWidgetService.deleteWorkflowWidget(projectId, id);
	}
}
