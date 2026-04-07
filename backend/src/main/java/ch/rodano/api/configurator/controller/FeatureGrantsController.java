package ch.rodano.api.configurator.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.FeatureGrantsService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class FeatureGrantsController {

	private final FeatureGrantsService featureGrantsService;

	public FeatureGrantsController(final FeatureGrantsService featureGrantsService) {
		this.featureGrantsService = featureGrantsService;
	}

	@GetMapping("/feature-grants")
	public ResponseEntity<Map<UUID, List<UUID>>> getFeatureGrants(@PathVariable final UUID projectId) {
		return ResponseEntity.ok(featureGrantsService.getFeatureGrants(projectId));
	}

	@PutMapping("/feature-grants")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> saveFeatureGrants(
		@PathVariable final UUID projectId,
		@RequestBody final Map<UUID, List<UUID>> profileFeatureMap
	) {
		featureGrantsService.saveFeatureGrants(projectId, profileFeatureMap);
		return ResponseEntity.noContent().build();
	}
}
