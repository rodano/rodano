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
@RequestMapping("/superuser/configurator/projects/{projectId}/config/form-models/{formModelId}/layouts/{layoutId}/constraint")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class FormLayoutConstraintController {

	private final RuleDAOService ruleDAOService;

	public FormLayoutConstraintController(final RuleDAOService ruleDAOService) {
		this.ruleDAOService = ruleDAOService;
	}

	@GetMapping
	public ResponseEntity<RuleConstraintDTO> getConstraint(
		@PathVariable final UUID projectId,
		@PathVariable final UUID layoutId
	) {
		return ResponseEntity.ok(ruleDAOService.getConstraint(projectId, layoutId));
	}

	@PutMapping
	@SkipProjectAccessCheck
	public ResponseEntity<RuleConstraintDTO> saveConstraint(
		@PathVariable final UUID projectId,
		@PathVariable final UUID layoutId,
		@RequestBody final RuleConstraintDTO dto
	) {
		return ResponseEntity.ok(ruleDAOService.saveConstraint(projectId, layoutId, RuleConstraintOwnerType.FORM_LAYOUT,
			RuleConstraintConstraintType.DEFAULT, dto));
	}
}
