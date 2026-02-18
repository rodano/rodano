package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.workflow.WorkflowDTO;

public interface WorkflowService {

	List<WorkflowDTO> getWorkflows(UUID projectId);

	WorkflowDTO getWorkflow(UUID projectId, UUID workflowId);

	WorkflowDTO createWorkflow(UUID projectId, WorkflowDTO workflow);

	WorkflowDTO updateWorkflow(UUID projectId, UUID workflowId, WorkflowDTO workflow);

	void deleteWorkflow(UUID projectId, UUID workflowId);
}
