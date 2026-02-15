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

import ch.rodano.api.config.ValidatorDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.ValidatorService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class ValidatorController {

	private final ValidatorService validatorService;

	public ValidatorController(final ValidatorService validatorService) {
		this.validatorService = validatorService;
	}

	/**
	 * Get all validators for a project
	 */
	@GetMapping("/validators")
	public ResponseEntity<List<ValidatorDTO>> getValidators(@PathVariable final UUID projectId) {
		final var Validators = validatorService.getValidators(projectId);
		return ResponseEntity.ok(Validators);
	}

	/**
	 * Get a specific validator
	 */
	@GetMapping("/validators/{validatorId}")
	public ResponseEntity<ValidatorDTO> getValidator(
		@PathVariable final UUID projectId,
		@PathVariable final UUID validatorId
	) {
		final var Validator = validatorService.getValidator(projectId, validatorId);
		return ResponseEntity.ok(Validator);
	}

	/**
	 * Create a new validator
	 */
	@PostMapping("/validators")
	@SkipProjectAccessCheck
	public ResponseEntity<ValidatorDTO> createValidator(
		@PathVariable final UUID projectId,
		@RequestBody final ValidatorDTO validator
	) {
		final var created = validatorService.createValidator(projectId, validator);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing validator
	 */
	@PutMapping("/validators/{validatorId}")
	@SkipProjectAccessCheck
	public ResponseEntity<ValidatorDTO> updateValidator(
		@PathVariable final UUID projectId,
		@PathVariable final UUID validatorId,
		@RequestBody final ValidatorDTO validator
	) {
		final var updated = validatorService.updateValidator(projectId, validatorId, validator);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a validator
	 */
	@DeleteMapping("/validators/{validatorId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteValidator(
		@PathVariable final UUID projectId,
		@PathVariable final UUID validatorId
	) {
		validatorService.deleteValidator(projectId, validatorId);
		return ResponseEntity.noContent().build();
	}
}
