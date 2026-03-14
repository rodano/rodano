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

import ch.rodano.api.config.TimelineGraphDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.TimelineGraphService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class TimelineGraphController {

	private final TimelineGraphService timelineGraphService;

	public TimelineGraphController(final TimelineGraphService timelineGraphService) {
		this.timelineGraphService = timelineGraphService;
	}

	/**
	 * Get all timeline graphs for a project
	 */
	@GetMapping("/timeline-graphs")
	public ResponseEntity<List<TimelineGraphDTO>> getTimelineGraphs(@PathVariable final UUID projectId) {
		final var TimelineGraphs = timelineGraphService.getTimelineGraphs(projectId);
		return ResponseEntity.ok(TimelineGraphs);
	}

	/**
	 * Get a specific timeline graph
	 */
	@GetMapping("/timeline-graphs/{timelineGraphId}")
	public ResponseEntity<TimelineGraphDTO> getTimelineGraph(
		@PathVariable final UUID projectId,
		@PathVariable final UUID timelineGraphId
	) {
		final var timelineGraph = timelineGraphService.getTimelineGraph(projectId, timelineGraphId);
		return ResponseEntity.ok(timelineGraph);
	}

	/**
	 * Create a new timeline graph
	 */
	@PostMapping("/timeline-graphs")
	@SkipProjectAccessCheck
	public ResponseEntity<TimelineGraphDTO> createTimelineGraph(
		@PathVariable final UUID projectId,
		@RequestBody final TimelineGraphDTO timelineGraph
	) {
		final var created = timelineGraphService.createTimelineGraph(projectId, timelineGraph);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing timeline graph
	 */
	@PutMapping("/timeline-graphs/{timelineGraphId}")
	@SkipProjectAccessCheck
	public ResponseEntity<TimelineGraphDTO> updateTimelineGraph(
		@PathVariable final UUID projectId,
		@PathVariable final UUID timelineGraphId,
		@RequestBody final TimelineGraphDTO timelineGraph
	) {
		final var updated = timelineGraphService.updateTimelineGraph(projectId, timelineGraphId, timelineGraph);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a timeline graph
	 */
	@DeleteMapping("/timeline-graphs/{timelineGraphId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteTimelineGraph(
		@PathVariable final UUID projectId,
		@PathVariable final UUID timelineGraphId
	) {
		timelineGraphService.deleteTimelineGraph(projectId, timelineGraphId);
		return ResponseEntity.noContent().build();
	}
}
