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

import ch.rodano.api.config.FeatureDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.FeatureService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class FeatureController {

	private final FeatureService featureService;

	public FeatureController(final FeatureService featureService) {
		this.featureService = featureService;
	}

	/**
	 * Get all features for a project
	 */
	@GetMapping("/features")
	public ResponseEntity<List<FeatureDTO>> getFeatures(@PathVariable final UUID projectId) {
		final var features = featureService.getFeatures(projectId);
		return ResponseEntity.ok(features);
	}

	/**
	 * Get a specific feature
	 */
	@GetMapping("/features/{featureId}")
	public ResponseEntity<FeatureDTO> getFeature(
		@PathVariable final UUID projectId,
		@PathVariable final UUID featureId
	) {
		final var Feature = featureService.getFeature(projectId, featureId);
		return ResponseEntity.ok(Feature);
	}

	/**
	 * Create a new feature
	 */
	@PostMapping("/features")
	@SkipProjectAccessCheck
	public ResponseEntity<FeatureDTO> createFeature(
		@PathVariable final UUID projectId,
		@RequestBody final FeatureDTO feature
	) {
		final var created = featureService.createFeature(projectId, feature);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing feature
	 */
	@PutMapping("/features/{featureId}")
	@SkipProjectAccessCheck
	public ResponseEntity<FeatureDTO> updateFeature(
		@PathVariable final UUID projectId,
		@PathVariable final UUID featureId,
		@RequestBody final FeatureDTO feature
	) {
		final var updated = featureService.updateFeature(projectId, featureId, feature);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a feature
	 */
	@DeleteMapping("/features/{featureId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteFeature(
		@PathVariable final UUID projectId,
		@PathVariable final UUID featureId
	) {
		featureService.deleteFeature(projectId, featureId);
		return ResponseEntity.noContent().build();
	}
}
