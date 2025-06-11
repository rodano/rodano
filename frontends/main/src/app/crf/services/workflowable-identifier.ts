import {WorkflowableEntity} from '@core/model/workflowable-entity';

export interface WorkflowableIdentifier {
	entity: WorkflowableEntity;
	scopePk: number;
	eventPk?: number;
	formPk?: number;
}
