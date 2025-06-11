import {Injectable} from '@angular/core';
import {Subject} from 'rxjs';
import {WorkflowableDTO} from '@core/utilities/workflowable-dto';
import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {TypedWorkflowable} from './typed-workflowable';

/**
 * Service used to monitor changes in the CRF
 * It emits 2 kinds of events:
 * - when a workflowable has been updated, and the updated object can be provided
 * - when a workflowable has been modified, and may need to be refreshed
 */
@Injectable({
	providedIn: 'root'
})
export class CRFChangeService {
	private readonly updatedWorkflowableStream$ = new Subject<TypedWorkflowable>();
	public readonly updatedWorkflowable$ = this.updatedWorkflowableStream$.asObservable();

	public emitUpdatedWorkflowable(entity: WorkflowableEntity, workflowable: WorkflowableDTO) {
		const typedWorkflowable = {
			entity,
			workflowable
		};
		this.updatedWorkflowableStream$.next(typedWorkflowable);
	}
}
