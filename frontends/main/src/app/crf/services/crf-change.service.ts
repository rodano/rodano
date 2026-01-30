import {Injectable} from '@angular/core';
import {Subject} from 'rxjs';
import {Workflowable} from '@core/utilities/workflowable';
import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {TypedWorkflowable} from './typed-workflowable';

/**
 * Service used to monitor entities changes in the CRF
 * It allows to emit an event when a CRF (scope/event/form/workflow status) is updated
 */
@Injectable({
	providedIn: 'root'
})
export class CRFChangeService {
	private readonly updatedWorkflowableStream$ = new Subject<TypedWorkflowable>();
	public readonly updatedWorkflowable$ = this.updatedWorkflowableStream$.asObservable();

	public emitUpdatedWorkflowable(entity: WorkflowableEntity, workflowable: Workflowable) {
		const typedWorkflowable = {
			entity,
			workflowable
		};
		this.updatedWorkflowableStream$.next(typedWorkflowable);
	}
}
