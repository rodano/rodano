import {Service} from '@angular/core';
import {Subject} from 'rxjs';
import {Workflowable} from '@core/utilities/workflowable';
import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {TypedWorkflowable} from './typed-workflowable';

/**
 * Service used to monitor workflowable entities updates statuses changes in the application, especially in the CRF
 * It allows to emit an event when a workflowable entity is updated
 */
@Service()
export class WorkflowableUpdateService {
	private readonly updatedWorkflowableStream$ = new Subject<TypedWorkflowable>();
	public readonly updatedWorkflowable$ = this.updatedWorkflowableStream$.asObservable();

	public emitUpdatedWorkflowable(entity: WorkflowableEntity, workflowable: Workflowable) {
		const typedWorkflowable = {
			entity,
			workflowable
		};
		this.updatedWorkflowableStream$.next(typedWorkflowable);
	}

	public match(workflowable: TypedWorkflowable, entity: WorkflowableEntity, pk: number): boolean {
		return workflowable.entity === entity && workflowable.workflowable.pk === pk;
	}
}
