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

import ch.rodano.api.workflow.WorkflowDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.WorkflowService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class WorkflowController {

	private final WorkflowService workflowService;

	public WorkflowController(final WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	/**
	 * Get all workflows for a project
	 */
	@GetMapping("/workflows")
	public ResponseEntity<List<WorkflowDTO>> getWorkflows(@PathVariable final UUID projectId) {
		final var workflows = workflowService.getWorkflows(projectId);
		return ResponseEntity.ok(workflows);
	}

	/**
	 * Get a specific workflow
	 */
	@GetMapping("/workflows/{workflowId}")
	public ResponseEntity<WorkflowDTO> getWorkflow(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowId
	) {
		final var Workflow = workflowService.getWorkflow(projectId, workflowId);
		return ResponseEntity.ok(Workflow);
	}

	/**
	 * Create a new workflow
	 */
	@PostMapping("/workflows")
	@SkipProjectAccessCheck
	public ResponseEntity<WorkflowDTO> createWorkflow(
		@PathVariable final UUID projectId,
		@RequestBody final WorkflowDTO workflow
	) {
		final var created = workflowService.createWorkflow(projectId, workflow);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing workflow
	 */
	@PutMapping("/workflows/{workflowId}")
	@SkipProjectAccessCheck
	public ResponseEntity<WorkflowDTO> updateWorkflow(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowId,
		@RequestBody final WorkflowDTO workflow
	) {
		final var updated = workflowService.updateWorkflow(projectId, workflowId, workflow);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a workflow
	 */
	@DeleteMapping("/workflows/{workflowId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteWorkflow(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowId
	) {
		workflowService.deleteWorkflow(projectId, workflowId);
		return ResponseEntity.noContent().build();
	}
}
