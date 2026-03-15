package ch.rodano.api.configurator.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.rodano.api.config.WorkflowWidgetConfigDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.WorkflowWidgetConfigService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class WorkflowWidgetConfigController {

	private final WorkflowWidgetConfigService workflowWidgetConfigService;

	public WorkflowWidgetConfigController(final WorkflowWidgetConfigService workflowWidgetConfigService) {
		this.workflowWidgetConfigService = workflowWidgetConfigService;
	}

	/**
	 * Get all workflow widgets for a project
	 */
	@GetMapping("/widgets")
	public ResponseEntity<List<WorkflowWidgetConfigDTO>> getPrivacyPolicies(@PathVariable final UUID projectId) {
		final var workflowWidgets = workflowWidgetConfigService.getWorkflowWidgets(projectId);
		return ResponseEntity.ok(workflowWidgets);
	}

	/**
	 * Get a specific workflow widget
	 */
	@GetMapping("/widgets/{workflowWidgetId}")
	public ResponseEntity<WorkflowWidgetConfigDTO> getWorkflowWidget(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowWidgetId
	) {
		final var WorkflowWidget = workflowWidgetConfigService.getWorkflowWidget(projectId, workflowWidgetId);
		return ResponseEntity.ok(WorkflowWidget);
	}

	/**
	 * Create a new workflow widget
	 */
	@PostMapping("/widgets")
	@SkipProjectAccessCheck
	public ResponseEntity<WorkflowWidgetConfigDTO> createWorkflowWidget(
		@PathVariable final UUID projectId,
		@RequestBody final WorkflowWidgetConfigDTO workflowWidget
	) {
		final var created = workflowWidgetConfigService.createWorkflowWidget(projectId, workflowWidget);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing workflow widget
	 */
	@PutMapping("/widgets/{workflowWidgetId}")
	@SkipProjectAccessCheck
	public ResponseEntity<WorkflowWidgetConfigDTO> updateWorkflowWidget(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowWidgetId,
		@RequestBody final WorkflowWidgetConfigDTO workflowWidget
	) {
		final var updated = workflowWidgetConfigService.updateWorkflowWidget(projectId, workflowWidgetId, workflowWidget);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a workflow widget
	 */
	@DeleteMapping("/widgets/{workflowWidgetId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteWorkflowWidget(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowWidgetId
	) {
		workflowWidgetConfigService.deleteWorkflowWidget(projectId, workflowWidgetId);
		return ResponseEntity.noContent().build();
	}
}
