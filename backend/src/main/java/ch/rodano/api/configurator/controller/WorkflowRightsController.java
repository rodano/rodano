package ch.rodano.api.configurator.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.rodano.api.config.WorkflowRightsDTO;
import ch.rodano.core.services.bll.configurator.WorkflowRightsService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class WorkflowRightsController {

	private final WorkflowRightsService workflowRightsService;

	public WorkflowRightsController(final WorkflowRightsService workflowRightsService) {
		this.workflowRightsService = workflowRightsService;
	}

	@GetMapping("/workflow-rights")
	public ResponseEntity<WorkflowRightsDTO> getWorkflowRights(@PathVariable final UUID projectId) {
		return ResponseEntity.ok(workflowRightsService.getWorkflowRights(projectId));
	}

	@PutMapping("/workflow-rights")
	public ResponseEntity<Void> saveWorkflowRights(
		@PathVariable final UUID projectId,
		@RequestBody final WorkflowRightsDTO dto
	) {
		workflowRightsService.saveWorkflowRights(projectId, dto);
		return ResponseEntity.noContent().build();
	}
}
