import {Injectable} from '@angular/core';
import {EntityModificationTracker} from '../entity-modification-tracker';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {Workflow} from '@core/model/workflow';
import {WorkflowService} from '../api/workflow.service';

@Injectable({
	providedIn: 'root'
})
export class WorkflowManagerService {
	private tracker: EntityModificationTracker<Workflow>;
	private loaded = false;

	constructor(private workflowService: WorkflowService) {
		this.tracker = new EntityModificationTracker<Workflow>(
			wf => wf.workflowId,
			['id', 'aggregatedWorkflowId', 'initialStateId', 'actionId', 'order', 'mandatory', 'unique', 'icon'],
			['shortname', 'longname', 'description', 'message'],
			[]
		);
	}

	load(projectId: string): Observable<Workflow[]> {
		if(this.loaded) {
			return of(this.tracker.getCurrent());
		}
		return this.workflowService.getWorkflows(projectId).pipe(
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

	create(projectId: string, workflow: Workflow): Observable<Workflow> {
		return this.workflowService.createWorkflow(projectId, workflow).pipe(
			map(created => {
				this.tracker.addEntity(created);
				return created;
			})
		);
	}

	update(workflow: Workflow): void {
		this.tracker.updateEntity(workflow);
	}

	delete(projectId: string, workflowId: string): Observable<void> {
		return this.workflowService.deleteWorkflow(projectId, workflowId).pipe(
			map(() => {
				this.tracker.removeEntity(workflowId);
			})
		);
	}

	isLoaded(): boolean {
		return this.loaded;
	}

	getModifiedIds(): Set<string> {
		return this.tracker.getModifiedIds();
	}

	getModifiedFieldsMap(): Map<string, Set<string>> {
		return this.tracker.getModifiedFieldsMap();
	}

	getOriginals(): Workflow[] {
		return this.tracker.getOriginals();
	}

	clearModifications(): void {
		this.tracker.clearModifications();
	}

	resetToOriginals(): void {
		this.tracker.resetToOriginals();
	}

	getAll(): Workflow[] {
		return this.tracker.getCurrent();
	}

	getById(id: string): Workflow | undefined {
		return this.tracker.getEntity(id);
	}

	isModified(id: string): boolean {
		return this.tracker.isModified(id);
	}

	isFieldModified(id: string, field: string): boolean {
		return this.tracker.isFieldModified(id, field);
	}

	getModificationCount(): number {
		return this.tracker.getTotalModifiedFieldsCount();
	}

	updateOnServer(projectId: string, workflowId: string, workflow: Workflow): Observable<Workflow> {
		return this.workflowService.updateWorkflow(projectId, workflowId, workflow);
	}

	syncOriginalsWithCurrent(): void {
		this.tracker.syncOriginalsWithCurrent();
	}
}
