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

import ch.rodano.core.services.bll.configurator.MenuGrantsConfigService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class MenuGrantsConfigController {

	private final MenuGrantsConfigService menuGrantsConfigService;

	public MenuGrantsConfigController(final MenuGrantsConfigService menuGrantsConfigService) {
		this.menuGrantsConfigService = menuGrantsConfigService;
	}

	@GetMapping("/menu-grants")
	public ResponseEntity<Map<UUID, List<UUID>>> getMenuGrants(@PathVariable final UUID projectId) {
		return ResponseEntity.ok(menuGrantsConfigService.getMenuGrants(projectId));
	}

	@PutMapping("/menu-grants")
	public ResponseEntity<Void> saveMenuGrants(
		@PathVariable final UUID projectId,
		@RequestBody final Map<UUID, List<UUID>> profileMenuMap
	) {
		menuGrantsConfigService.saveMenuGrants(projectId, profileMenuMap);
		return ResponseEntity.noContent().build();
	}
}
