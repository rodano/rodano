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

import ch.rodano.api.config.FormModelDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.FormModelService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class FormModelController {

	private final FormModelService formModelService;

	public FormModelController(final FormModelService formModelService) {
		this.formModelService = formModelService;
	}

	/**
	 * Get all form models for a project
	 */
	@GetMapping("/form-models")
	public ResponseEntity<List<FormModelDTO>> getFormModels(@PathVariable final UUID projectId) {
		final var formModels = formModelService.getFormModels(projectId);
		return ResponseEntity.ok(formModels);
	}

	/**
	 * Get a specific form model
	 */
	@GetMapping("/form-models/{formModelId}")
	public ResponseEntity<FormModelDTO> getFormModel(
		@PathVariable final UUID projectId,
		@PathVariable final UUID formModelId
	) {
		final var FormModel = formModelService.getFormModel(projectId, formModelId);
		return ResponseEntity.ok(FormModel);
	}

	/**
	 * Create a new form model
	 */
	@PostMapping("/form-models")
	@SkipProjectAccessCheck
	public ResponseEntity<FormModelDTO> createFormModel(
		@PathVariable final UUID projectId,
		@RequestBody final FormModelDTO formModel
	) {
		final var created = formModelService.createFormModel(projectId, formModel);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing form model
	 */
	@PutMapping("/form-models/{formModelId}")
	@SkipProjectAccessCheck
	public ResponseEntity<FormModelDTO> updateFormModel(
		@PathVariable final UUID projectId,
		@PathVariable final UUID formModelId,
		@RequestBody final FormModelDTO formModel
	) {
		final var updated = formModelService.updateFormModel(projectId, formModelId, formModel);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a form model
	 */
	@DeleteMapping("/form-models/{formModelId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteFormModel(
		@PathVariable final UUID projectId,
		@PathVariable final UUID formModelId
	) {
		formModelService.deleteFormModel(projectId, formModelId);
		return ResponseEntity.noContent().build();
	}
}
