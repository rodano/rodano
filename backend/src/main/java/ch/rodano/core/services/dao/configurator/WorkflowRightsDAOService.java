package ch.rodano.core.services.dao.configurator;

import java.util.UUID;

import ch.rodano.api.config.WorkflowRightsDTO;

public interface WorkflowRightsDAOService {

	WorkflowRightsDTO getWorkflowRights(UUID projectId);

	void saveWorkflowRights(UUID projectId, WorkflowRightsDTO dto);
}
