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

import ch.rodano.api.config.WorkflowSummaryDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.WorkflowSummaryConfigService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class WorkflowSummaryController {

	private final WorkflowSummaryConfigService workflowSummaryService;

	public WorkflowSummaryController(final WorkflowSummaryConfigService workflowSummaryService) {
		this.workflowSummaryService = workflowSummaryService;
	}

	/**
	 * Get all workflow summaries for a project
	 */
	@GetMapping("/summaries")
	public ResponseEntity<List<WorkflowSummaryDTO>> getPrivacyPolicies(@PathVariable final UUID projectId) {
		final var workflowSummaries = workflowSummaryService.getWorkflowSummaries(projectId);
		return ResponseEntity.ok(workflowSummaries);
	}

	/**
	 * Get a specific workflow Summary
	 */
	@GetMapping("/summaries/{workflowSummaryId}")
	public ResponseEntity<WorkflowSummaryDTO> getWorkflowSummary(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowSummaryId
	) {
		final var workflowSummary = workflowSummaryService.getWorkflowSummary(projectId, workflowSummaryId);
		return ResponseEntity.ok(workflowSummary);
	}

	/**
	 * Create a new workflow summary
	 */
	@PostMapping("/summaries")
	@SkipProjectAccessCheck
	public ResponseEntity<WorkflowSummaryDTO> createWorkflowSummary(
		@PathVariable final UUID projectId,
		@RequestBody final WorkflowSummaryDTO workflowSummary
	) {
		final var created = workflowSummaryService.createWorkflowSummary(projectId, workflowSummary);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing workflow summary
	 */
	@PutMapping("/summaries/{workflowSummaryId}")
	@SkipProjectAccessCheck
	public ResponseEntity<WorkflowSummaryDTO> updateWorkflowSummary(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowSummaryId,
		@RequestBody final WorkflowSummaryDTO workflowSummary
	) {
		final var updated = workflowSummaryService.updateWorkflowSummary(projectId, workflowSummaryId, workflowSummary);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a workflow summary
	 */
	@DeleteMapping("/summaries/{workflowSummaryId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteWorkflowSummary(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowSummaryId
	) {
		workflowSummaryService.deleteWorkflowSummary(projectId, workflowSummaryId);
		return ResponseEntity.noContent().build();
	}
}
