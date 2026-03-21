package ch.rodano.core.services.bll.configurator;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.WorkflowRightsDTO;
import ch.rodano.core.services.dao.configurator.WorkflowRightsDAOService;

@Service
@Transactional
public class WorkflowRightsServiceImpl implements WorkflowRightsService {

	private final WorkflowRightsDAOService workflowRightsDAOService;

	public WorkflowRightsServiceImpl(final WorkflowRightsDAOService workflowRightsDAOService) {
		this.workflowRightsDAOService = workflowRightsDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public WorkflowRightsDTO getWorkflowRights(final UUID projectId) {
		return workflowRightsDAOService.getWorkflowRights(projectId);
	}

	@Override
	public void saveWorkflowRights(final UUID projectId, final WorkflowRightsDTO dto) {
		workflowRightsDAOService.saveWorkflowRights(projectId, dto);
	}
}
