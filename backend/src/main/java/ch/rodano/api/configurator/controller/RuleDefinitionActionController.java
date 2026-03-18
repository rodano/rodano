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

import ch.rodano.api.config.RuleDefinitionActionDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.RuleDefinitionActionService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class RuleDefinitionActionController {

	private final RuleDefinitionActionService ruleDefinitionActionService;

	public RuleDefinitionActionController(final RuleDefinitionActionService ruleDefinitionActionService) {
		this.ruleDefinitionActionService = ruleDefinitionActionService;
	}

	/**
	 * Get all rule definition actions for a project
	 */
	@GetMapping("/rule-def-actions")
	public ResponseEntity<List<RuleDefinitionActionDTO>> getRuleDefinitionActions(@PathVariable final UUID projectId) {
		final var ruleDefinitionProperties = ruleDefinitionActionService.getRuleDefinitionActions(projectId);
		return ResponseEntity.ok(ruleDefinitionProperties);
	}

	/**
	 * Get a specific rule definition action
	 */
	@GetMapping("/rule-def-actions/{ruleDefinitionActionId}")
	public ResponseEntity<RuleDefinitionActionDTO> getRuleDefinitionAction(
		@PathVariable final UUID projectId,
		@PathVariable final UUID ruleDefinitionActionId
	) {
		final var ruleDefinitionAction = ruleDefinitionActionService.getRuleDefinitionAction(projectId, ruleDefinitionActionId);
		return ResponseEntity.ok(ruleDefinitionAction);
	}

	/**
	 * Create a new rule definition action
	 */
	@PostMapping("/rule-def-actions")
	@SkipProjectAccessCheck
	public ResponseEntity<RuleDefinitionActionDTO> createRuleDefinitionAction(
		@PathVariable final UUID projectId,
		@RequestBody final RuleDefinitionActionDTO ruleDefinitionAction
	) {
		final var created = ruleDefinitionActionService.createRuleDefinitionAction(projectId, ruleDefinitionAction);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing rule definition action
	 */
	@PutMapping("/rule-def-actions/{ruleDefinitionActionId}")
	@SkipProjectAccessCheck
	public ResponseEntity<RuleDefinitionActionDTO> updateRuleDefinitionAction(
		@PathVariable final UUID projectId,
		@PathVariable final UUID ruleDefinitionActionId,
		@RequestBody final RuleDefinitionActionDTO ruleDefinitionAction
	) {
		final var updated = ruleDefinitionActionService.updateRuleDefinitionAction(projectId, ruleDefinitionActionId, ruleDefinitionAction);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a rule definition action
	 */
	@DeleteMapping("/rule-def-actions/{ruleDefinitionActionId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteRuleDefinitionAction(
		@PathVariable final UUID projectId,
		@PathVariable final UUID ruleDefinitionActionId
	) {
		ruleDefinitionActionService.deleteRuleDefinitionAction(projectId, ruleDefinitionActionId);
		return ResponseEntity.noContent().build();
	}
}
