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

import ch.rodano.api.config.TimelineGraphSectionDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.TimelineGraphSectionService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config/timeline-graphs/{timelineGraphId}")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class TimelineGraphSectionController {

	private final TimelineGraphSectionService timelineGraphSectionService;

	public TimelineGraphSectionController(final TimelineGraphSectionService timelineGraphSectionService) {
		this.timelineGraphSectionService = timelineGraphSectionService;
	}

	/**
	 * Get all timeline graph sections for a project
	 */
	@GetMapping("/sections")
	public ResponseEntity<List<TimelineGraphSectionDTO>> getSections(
		@PathVariable final UUID projectId,
		@PathVariable final UUID timelineGraphId
	) {
		final var sections = timelineGraphSectionService.getSections(projectId, timelineGraphId);
		return ResponseEntity.ok(sections);
	}

	/**
	 * Get a specific timeline graph section
	 */
	@GetMapping("/sections/{sectionId}")
	public ResponseEntity<TimelineGraphSectionDTO> getSection(
		@PathVariable final UUID projectId,
		@PathVariable final UUID timelineGraphId,
		@PathVariable final UUID sectionId
	) {
		final var section = timelineGraphSectionService.getSection(projectId, timelineGraphId, sectionId);
		return ResponseEntity.ok(section);
	}

	/**
	 * Create a new timeline graph section
	 */
	@PostMapping("/sections")
	@SkipProjectAccessCheck
	public ResponseEntity<TimelineGraphSectionDTO> createSection(
		@PathVariable final UUID projectId,
		@PathVariable final UUID timelineGraphId,
		@RequestBody final TimelineGraphSectionDTO section
	) {
		final var created = timelineGraphSectionService.createSection(projectId, timelineGraphId, section);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing timeline graph section
	 */
	@PutMapping("/sections/{sectionId}")
	@SkipProjectAccessCheck
	public ResponseEntity<TimelineGraphSectionDTO> updateSection(
		@PathVariable final UUID projectId,
		@PathVariable final UUID timelineGraphId,
		@PathVariable final UUID sectionId,
		@RequestBody final TimelineGraphSectionDTO section
	) {
		final var updated = timelineGraphSectionService.updateSection(projectId, timelineGraphId, sectionId, section);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a timeline graph section
	 */
	@DeleteMapping("/sections/{sectionId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteSection(
		@PathVariable final UUID projectId,
		@PathVariable final UUID timelineGraphId,
		@PathVariable final UUID sectionId
	) {
		timelineGraphSectionService.deleteSection(projectId, timelineGraphId, sectionId);
		return ResponseEntity.noContent().build();
	}
}
