import {Injectable} from '@angular/core';
import {EntityModificationTracker} from '../entity-modification-tracker';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {WorkflowAction} from '@core/model/workflow-action';
import {WorkflowActionService} from '../api/workflow-action.service';

@Injectable({
	providedIn: 'root'
})
export class WorkflowActionManagerService {
	private tracker: EntityModificationTracker<WorkflowAction>;
	private loaded = false;

	constructor(private workflowActionService: WorkflowActionService) {
		this.tracker = new EntityModificationTracker<WorkflowAction>(
			wfa => wfa.workflowActionId,
			['id', 'workflowId', 'documentable', 'requireSignature'],
			['shortname', 'longname', 'description', 'requireSignatureText', 'documentableOptions'],
			[]
		);
	}

	load(projectId: string): Observable<WorkflowAction[]> {
		if(this.loaded) {
			return of(this.tracker.getCurrent());
		}
		return this.workflowActionService.getWorkflowActions(projectId).pipe(
			map(models => {
				this.tracker.initialize(models);
				this.loaded = true;
				return models;
			})
		);
	}

	invalidate(): void {
		this.loaded = false;
	}

	getAllForWorkflow(workflowId: string): WorkflowAction[] {
		return this.tracker.getCurrent().filter(wfa => wfa.workflowId === workflowId);
	}

	getOriginalsForWorkflow(workflowId: string): WorkflowAction[] {
		return this.tracker.getOriginals().filter(wfa => wfa.workflowId === workflowId);
	}

	create(projectId: string, workflowAction: WorkflowAction): Observable<WorkflowAction> {
		return this.workflowActionService.createWorkflowAction(projectId, workflowAction).pipe(
			map(created => {
				this.tracker.addEntity(created);
				return created;
			})
		);
	}

	update(workflowAction: WorkflowAction): void {
		this.tracker.updateEntity(workflowAction);
	}

	delete(projectId: string, workflowActionId: string): Observable<void> {
		return this.workflowActionService.deleteWorkflowAction(projectId, workflowActionId).pipe(
			map(() => {
				this.tracker.removeEntity(workflowActionId);
			})
		);
	}

	getModifiedIds(): Set<string> {
		return this.tracker.getModifiedIds();
	}

	getModifiedFieldsMap(): Map<string, Set<string>> {
		return this.tracker.getModifiedFieldsMap();
	}

	getOriginals(): WorkflowAction[] {
		return this.tracker.getOriginals();
	}

	clearModifications(): void {
		this.tracker.clearModifications();
	}

	resetToOriginals(): void {
		this.tracker.resetToOriginals();
	}

	setAll(workflowActions: WorkflowAction[]): void {
		this.tracker.initialize(workflowActions);
	}

	getAll(): WorkflowAction[] {
		return this.tracker.getCurrent();
	}

	getById(id: string): WorkflowAction | undefined {
		return this.tracker.getEntity(id);
	}

	isModified(id: string): boolean {
		return this.tracker.isModified(id);
	}

	getModificationCount(): number {
		return this.tracker.getTotalModifiedFieldsCount();
	}

	syncOriginalsWithCurrent(): void {
		this.tracker.syncOriginalsWithCurrent();
	}
}
