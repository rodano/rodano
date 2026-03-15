import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {BaseManagerService} from './base-manager.service';
import {WorkflowSummary} from '@core/model/workflow-summary';
import {WorkflowSummaryService} from '../api/workflow-summary.service';

@Injectable({providedIn: 'root'})
export class WorkflowSummaryManagerService extends BaseManagerService<WorkflowSummary> {
	constructor(private workflowSummaryService: WorkflowSummaryService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (ww: WorkflowSummary) => ww.workflowSummaryId;}
	protected getSimpleFields(): (keyof WorkflowSummary)[] {
		return ['id', 'workflowEntity', 'leafScopeModelId', 'filterExpectedEvents', 'displayLegend', 'displayColumnExport'];
	}

	protected getTranslationFields(): (keyof WorkflowSummary)[] {
		return ['title'];
	}

	protected getArrayFields(): (keyof WorkflowSummary)[] {
		return ['columns', 'workflowIds', 'eventModelIds'];
	}

	protected fetchAll(projectId: string): Observable<WorkflowSummary[]> {
		return this.workflowSummaryService.getWorkflowSummaries(projectId);
	}

	protected createEntity(projectId: string, entity: WorkflowSummary): Observable<WorkflowSummary> {
		return this.workflowSummaryService.createWorkflowSummary(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.workflowSummaryService.deleteWorkflowSummary(projectId, id);
	}
}
