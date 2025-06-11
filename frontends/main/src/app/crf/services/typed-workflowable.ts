import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {WorkflowableDTO} from '@core/utilities/workflowable-dto';

export interface TypedWorkflowable {
	entity: WorkflowableEntity;
	workflowable: WorkflowableDTO;
}
