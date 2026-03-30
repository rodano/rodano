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

import ch.rodano.api.config.RuleDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.model.jooq.enums.RuleEntityType;
import ch.rodano.core.services.dao.configurator.RuleDAOService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config/event-models/{eventModelId}/rules")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class EventModelRuleController {

	private final RuleDAOService ruleDAOService;

	public EventModelRuleController(final RuleDAOService ruleDAOService) {
		this.ruleDAOService = ruleDAOService;
	}

	@GetMapping
	public ResponseEntity<List<RuleDTO>> getRules(
		@PathVariable final UUID projectId,
		@PathVariable final UUID eventModelId
	) {
		return ResponseEntity.ok(ruleDAOService.getRules(projectId, RuleEntityType.EVENT_MODEL, eventModelId));
	}

	@PostMapping
	@SkipProjectAccessCheck
	public ResponseEntity<RuleDTO> createRule(
		@PathVariable final UUID projectId,
		@PathVariable final UUID eventModelId,
		@RequestBody final RuleDTO dto
	) {
		return ResponseEntity.ok(ruleDAOService.createRule(projectId, RuleEntityType.EVENT_MODEL, eventModelId, null, dto));
	}

	@PutMapping("/{ruleId}")
	@SkipProjectAccessCheck
	public ResponseEntity<RuleDTO> updateRule(
		@PathVariable final UUID projectId,
		@PathVariable final UUID eventModelId,
		@PathVariable final UUID ruleId,
		@RequestBody final RuleDTO dto
	) {
		return ResponseEntity.ok(ruleDAOService.updateRule(projectId, RuleEntityType.EVENT_MODEL, eventModelId, ruleId, dto));
	}

	@DeleteMapping("/{ruleId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteRule(
		@PathVariable final UUID projectId,
		@PathVariable final UUID eventModelId,
		@PathVariable final UUID ruleId
	) {
		ruleDAOService.deleteRule(projectId, RuleEntityType.EVENT_MODEL, eventModelId, ruleId);
		return ResponseEntity.noContent().build();
	}
}
