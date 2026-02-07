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

import ch.rodano.api.config.EventModelDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.EventModelService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class EventModelController {

	private final EventModelService eventModelService;

	public EventModelController(final EventModelService eventModelService) {
		this.eventModelService = eventModelService;
	}

	/**
	 * Get all event models for a project
	 */
	@GetMapping("/event-models")
	public ResponseEntity<List<EventModelDTO>> getEventModels(@PathVariable final UUID projectId) {
		final var eventModels = eventModelService.getEventModels(projectId);
		return ResponseEntity.ok(eventModels);
	}

	/**
	 * Get a specific event model
	 */
	@GetMapping("/event-models/{eventModelId}")
	public ResponseEntity<EventModelDTO> getEventModel(
		@PathVariable final UUID projectId,
		@PathVariable final UUID eventModelId
	) {
		final var eventModel = eventModelService.getEventModel(projectId, eventModelId);
		return ResponseEntity.ok(eventModel);
	}

	/**
	 * Create a new event model
	 */
	@PostMapping("/event-models")
	@SkipProjectAccessCheck
	public ResponseEntity<EventModelDTO> createEventModel(
		@PathVariable final UUID projectId,
		@RequestBody final EventModelDTO eventModel
	) {
		final var created = eventModelService.createEventModel(projectId, eventModel);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing event model
	 */
	@PutMapping("/event-models/{eventModelId}")
	@SkipProjectAccessCheck
	public ResponseEntity<EventModelDTO> updateEventModel(
		@PathVariable final UUID projectId,
		@PathVariable final UUID eventModelId,
		@RequestBody final EventModelDTO eventModel
	) {
		final var updated = eventModelService.updateEventModel(projectId, eventModelId, eventModel);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete an event model
	 */
	@DeleteMapping("/event-models/{eventModelId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteEventModel(
		@PathVariable final UUID projectId,
		@PathVariable final UUID eventModelId
	) {
		eventModelService.deleteEventModel(projectId, eventModelId);
		return ResponseEntity.noContent().build();
	}
}
