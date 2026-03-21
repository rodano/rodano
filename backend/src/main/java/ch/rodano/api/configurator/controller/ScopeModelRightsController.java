package ch.rodano.api.configurator.controller;

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

import ch.rodano.api.config.EntityRightDTO;
import ch.rodano.core.services.bll.configurator.ScopeModelRightsService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class ScopeModelRightsController {

	private final ScopeModelRightsService scopeModelRightsService;

	public ScopeModelRightsController(final ScopeModelRightsService scopeModelRightsService) {
		this.scopeModelRightsService = scopeModelRightsService;
	}

	@GetMapping("/scope-model-rights")
	public ResponseEntity<Map<UUID, Map<UUID, EntityRightDTO>>> getScopeModelRights(@PathVariable final UUID projectId) {
		return ResponseEntity.ok(scopeModelRightsService.getScopeModelRights(projectId));
	}

	@PutMapping("/scope-model-rights")
	public ResponseEntity<Void> saveScopeModelRights(
		@PathVariable final UUID projectId,
		@RequestBody final Map<UUID, Map<UUID, EntityRightDTO>> rights
	) {
		scopeModelRightsService.saveScopeModelRights(projectId, rights);
		return ResponseEntity.noContent().build();
	}
}
