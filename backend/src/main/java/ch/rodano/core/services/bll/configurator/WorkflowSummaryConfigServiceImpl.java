package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.WorkflowSummaryDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.WorkflowSummaryDAOService;

@Service
@Transactional
public class WorkflowSummaryConfigServiceImpl implements WorkflowSummaryConfigService {

	private final WorkflowSummaryDAOService workflowSummaryDAOService;

	public WorkflowSummaryConfigServiceImpl(final WorkflowSummaryDAOService workflowSummaryDAOService) {
		this.workflowSummaryDAOService = workflowSummaryDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkflowSummaryDTO> getWorkflowSummaries(final UUID projectId) {
		return workflowSummaryDAOService.getWorkflowSummaries(projectId);
	}

	@Override
	@Transactional(readOnly = true)
	public WorkflowSummaryDTO getWorkflowSummary(final UUID projectId, final UUID workflowSummaryId) {
		final var workflowSummary = workflowSummaryDAOService.getWorkflowSummary(projectId, workflowSummaryId);
		if(workflowSummary == null) {
			throw new NotFoundException("Workflow summary not found: " + workflowSummaryId);
		}
		return workflowSummary;
	}

	@Override
	public WorkflowSummaryDTO createWorkflowSummary(final UUID projectId, final WorkflowSummaryDTO dto) {
		return workflowSummaryDAOService.createWorkflowSummary(projectId, dto);
	}

	@Override
	public WorkflowSummaryDTO updateWorkflowSummary(final UUID projectId, final UUID workflowSummaryId, final WorkflowSummaryDTO dto) {
		final var existing = workflowSummaryDAOService.getWorkflowSummary(projectId, workflowSummaryId);
		if(existing == null) {
			throw new NotFoundException("Workflow summary not found: " + workflowSummaryId);
		}
		return workflowSummaryDAOService.updateWorkflowSummary(projectId, workflowSummaryId, dto);
	}

	@Override
	public void deleteWorkflowSummary(final UUID projectId, final UUID workflowSummaryId) {
		final var existing = workflowSummaryDAOService.getWorkflowSummary(projectId, workflowSummaryId);
		if(existing == null) {
			throw new NotFoundException("Workflow summary not found: " + workflowSummaryId);
		}
		workflowSummaryDAOService.deleteWorkflowSummary(projectId, workflowSummaryId);
	}
}
