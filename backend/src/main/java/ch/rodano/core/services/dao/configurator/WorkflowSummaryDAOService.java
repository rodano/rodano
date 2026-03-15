package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.WorkflowSummaryDTO;

public interface WorkflowSummaryDAOService {

	List<WorkflowSummaryDTO> getWorkflowSummaries(UUID projectId);

	WorkflowSummaryDTO getWorkflowSummary(UUID projectId, UUID workflowSummaryId);

	WorkflowSummaryDTO createWorkflowSummary(UUID projectId, WorkflowSummaryDTO dto);

	WorkflowSummaryDTO updateWorkflowSummary(UUID projectId, UUID workflowSummaryId, WorkflowSummaryDTO dto);

	void deleteWorkflowSummary(UUID projectId, UUID workflowSummaryId);
}
