package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.workflow.WorkflowStateDTO;

public interface WorkflowStateService {

	List<WorkflowStateDTO> getWorkflowStates(UUID projectId, String view);

	default List<WorkflowStateDTO> getWorkflowStates(final UUID projectId) {
		return getWorkflowStates(projectId, "summary");
	}

	WorkflowStateDTO getWorkflowState(UUID projectId, UUID workflowStateId);

	WorkflowStateDTO createWorkflowState(UUID projectId, WorkflowStateDTO workflowState);

	WorkflowStateDTO updateWorkflowState(UUID projectId, UUID workflowStateId, WorkflowStateDTO workflowState);

	void deleteWorkflowState(UUID projectId, UUID workflowStateId);
}
