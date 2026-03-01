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

import ch.rodano.api.config.ResourceCategoryDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.ResourceCategoryService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class ResourceCategoryController {

	private final ResourceCategoryService resourceCategoryService;

	public ResourceCategoryController(final ResourceCategoryService resourceCategoryService) {
		this.resourceCategoryService = resourceCategoryService;
	}

	/**
	 * Get all resource categories for a project
	 */
	@GetMapping("/resource-categories")
	public ResponseEntity<List<ResourceCategoryDTO>> getResourceCategories(@PathVariable final UUID projectId) {
		final var resourceCategories = resourceCategoryService.getResourceCategories(projectId);
		return ResponseEntity.ok(resourceCategories);
	}

	/**
	 * Get a specific resource category
	 */
	@GetMapping("/resource-categories/{resourceCategoryId}")
	public ResponseEntity<ResourceCategoryDTO> getResourceCategory(
		@PathVariable final UUID projectId,
		@PathVariable final UUID resourceCategoryId
	) {
		final var resourceCategory = resourceCategoryService.getResourceCategory(projectId, resourceCategoryId);
		return ResponseEntity.ok(resourceCategory);
	}

	/**
	 * Create a new resource category
	 */
	@PostMapping("/resource-categories")
	@SkipProjectAccessCheck
	public ResponseEntity<ResourceCategoryDTO> createResourceCategory(
		@PathVariable final UUID projectId,
		@RequestBody final ResourceCategoryDTO resourceCategory
	) {
		final var created = resourceCategoryService.createResourceCategory(projectId, resourceCategory);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing resource category
	 */
	@PutMapping("/resource-categories/{resourceCategoryId}")
	@SkipProjectAccessCheck
	public ResponseEntity<ResourceCategoryDTO> updateResourceCategory(
		@PathVariable final UUID projectId,
		@PathVariable final UUID resourceCategoryId,
		@RequestBody final ResourceCategoryDTO resourceCategory
	) {
		final var updated = resourceCategoryService.updateResourceCategory(projectId, resourceCategoryId, resourceCategory);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a resource category
	 */
	@DeleteMapping("/resource-categories/{resourceCategoryId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteResourceCategory(
		@PathVariable final UUID projectId,
		@PathVariable final UUID resourceCategoryId
	) {
		resourceCategoryService.deleteResourceCategory(projectId, resourceCategoryId);
		return ResponseEntity.noContent().build();
	}
}
