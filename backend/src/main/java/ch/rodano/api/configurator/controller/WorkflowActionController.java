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

import ch.rodano.api.workflow.WorkflowActionDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.WorkflowActionService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class WorkflowActionController {

	private final WorkflowActionService workflowActionService;

	public WorkflowActionController(final WorkflowActionService workflowActionService) {
		this.workflowActionService = workflowActionService;
	}

	/**
	 * Get all workflow actions for a project
	 */
	@GetMapping("/workflow-actions")
	public ResponseEntity<List<WorkflowActionDTO>> getWorkflowActions(@PathVariable final UUID projectId) {
		final var workflowActions = workflowActionService.getWorkflowActions(projectId);
		return ResponseEntity.ok(workflowActions);
	}

	/**
	 * Get a specific workflow action
	 */
	@GetMapping("/workflow-actions/{workflowActionId}")
	public ResponseEntity<WorkflowActionDTO> getWorkflowAction(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowActionId
	) {
		final var WorkflowAction = workflowActionService.getWorkflowAction(projectId, workflowActionId);
		return ResponseEntity.ok(WorkflowAction);
	}

	/**
	 * Create a new workflow action
	 */
	@PostMapping("/workflow-actions")
	@SkipProjectAccessCheck
	public ResponseEntity<WorkflowActionDTO> createWorkflowAction(
		@PathVariable final UUID projectId,
		@RequestBody final WorkflowActionDTO workflowAction
	) {
		final var created = workflowActionService.createWorkflowAction(projectId, workflowAction);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing workflow action
	 */
	@PutMapping("/workflow-actions/{workflowActionId}")
	@SkipProjectAccessCheck
	public ResponseEntity<WorkflowActionDTO> updateWorkflowAction(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowActionId,
		@RequestBody final WorkflowActionDTO workflowAction
	) {
		final var updated = workflowActionService.updateWorkflowAction(projectId, workflowActionId, workflowAction);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a workflow action
	 */
	@DeleteMapping("/workflow-actions/{workflowActionId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteWorkflowAction(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowActionId
	) {
		workflowActionService.deleteWorkflowAction(projectId, workflowActionId);
		return ResponseEntity.noContent().build();
	}
}
