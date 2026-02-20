import {Injectable} from '@angular/core';
import {EntityModificationTracker} from '../entity-modification-tracker';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {WorkflowState} from '@core/model/workflow-state';
import {WorkflowStateService} from '../api/workflow-state.service';

@Injectable({
	providedIn: 'root'
})
export class WorkflowStateManagerService {
	private tracker: EntityModificationTracker<WorkflowState>;
	private loaded = false;
	private fullLoaded = false;

	constructor(private workflowStateService: WorkflowStateService) {
		this.tracker = new EntityModificationTracker<WorkflowState>(
			wfs => wfs.workflowStateId,
			['id', 'workflowId', 'important', 'color', 'icon', 'aggregateStateId', 'aggregateStateMatcher'],
			['shortname', 'longname', 'description'],
			['possibleActions']
		);
	}

	load(projectId: string): Observable<WorkflowState[]> {
		if(this.loaded) {
			return of(this.tracker.getCurrent());
		}
		return this.workflowStateService.getWorkflowStates(projectId).pipe(
			map(models => {
				this.tracker.initialize(models);
				this.loaded = true;
				return models;
			})
		);
	}

	loadFull(projectId: string): Observable<WorkflowState[]> {
		if(this.fullLoaded) {
			return of(this.tracker.getCurrent());
		}

		return this.workflowStateService.getWorkflowStatesFull(projectId).pipe(
			map(fullModels => {
				fullModels.forEach(fullModel => {
					const existing = this.tracker.getEntity(fullModel.workflowStateId);
					if(existing) {
						Object.assign(existing, fullModel);
					}
					else {
						this.tracker.addEntity(fullModel);
					}
				});
				this.loaded = true;
				this.fullLoaded = true;
				return this.tracker.getCurrent();
			})
		);
	}

	invalidate(): void {
		this.loaded = false;
		this.fullLoaded = false;
	}

	getAllForWorkflow(workflowId: string): WorkflowState[] {
		return this.tracker.getCurrent().filter(wfs => wfs.workflowId === workflowId);
	}

	getOriginalsForWorkflow(workflowId: string): WorkflowState[] {
		return this.tracker.getOriginals().filter(wfs => wfs.workflowId === workflowId);
	}

	create(projectId: string, workflowState: WorkflowState): Observable<WorkflowState> {
		return this.workflowStateService.createWorkflowState(projectId, workflowState).pipe(
			map(created => {
				this.tracker.addEntity(created);
				return created;
			})
		);
	}

	update(workflowState: WorkflowState): void {
		this.tracker.updateEntity(workflowState);
	}

	delete(projectId: string, workflowStateId: string): Observable<void> {
		return this.workflowStateService.deleteWorkflowState(projectId, workflowStateId).pipe(
			map(() => {
				this.tracker.removeEntity(workflowStateId);
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

	getOriginals(): WorkflowState[] {
		return this.tracker.getOriginals();
	}

	clearModifications(): void {
		this.tracker.clearModifications();
	}

	resetToOriginals(): void {
		this.tracker.resetToOriginals();
	}

	setAll(workflowStates: WorkflowState[]): void {
		this.tracker.initialize(workflowStates);
		this.fullLoaded = false;
	}

	getAll(): WorkflowState[] {
		return this.tracker.getCurrent();
	}

	getById(id: string): WorkflowState | undefined {
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
