package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.workflow.WorkflowStateDTO;

public interface WorkflowStateDAOService {

	List<WorkflowStateDTO> getWorkflowStates(UUID projectId, String view);

	List<WorkflowStateDTO> getWorkflowStatesSummary(UUID projectId);

	List<WorkflowStateDTO> getWorkflowStatesFull(UUID projectId);

	WorkflowStateDTO getWorkflowState(UUID projectId, UUID workflowStateId);

	WorkflowStateDTO createWorkflowState(UUID projectId, WorkflowStateDTO workflowState);

	WorkflowStateDTO updateWorkflowState(UUID projectId, UUID workflowStateId, WorkflowStateDTO workflowState);

	void deleteWorkflowState(UUID projectId, UUID workflowStateId);
}
