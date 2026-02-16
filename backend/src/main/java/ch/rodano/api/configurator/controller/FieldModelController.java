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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.rodano.api.config.FieldModelDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.FieldModelService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class FieldModelController {

	private final FieldModelService fieldModelService;

	public FieldModelController(final FieldModelService fieldModelService) {
		this.fieldModelService = fieldModelService;
	}

	/**
	 * Get all field models for a project
	 */
	@GetMapping("/field-models")
	public ResponseEntity<List<FieldModelDTO>> getFieldModels(
		@PathVariable final UUID projectId,
		@RequestParam(name = "view", required = false, defaultValue = "summary") final String view
	) {
		final var fieldModels = fieldModelService.getFieldModels(projectId, view);
		return ResponseEntity.ok(fieldModels);
	}

	/**
	 * Get a specific field model
	 */
	@GetMapping("/field-models/{fieldModelId}")
	public ResponseEntity<FieldModelDTO> getFieldModel(
		@PathVariable final UUID projectId,
		@PathVariable final UUID fieldModelId
	) {
		final var fieldModel = fieldModelService.getFieldModel(projectId, fieldModelId);
		return ResponseEntity.ok(fieldModel);
	}

	/**
	 * Create a new field model
	 */
	@PostMapping("/field-models")
	@SkipProjectAccessCheck
	public ResponseEntity<FieldModelDTO> createFieldModel(
		@PathVariable final UUID projectId,
		@RequestBody final FieldModelDTO fieldModel
	) {
		final var created = fieldModelService.createFieldModel(projectId, fieldModel);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing field model
	 */
	@PutMapping("/field-models/{fieldModelId}")
	@SkipProjectAccessCheck
	public ResponseEntity<FieldModelDTO> updateFieldModel(
		@PathVariable final UUID projectId,
		@PathVariable final UUID fieldModelId,
		@RequestBody final FieldModelDTO fieldModel
	) {
		final var updated = fieldModelService.updateFieldModel(projectId, fieldModelId, fieldModel);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a field model
	 */
	@DeleteMapping("/field-models/{fieldModelId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteFieldModel(
		@PathVariable final UUID projectId,
		@PathVariable final UUID fieldModelId
	) {
		fieldModelService.deleteFieldModel(projectId, fieldModelId);
		return ResponseEntity.noContent().build();
	}
}
