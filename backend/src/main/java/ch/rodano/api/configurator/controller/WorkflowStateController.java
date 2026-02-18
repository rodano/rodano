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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.rodano.api.workflow.WorkflowStateDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.WorkflowStateService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class WorkflowStateController {

	private final WorkflowStateService workflowStateService;

	public WorkflowStateController(final WorkflowStateService workflowStateService) {
		this.workflowStateService = workflowStateService;
	}

	/**
	 * Get all workflow states for a project
	 */
	@GetMapping("/workflow-states")
	public ResponseEntity<List<WorkflowStateDTO>> getWorkflowStates(
		@PathVariable final UUID projectId,
		@RequestParam(name = "view", required = false, defaultValue = "summary") final String view
	) {
		final var WorkflowStates = workflowStateService.getWorkflowStates(projectId, view);
		return ResponseEntity.ok(WorkflowStates);
	}

	/**
	 * Get a specific workflow state
	 */
	@GetMapping("/workflow-states/{workflowStateId}")
	public ResponseEntity<WorkflowStateDTO> getWorkflowState(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowStateId
	) {
		final var WorkflowState = workflowStateService.getWorkflowState(projectId, workflowStateId);
		return ResponseEntity.ok(WorkflowState);
	}

	/**
	 * Create a new workflow state
	 */
	@PostMapping("/workflow-states")
	@SkipProjectAccessCheck
	public ResponseEntity<WorkflowStateDTO> createWorkflowState(
		@PathVariable final UUID projectId,
		@RequestBody final WorkflowStateDTO workflowState
	) {
		final var created = workflowStateService.createWorkflowState(projectId, workflowState);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing workflow state
	 */
	@PutMapping("/workflow-states/{workflowStateId}")
	@SkipProjectAccessCheck
	public ResponseEntity<WorkflowStateDTO> updateWorkflowState(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowStateId,
		@RequestBody final WorkflowStateDTO workflowState
	) {
		final var updated = workflowStateService.updateWorkflowState(projectId, workflowStateId, workflowState);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a workflow state
	 */
	@DeleteMapping("/workflow-states/{workflowStateId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteWorkflowState(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowStateId
	) {
		workflowStateService.deleteWorkflowState(projectId, workflowStateId);
		return ResponseEntity.noContent().build();
	}
}
