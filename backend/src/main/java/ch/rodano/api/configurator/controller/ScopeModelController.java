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

import ch.rodano.api.config.ScopeModelDTO;
import ch.rodano.api.config.WidgetLayoutDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.ScopeModelLayoutService;
import ch.rodano.core.services.bll.configurator.ScopeModelService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class ScopeModelController {

	private final ScopeModelService scopeModelService;
	private final ScopeModelLayoutService scopeModelLayoutService;

	public ScopeModelController(final ScopeModelService scopeModelService, final ScopeModelLayoutService scopeModelLayoutService) {
		this.scopeModelService = scopeModelService;
		this.scopeModelLayoutService = scopeModelLayoutService;
	}

	/**
	 * Get all scope models for a project
	 */
	@GetMapping("/scope-models")
	public ResponseEntity<List<ScopeModelDTO>> getScopeModels(
		@PathVariable final UUID projectId,
		@RequestParam(name = "view", required = false, defaultValue = "summary") final String view
	) {
		final var scopeModels = scopeModelService.getScopeModels(projectId, view);
		return ResponseEntity.ok(scopeModels);
	}

	/**
	 * Get a specific scope model
	 */
	@GetMapping("/scope-models/{scopeModelId}")
	public ResponseEntity<ScopeModelDTO> getScopeModel(
		@PathVariable final UUID projectId,
		@PathVariable final UUID scopeModelId
	) {
		final var scopeModel = scopeModelService.getScopeModel(projectId, scopeModelId);
		return ResponseEntity.ok(scopeModel);
	}

	/**
	 * Create a new scope model
	 */
	@PostMapping("/scope-models")
	@SkipProjectAccessCheck
	public ResponseEntity<ScopeModelDTO> createScopeModel(
		@PathVariable final UUID projectId,
		@RequestBody final ScopeModelDTO scopeModel
	) {
		final var created = scopeModelService.createScopeModel(projectId, scopeModel);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing scope model
	 */
	@PutMapping("/scope-models/{scopeModelId}")
	@SkipProjectAccessCheck
	public ResponseEntity<ScopeModelDTO> updateScopeModel(
		@PathVariable final UUID projectId,
		@PathVariable final UUID scopeModelId,
		@RequestBody final ScopeModelDTO scopeModel
	) {
		final var updated = scopeModelService.updateScopeModel(projectId, scopeModelId, scopeModel);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a scope model
	 */
	@DeleteMapping("/scope-models/{scopeModelId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteScopeModel(
		@PathVariable final UUID projectId,
		@PathVariable final UUID scopeModelId
	) {
		scopeModelService.deleteScopeModel(projectId, scopeModelId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/scope-models/{scopeModelId}/layout")
	public ResponseEntity<WidgetLayoutDTO> getLayout(
		@PathVariable final UUID projectId,
		@PathVariable final UUID scopeModelId
	) {
		return ResponseEntity.ok(scopeModelLayoutService.getLayout(projectId, scopeModelId));
	}

	@PutMapping("/scope-models/{scopeModelId}/layout")
	@SkipProjectAccessCheck
	public ResponseEntity<WidgetLayoutDTO> saveLayout(
		@PathVariable final UUID projectId,
		@PathVariable final UUID scopeModelId,
		@RequestBody final WidgetLayoutDTO dto
	) {
		return ResponseEntity.ok(scopeModelLayoutService.saveLayout(projectId, scopeModelId, dto));
	}
}
