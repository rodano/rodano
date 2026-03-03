import {Injectable} from '@angular/core';
import {Observable, of} from 'rxjs';
import {WorkflowState} from '@core/model/workflow-state';
import {WorkflowStateService} from '../api/workflow-state.service';
import {BaseManagerService} from './base-manager.service';
import {EventModel} from '@core/model/event-model';
import {map} from 'rxjs/operators';

@Injectable({providedIn: 'root'})
export class WorkflowStateManagerService extends BaseManagerService<WorkflowState> {
	private fullLoaded = false;

	constructor(private workflowStateService: WorkflowStateService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (wfs: WorkflowState) => wfs.workflowStateId;}
	protected getSimpleFields(): (keyof WorkflowState)[] {
		return ['id', 'workflowId', 'important', 'color', 'icon', 'aggregateStateId', 'aggregateStateMatcher'];
	}

	protected getTranslationFields(): (keyof WorkflowState)[] {
		return ['shortname', 'longname', 'description'];
	}

	protected getArrayFields(): (keyof WorkflowState)[] {
		return ['possibleActions'];
	}

	protected fetchAll(projectId: string): Observable<WorkflowState[]> {
		return this.workflowStateService.getWorkflowStates(projectId);
	}

	protected createEntity(projectId: string, entity: WorkflowState): Observable<WorkflowState> {
		return this.workflowStateService.createWorkflowState(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.workflowStateService.deleteWorkflowState(projectId, id);
	}

	getAllForWorkflow(workflowId: string): WorkflowState[] {
		return this.tracker.getCurrent().filter(wfs => wfs.workflowId === workflowId);
	}

	override invalidate(): void {
		super.invalidate();
		this.fullLoaded = false;
	}

	loadFull(projectId: string): Observable<WorkflowState[]> {
		if(this.fullLoaded) {
			return of(this.tracker.getCurrent());
		}
		return this.workflowStateService.getWorkflowStatesFull(projectId).pipe(
			map(fullModels => {
				fullModels.forEach(fullModel => {
					const existing = this.getById(fullModel.workflowStateId);
					if(existing) {
						Object.assign(existing, fullModel);
					}
					else {
						this.tracker.addEntity(fullModel);
					}
				});
				this.loaded = true;
				this.fullLoaded = true;
				this.syncOriginalsWithCurrent();
				return this.getAll();
			})
		);
	}
}
