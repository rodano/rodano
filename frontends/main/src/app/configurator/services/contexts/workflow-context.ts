import {WorkflowManagerService} from '../manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../manager/workflow-state-manager.service';
import {WorkflowActionManagerService} from '../manager/workflow-action-manager.service';
import {Workflow} from '@core/model/workflow';
import {WorkflowState} from '@core/model/workflow-state';
import {WorkflowAction} from '@core/model/workflow-action';

export interface WorkflowContext {
	workflowManager: WorkflowManagerService;
	workflowStateManager: WorkflowStateManagerService;
	workflowActionManager: WorkflowActionManagerService;
	workflows: Workflow[];
	workflowStates: WorkflowState[];
	workflowActions: WorkflowAction[];
	originalWorkflows: Workflow[];
	modifiedWorkflowIds: Set<string>;
	modifiedWorkflowStateIds: Set<string>;
	modifiedWorkflowActionIds: Set<string>;
}
