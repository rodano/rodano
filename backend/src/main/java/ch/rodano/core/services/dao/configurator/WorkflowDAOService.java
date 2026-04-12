package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.workflow.WorkflowDTO;

public interface WorkflowDAOService {

	List<WorkflowDTO> getWorkflows(UUID projectId);

	WorkflowDTO getWorkflow(UUID projectId, UUID workflowId);

	WorkflowDTO createWorkflow(UUID projectId, WorkflowDTO workflow);

	WorkflowDTO updateWorkflow(UUID projectId, UUID workflowId, WorkflowDTO workflow);

	void deleteWorkflow(UUID projectId, UUID workflowId);

	boolean hasPatientData(UUID projectId, UUID workflowId);
}
