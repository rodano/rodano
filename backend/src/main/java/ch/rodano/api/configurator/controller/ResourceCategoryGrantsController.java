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

import ch.rodano.core.services.bll.configurator.ResourceCategoryGrantsService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class ResourceCategoryGrantsController {

	private final ResourceCategoryGrantsService resourceCategoryGrantsService;

	public ResourceCategoryGrantsController(final ResourceCategoryGrantsService resourceCategoryGrantsService) {
		this.resourceCategoryGrantsService = resourceCategoryGrantsService;
	}

	@GetMapping("/category-grants")
	public ResponseEntity<Map<UUID, List<UUID>>> getResourceCategoryGrants(@PathVariable final UUID projectId) {
		return ResponseEntity.ok(resourceCategoryGrantsService.getResourceCategoryGrants(projectId));
	}

	@PutMapping("/category-grants")
	public ResponseEntity<Void> saveResourceCategoryGrants(
		@PathVariable final UUID projectId,
		@RequestBody final Map<UUID, List<UUID>> profileResourceCategoryMap
	) {
		resourceCategoryGrantsService.saveResourceCategoryGrants(projectId, profileResourceCategoryMap);
		return ResponseEntity.noContent().build();
	}
}
