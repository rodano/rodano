package ch.rodano.core.services.bll.configurator;

import java.util.UUID;

import ch.rodano.api.config.WorkflowRightsDTO;

public interface WorkflowRightsService {

	WorkflowRightsDTO getWorkflowRights(UUID projectId);

	void saveWorkflowRights(UUID projectId, WorkflowRightsDTO dto);
}
