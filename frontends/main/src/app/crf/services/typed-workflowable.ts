import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {Workflowable} from '@core/utilities/workflowable';

export interface TypedWorkflowable {
	entity: WorkflowableEntity;
	workflowable: Workflowable;
}
