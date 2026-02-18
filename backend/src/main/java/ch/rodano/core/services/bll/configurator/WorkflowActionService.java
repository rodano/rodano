package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.workflow.WorkflowActionDTO;

public interface WorkflowActionService {

	List<WorkflowActionDTO> getWorkflowActions(UUID projectId);

	WorkflowActionDTO getWorkflowAction(UUID projectId, UUID workflowActionId);

	WorkflowActionDTO createWorkflowAction(UUID projectId, WorkflowActionDTO workflowAction);

	WorkflowActionDTO updateWorkflowAction(UUID projectId, UUID workflowActionId, WorkflowActionDTO workflowAction);

	void deleteWorkflowAction(UUID projectId, UUID workflowActionId);
}
