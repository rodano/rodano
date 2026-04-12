package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.exception.ConfigurationConstraintException;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.api.workflow.WorkflowDTO;
import ch.rodano.core.services.dao.configurator.WorkflowDAOService;

@Service
@Transactional
public class WorkflowServiceImpl implements WorkflowService {

	private final WorkflowDAOService workflowDAOService;

	public WorkflowServiceImpl(final WorkflowDAOService workflowDAOService) {
		this.workflowDAOService = workflowDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkflowDTO> getWorkflows(final UUID projectId) {
		return workflowDAOService.getWorkflows(projectId);
	}

	@Override
	@Transactional(readOnly = true)
	public WorkflowDTO getWorkflow(final UUID projectId, final UUID workflowId) {
		final var workflow = workflowDAOService.getWorkflow(projectId, workflowId);
		if(workflow == null) {
			throw new NotFoundException("Workflow not found: " + workflowId);
		}
		return workflow;
	}

	@Override
	public WorkflowDTO createWorkflow(final UUID projectId, final WorkflowDTO workflow) {
		return workflowDAOService.createWorkflow(projectId, workflow);
	}

	@Override
	public WorkflowDTO updateWorkflow(final UUID projectId, final UUID workflowId, final WorkflowDTO workflow) {
		final var existing = workflowDAOService.getWorkflow(projectId, workflowId);
		if(existing == null) {
			throw new NotFoundException("Workflow not found: " + workflowId);
		}
		return workflowDAOService.updateWorkflow(projectId, workflowId, workflow);
	}

	@Override
	public void deleteWorkflow(final UUID projectId, final UUID workflowId) {
		final var existing = workflowDAOService.getWorkflow(projectId, workflowId);
		if(existing == null) {
			throw new NotFoundException("Workflow not found: " + workflowId);
		}
		if(workflowDAOService.hasPatientData(projectId, workflowId)) {
			throw new ConfigurationConstraintException(
				"Workflow '%s' cannot be deleted: it has existing patient data".formatted(existing.getId())
			);
		}
		workflowDAOService.deleteWorkflow(projectId, workflowId);
	}
}
