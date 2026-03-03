import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Workflow} from '@core/model/workflow';
import {WorkflowService} from '../api/workflow.service';
import {BaseManagerService} from './base-manager.service';

@Injectable({providedIn: 'root'})
export class WorkflowManagerService extends BaseManagerService<Workflow> {
	constructor(private workflowService: WorkflowService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (wf: Workflow) => wf.workflowId;}
	protected getSimpleFields(): (keyof Workflow)[] {
		return ['id', 'aggregatedWorkflowId', 'initialStateId', 'actionId', 'order', 'mandatory', 'unique', 'icon'];
	}

	protected getTranslationFields(): (keyof Workflow)[] {
		return ['shortname', 'longname', 'description', 'message'];
	}

	protected getArrayFields(): (keyof Workflow)[] {
		return [];
	}

	protected fetchAll(projectId: string): Observable<Workflow[]> {
		return this.workflowService.getWorkflows(projectId);
	}

	protected createEntity(projectId: string, entity: Workflow): Observable<Workflow> {
		return this.workflowService.createWorkflow(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.workflowService.deleteWorkflow(projectId, id);
	}
}
