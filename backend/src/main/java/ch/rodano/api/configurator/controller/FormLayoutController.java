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

import ch.rodano.api.config.LayoutDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.FormLayoutService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class FormLayoutController {

	private final FormLayoutService formLayoutService;

	public FormLayoutController(final FormLayoutService formLayoutService) {
		this.formLayoutService = formLayoutService;
	}

	/**
	 * Get all form layouts for a project
	 */
	@GetMapping("/form-models/{formModelId}/form-layouts")
	public ResponseEntity<List<LayoutDTO>> getLayouts(
		@PathVariable final UUID projectId,
		@PathVariable final UUID formModelId
	) {
		final var Layouts = formLayoutService.getLayouts(projectId, formModelId);
		return ResponseEntity.ok(Layouts);
	}

	/**
	 * Get a specific form layout
	 */
	@GetMapping("/form-models/{formModelId}/form-layouts/{formLayoutId}")
	public ResponseEntity<LayoutDTO> getLayout(
		@PathVariable final UUID projectId,
		@PathVariable final UUID formModelId,
		@PathVariable final UUID formLayoutId
	) {
		final var Layout = formLayoutService.getLayout(projectId, formModelId, formLayoutId);
		return ResponseEntity.ok(Layout);
	}

	/**
	 * Create a new form layout
	 */
	@PostMapping("/form-models/{formModelId}/form-layouts")
	@SkipProjectAccessCheck
	public ResponseEntity<LayoutDTO> createLayout(
		@PathVariable final UUID projectId,
		@PathVariable final UUID formModelId,
		@RequestBody final LayoutDTO formLayout
	) {
		final var created = formLayoutService.createLayout(projectId, formModelId, formLayout);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing form layout
	 */
	@PutMapping("/form-models/{formModelId}/form-layouts/{formLayoutId}")
	@SkipProjectAccessCheck
	public ResponseEntity<LayoutDTO> updateLayout(
		@PathVariable final UUID projectId,
		@PathVariable final UUID formModelId,
		@PathVariable final UUID formLayoutId,
		@RequestBody final LayoutDTO formLayout
	) {
		final var updated = formLayoutService.updateLayout(projectId, formModelId, formLayoutId, formLayout);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a form layout
	 */
	@DeleteMapping("/form-models/{formModelId}/form-layouts/{formLayoutId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteLayout(
		@PathVariable final UUID projectId,
		@PathVariable final UUID formModelId,
		@PathVariable final UUID formLayoutId
	) {
		formLayoutService.deleteLayout(projectId, formModelId, formLayoutId);
		return ResponseEntity.noContent().build();
	}
}
