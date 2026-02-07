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

import ch.rodano.api.config.EventGroupDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.EventGroupService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class EventGroupController {

	private final EventGroupService eventGroupService;

	public EventGroupController(final EventGroupService eventGroupService) {
		this.eventGroupService = eventGroupService;
	}

	/**
	 * Get all event groups for a project
	 */
	@GetMapping("/event-groups")
	public ResponseEntity<List<EventGroupDTO>> getEventGroups(@PathVariable final UUID projectId) {
		final var eventGroups = eventGroupService.getEventGroups(projectId);
		return ResponseEntity.ok(eventGroups);
	}

	/**
	 * Get a specific event group
	 */
	@GetMapping("/event-groups/{eventGroupId}")
	public ResponseEntity<EventGroupDTO> getEventGroup(
		@PathVariable final UUID projectId,
		@PathVariable final UUID eventGroupId
	) {
		final var eventGroup = eventGroupService.getEventGroup(projectId, eventGroupId);
		return ResponseEntity.ok(eventGroup);
	}

	/**
	 * Create a new event group
	 */
	@PostMapping("/event-groups")
	@SkipProjectAccessCheck
	public ResponseEntity<EventGroupDTO> createEventGroup(
		@PathVariable final UUID projectId,
		@RequestBody final EventGroupDTO eventGroup
	) {
		final var created = eventGroupService.createEventGroup(projectId, eventGroup);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing event group
	 */
	@PutMapping("/event-groups/{eventGroupId}")
	@SkipProjectAccessCheck
	public ResponseEntity<EventGroupDTO> updateEventGroup(
		@PathVariable final UUID projectId,
		@PathVariable final UUID eventGroupId,
		@RequestBody final EventGroupDTO eventGroup
	) {
		final var updated = eventGroupService.updateEventGroup(projectId, eventGroupId, eventGroup);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete an event group
	 */
	@DeleteMapping("/event-groups/{eventGroupId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteEventGroup(
		@PathVariable final UUID projectId,
		@PathVariable final UUID eventGroupId
	) {
		eventGroupService.deleteEventGroup(projectId, eventGroupId);
		return ResponseEntity.noContent().build();
	}
}
