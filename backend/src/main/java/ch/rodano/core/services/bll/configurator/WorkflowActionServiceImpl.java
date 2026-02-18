package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.api.workflow.WorkflowActionDTO;
import ch.rodano.core.services.dao.configurator.WorkflowActionDAOService;

@Service
@Transactional
public class WorkflowActionServiceImpl implements WorkflowActionService {

	private final WorkflowActionDAOService workflowActionDAOService;

	public WorkflowActionServiceImpl(final WorkflowActionDAOService workflowActionDAOService) {
		this.workflowActionDAOService = workflowActionDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkflowActionDTO> getWorkflowActions(final UUID projectId) {
		return workflowActionDAOService.getWorkflowActions(projectId);
	}

	@Override
	@Transactional(readOnly = true)
	public WorkflowActionDTO getWorkflowAction(final UUID projectId, final UUID workflowActionId) {
		final var workflowAction = workflowActionDAOService.getWorkflowAction(projectId, workflowActionId);
		if(workflowAction == null) {
			throw new NotFoundException("Workflow action not found: " + workflowActionId);
		}
		return workflowAction;
	}

	@Override
	public WorkflowActionDTO createWorkflowAction(final UUID projectId, final WorkflowActionDTO workflowAction) {
		return workflowActionDAOService.createWorkflowAction(projectId, workflowAction);
	}

	@Override
	public WorkflowActionDTO updateWorkflowAction(final UUID projectId, final UUID workflowActionId, final WorkflowActionDTO workflowAction) {
		final var existing = workflowActionDAOService.getWorkflowAction(projectId, workflowActionId);
		if(existing == null) {
			throw new NotFoundException("Workflow action not found: " + workflowActionId);
		}
		return workflowActionDAOService.updateWorkflowAction(projectId, workflowActionId, workflowAction);
	}

	@Override
	public void deleteWorkflowAction(final UUID projectId, final UUID workflowActionId) {
		final var existing = workflowActionDAOService.getWorkflowAction(projectId, workflowActionId);
		if(existing == null) {
			throw new NotFoundException("Workflow action not found: " + workflowActionId);
		}
		workflowActionDAOService.deleteWorkflowAction(projectId, workflowActionId);
	}
}
