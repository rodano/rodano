import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {WorkflowAction} from '@core/model/workflow-action';
import {WorkflowActionService} from '../api/workflow-action.service';
import {BaseManagerService} from './base-manager.service';

@Injectable({providedIn: 'root'})
export class WorkflowActionManagerService extends BaseManagerService<WorkflowAction> {
	constructor(private workflowActionService: WorkflowActionService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (wfa: WorkflowAction) => wfa.workflowActionId;}
	protected getSimpleFields(): (keyof WorkflowAction)[] {
		return ['id', 'workflowId', 'documentable', 'requireSignature'];
	}

	protected getTranslationFields(): (keyof WorkflowAction)[] {
		return ['shortname', 'longname', 'description', 'requireSignatureText', 'documentableOptions'];
	}

	protected getArrayFields(): (keyof WorkflowAction)[] {
		return [];
	}

	protected fetchAll(projectId: string): Observable<WorkflowAction[]> {
		return this.workflowActionService.getWorkflowActions(projectId);
	}

	protected createEntity(projectId: string, entity: WorkflowAction): Observable<WorkflowAction> {
		return this.workflowActionService.createWorkflowAction(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.workflowActionService.deleteWorkflowAction(projectId, id);
	}

	getAllForWorkflow(workflowId: string): WorkflowAction[] {
		return this.tracker.getCurrent().filter(wfa => wfa.workflowId === workflowId);
	}
}
