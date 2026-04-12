package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.exception.ConfigurationConstraintException;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.api.workflow.WorkflowStateDTO;
import ch.rodano.core.services.dao.configurator.WorkflowStateDAOService;

@Service
@Transactional
public class WorkflowStateServiceImpl implements WorkflowStateService {

	private final WorkflowStateDAOService workflowStateDAOService;

	public WorkflowStateServiceImpl(final WorkflowStateDAOService workflowStateDAOService) {
		this.workflowStateDAOService = workflowStateDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkflowStateDTO> getWorkflowStates(final UUID projectId, final String view) {
		return workflowStateDAOService.getWorkflowStates(projectId, view);
	}

	@Override
	@Transactional(readOnly = true)
	public WorkflowStateDTO getWorkflowState(final UUID projectId, final UUID workflowStateId) {
		final var workflowState = workflowStateDAOService.getWorkflowState(projectId, workflowStateId);
		if(workflowState == null) {
			throw new NotFoundException("Workflow state not found: " + workflowStateId);
		}
		return workflowState;
	}

	@Override
	public WorkflowStateDTO createWorkflowState(final UUID projectId, final WorkflowStateDTO workflowState) {
		return workflowStateDAOService.createWorkflowState(projectId, workflowState);
	}

	@Override
	public WorkflowStateDTO updateWorkflowState(final UUID projectId, final UUID workflowStateId, final WorkflowStateDTO workflowState) {
		final var existing = workflowStateDAOService.getWorkflowState(projectId, workflowStateId);
		if(existing == null) {
			throw new NotFoundException("Workflow state not found:  " + workflowStateId);
		}
		return workflowStateDAOService.updateWorkflowState(projectId, workflowStateId, workflowState);
	}

	@Override
	public void deleteWorkflowState(final UUID projectId, final UUID workflowStateId) {
		final var existing = workflowStateDAOService.getWorkflowState(projectId, workflowStateId);
		if(existing == null) {
			throw new NotFoundException("Workflow state not found: " + workflowStateId);
		}
		if(workflowStateDAOService.hasPatientData(projectId, workflowStateId)) {
			throw new ConfigurationConstraintException(
				"Workflow state '%s' cannot be deleted: it has existing patient data".formatted(existing.getId())
			);
		}
		workflowStateDAOService.deleteWorkflowState(projectId, workflowStateId);
	}
}
