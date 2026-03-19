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

import ch.rodano.api.config.MenuConfigDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.MenuConfigService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class MenuConfigController {

	private final MenuConfigService menuConfigService;

	public MenuConfigController(final MenuConfigService menuConfigService) {
		this.menuConfigService = menuConfigService;
	}

	/**
	 * Get all menus for a project
	 */
	@GetMapping("/menus")
	public ResponseEntity<List<MenuConfigDTO>> getMenus(@PathVariable final UUID projectId) {
		final var menus = menuConfigService.getMenus(projectId);
		return ResponseEntity.ok(menus);
	}

	/**
	 * Get a specific menu
	 */
	@GetMapping("/menus/{menuId}")
	public ResponseEntity<MenuConfigDTO> getMenu(
		@PathVariable final UUID projectId,
		@PathVariable final UUID menuId
	) {
		final var menu = menuConfigService.getMenu(projectId, menuId);
		return ResponseEntity.ok(menu);
	}

	/**
	 * Create a new menu
	 */
	@PostMapping("/menus")
	@SkipProjectAccessCheck
	public ResponseEntity<MenuConfigDTO> createMenu(
		@PathVariable final UUID projectId,
		@RequestBody final MenuConfigDTO menu
	) {
		final var created = menuConfigService.createMenu(projectId, menu);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing menu
	 */
	@PutMapping("/menus/{menuId}")
	@SkipProjectAccessCheck
	public ResponseEntity<MenuConfigDTO> updateMenu(
		@PathVariable final UUID projectId,
		@PathVariable final UUID menuId,
		@RequestBody final MenuConfigDTO menu
	) {
		final var updated = menuConfigService.updateMenu(projectId, menuId, menu);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a menu
	 */
	@DeleteMapping("/menus/{menuId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteMenu(
		@PathVariable final UUID projectId,
		@PathVariable final UUID menuId
	) {
		menuConfigService.deleteMenu(projectId, menuId);
		return ResponseEntity.noContent().build();
	}
}
