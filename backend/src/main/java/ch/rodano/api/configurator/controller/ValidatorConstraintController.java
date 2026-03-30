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

import ch.rodano.api.config.RuleConstraintDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.model.jooq.enums.RuleConstraintConstraintType;
import ch.rodano.core.model.jooq.enums.RuleConstraintOwnerType;
import ch.rodano.core.services.dao.configurator.RuleDAOService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config/validators/{validatorId}/constraint")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class ValidatorConstraintController {

	private final RuleDAOService ruleDAOService;

	public ValidatorConstraintController(final RuleDAOService ruleDAOService) {
		this.ruleDAOService = ruleDAOService;
	}

	@GetMapping
	public ResponseEntity<RuleConstraintDTO> getConstraint(
		@PathVariable final UUID projectId,
		@PathVariable final UUID validatorId
	) {
		return ResponseEntity.ok(ruleDAOService.getConstraint(projectId, validatorId));
	}

	@PutMapping
	@SkipProjectAccessCheck
	public ResponseEntity<RuleConstraintDTO> saveConstraint(
		@PathVariable final UUID projectId,
		@PathVariable final UUID validatorId,
		@RequestBody final RuleConstraintDTO dto
	) {
		return ResponseEntity.ok(ruleDAOService.saveConstraint(projectId, validatorId,
			RuleConstraintOwnerType.VALIDATOR, RuleConstraintConstraintType.VALIDATION, dto));
	}
}
