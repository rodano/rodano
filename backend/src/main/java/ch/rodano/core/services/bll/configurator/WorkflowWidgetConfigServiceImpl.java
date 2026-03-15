package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.WorkflowWidgetConfigDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.WorkflowWidgetDAOService;

@Service
@Transactional
public class WorkflowWidgetConfigServiceImpl implements WorkflowWidgetConfigService {

	private final WorkflowWidgetDAOService workflowWidgetDAOService;

	public WorkflowWidgetConfigServiceImpl(final WorkflowWidgetDAOService workflowWidgetDAOService) {
		this.workflowWidgetDAOService = workflowWidgetDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkflowWidgetConfigDTO> getWorkflowWidgets(final UUID projectId) {
		return workflowWidgetDAOService.getWorkflowWidgets(projectId);
	}

	@Override
	@Transactional(readOnly = true)
	public WorkflowWidgetConfigDTO getWorkflowWidget(final UUID projectId, final UUID workflowWidgetId) {
		final var workflowWidget = workflowWidgetDAOService.getWorkflowWidget(projectId, workflowWidgetId);
		if(workflowWidget == null) {
			throw new NotFoundException("Workflow widget not found: " + workflowWidgetId);
		}
		return workflowWidget;
	}

	@Override
	public WorkflowWidgetConfigDTO createWorkflowWidget(final UUID projectId, final WorkflowWidgetConfigDTO dto) {
		return workflowWidgetDAOService.createWorkflowWidget(projectId, dto);
	}

	@Override
	public WorkflowWidgetConfigDTO updateWorkflowWidget(final UUID projectId, final UUID workflowWidgetId, final WorkflowWidgetConfigDTO dto) {
		final var existing = workflowWidgetDAOService.getWorkflowWidget(projectId, workflowWidgetId);
		if(existing == null) {
			throw new NotFoundException("Workflow widget not found: " + workflowWidgetId);
		}
		return workflowWidgetDAOService.updateWorkflowWidget(projectId, workflowWidgetId, dto);
	}

	@Override
	public void deleteWorkflowWidget(final UUID projectId, final UUID workflowWidgetId) {
		final var existing = workflowWidgetDAOService.getWorkflowWidget(projectId, workflowWidgetId);
		if(existing == null) {
			throw new NotFoundException("Workflow widget not found: " + workflowWidgetId);
		}
		workflowWidgetDAOService.deleteWorkflowWidget(projectId, workflowWidgetId);
	}
}
