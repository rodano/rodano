package ch.rodano.api.configurator.controller;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
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

import ch.rodano.api.config.RuleDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.model.jooq.enums.RuleEntityType;
import ch.rodano.core.services.dao.configurator.RuleDAOService;

import static ch.rodano.core.model.jooq.tables.WorkflowAction.WORKFLOW_ACTION;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config/workflow-actions/{workflowActionId}/rules")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class WorkflowActionRuleController {

	private final RuleDAOService ruleDAOService;
	private final DSLContext dslContext;

	public WorkflowActionRuleController(final RuleDAOService ruleDAOService, final DSLContext dslContext) {
		this.ruleDAOService = ruleDAOService;
		this.dslContext = dslContext;
	}

	@GetMapping
	public ResponseEntity<List<RuleDTO>> getRules(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowActionId
	) {
		return ResponseEntity.ok(ruleDAOService.getRules(projectId, RuleEntityType.WORKFLOW_ACTION, workflowActionId));
	}

	@PostMapping
	@SkipProjectAccessCheck
	public ResponseEntity<RuleDTO> createRule(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowActionId,
		@RequestBody final RuleDTO dto
	) {
		final String ruleType = dslContext.select(WORKFLOW_ACTION.CODE)
			.from(WORKFLOW_ACTION)
			.where(WORKFLOW_ACTION.WORKFLOW_ACTION_ID.eq(workflowActionId))
			.fetchOne(WORKFLOW_ACTION.CODE);
		return ResponseEntity.ok(ruleDAOService.createRule(projectId, RuleEntityType.WORKFLOW_ACTION, workflowActionId, ruleType, dto));
	}

	@PutMapping("/{ruleId}")
	@SkipProjectAccessCheck
	public ResponseEntity<RuleDTO> updateRule(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowActionId,
		@PathVariable final UUID ruleId,
		@RequestBody final RuleDTO dto
	) {
		return ResponseEntity.ok(ruleDAOService.updateRule(projectId, RuleEntityType.WORKFLOW_ACTION, workflowActionId, ruleId, dto));
	}

	@DeleteMapping("/{ruleId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteRule(
		@PathVariable final UUID projectId,
		@PathVariable final UUID workflowActionId,
		@PathVariable final UUID ruleId
	) {
		ruleDAOService.deleteRule(projectId, RuleEntityType.WORKFLOW_ACTION, workflowActionId, ruleId);
		return ResponseEntity.noContent().build();
	}
}
