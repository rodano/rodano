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

import ch.rodano.api.config.RuleDefinitionPropertyDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.RuleDefinitionPropertyService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class RuleDefinitionPropertyController {

	private final RuleDefinitionPropertyService ruleDefinitionPropertyService;

	public RuleDefinitionPropertyController(final RuleDefinitionPropertyService ruleDefinitionPropertyService) {
		this.ruleDefinitionPropertyService = ruleDefinitionPropertyService;
	}

	/**
	 * Get all rule definition properties for a project
	 */
	@GetMapping("/rule-def-properties")
	public ResponseEntity<List<RuleDefinitionPropertyDTO>> getRuleDefinitionProperties(@PathVariable final UUID projectId) {
		final var ruleDefinitionProperties = ruleDefinitionPropertyService.getRuleDefinitionProperties(projectId);
		return ResponseEntity.ok(ruleDefinitionProperties);
	}

	/**
	 * Get a specific rule definition property
	 */
	@GetMapping("/rule-def-properties/{ruleDefinitionPropertyId}")
	public ResponseEntity<RuleDefinitionPropertyDTO> getRuleDefinitionProperty(
		@PathVariable final UUID projectId,
		@PathVariable final UUID ruleDefinitionPropertyId
	) {
		final var ruleDefinitionProperty = ruleDefinitionPropertyService.getRuleDefinitionProperty(projectId, ruleDefinitionPropertyId);
		return ResponseEntity.ok(ruleDefinitionProperty);
	}

	/**
	 * Create a new rule definition property
	 */
	@PostMapping("/rule-def-properties")
	@SkipProjectAccessCheck
	public ResponseEntity<RuleDefinitionPropertyDTO> createRuleDefinitionProperty(
		@PathVariable final UUID projectId,
		@RequestBody final RuleDefinitionPropertyDTO ruleDefinitionProperty
	) {
		final var created = ruleDefinitionPropertyService.createRuleDefinitionProperty(projectId, ruleDefinitionProperty);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing rule definition property
	 */
	@PutMapping("/rule-def-properties/{ruleDefinitionPropertyId}")
	@SkipProjectAccessCheck
	public ResponseEntity<RuleDefinitionPropertyDTO> updateRuleDefinitionProperty(
		@PathVariable final UUID projectId,
		@PathVariable final UUID ruleDefinitionPropertyId,
		@RequestBody final RuleDefinitionPropertyDTO ruleDefinitionProperty
	) {
		final var updated = ruleDefinitionPropertyService.updateRuleDefinitionProperty(projectId, ruleDefinitionPropertyId, ruleDefinitionProperty);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a rule definition property
	 */
	@DeleteMapping("/rule-def-properties/{ruleDefinitionPropertyId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteRuleDefinitionProperty(
		@PathVariable final UUID projectId,
		@PathVariable final UUID ruleDefinitionPropertyId
	) {
		ruleDefinitionPropertyService.deleteRuleDefinitionProperty(projectId, ruleDefinitionPropertyId);
		return ResponseEntity.noContent().build();
	}
}
