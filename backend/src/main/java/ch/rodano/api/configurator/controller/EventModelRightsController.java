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
import ch.rodano.core.services.bll.configurator.EventModelRightsService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class EventModelRightsController {

	private final EventModelRightsService eventModelRightsService;

	public EventModelRightsController(final EventModelRightsService eventModelRightsService) {
		this.eventModelRightsService = eventModelRightsService;
	}

	@GetMapping("/event-model-rights")
	public ResponseEntity<Map<UUID, Map<UUID, EntityRightDTO>>> getEventModelRights(@PathVariable final UUID projectId) {
		return ResponseEntity.ok(eventModelRightsService.getEventModelRights(projectId));
	}

	@PutMapping("/event-model-rights")
	public ResponseEntity<Void> saveEventModelRights(
		@PathVariable final UUID projectId,
		@RequestBody final Map<UUID, Map<UUID, EntityRightDTO>> rights
	) {
		eventModelRightsService.saveEventModelRights(projectId, rights);
		return ResponseEntity.noContent().build();
	}
}
