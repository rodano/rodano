package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.workflow.WorkflowActionDTO;

public interface WorkflowActionDAOService {

	List<WorkflowActionDTO> getWorkflowActions(UUID projectId);

	WorkflowActionDTO getWorkflowAction(UUID projectId, UUID workflowActionId);

	List<WorkflowActionDTO> getWorkflowActionsByIds(UUID projectId, List<UUID> workflowActionIds);

	WorkflowActionDTO createWorkflowAction(UUID projectId, WorkflowActionDTO workflowAction);

	WorkflowActionDTO updateWorkflowAction(UUID projectId, UUID workflowActionId, WorkflowActionDTO workflowAction);

	void deleteWorkflowAction(UUID projectId, UUID workflowActionId);

	boolean hasPatientData(UUID projectId, UUID workflowActionId);
}
