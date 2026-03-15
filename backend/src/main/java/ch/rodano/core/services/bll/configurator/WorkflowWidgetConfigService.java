package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.WorkflowWidgetConfigDTO;

public interface WorkflowWidgetConfigService {

	List<WorkflowWidgetConfigDTO> getWorkflowWidgets(UUID projectId);

	WorkflowWidgetConfigDTO getWorkflowWidget(UUID projectId, UUID workflowWidgetId);

	WorkflowWidgetConfigDTO createWorkflowWidget(UUID projectId, WorkflowWidgetConfigDTO dto);

	WorkflowWidgetConfigDTO updateWorkflowWidget(UUID projectId, UUID workflowWidgetId, WorkflowWidgetConfigDTO dto);

	void deleteWorkflowWidget(UUID projectId, UUID workflowWidgetId);
}
