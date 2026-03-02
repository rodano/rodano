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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.rodano.api.config.ChartModelDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.ChartService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class ChartController {

	private final ChartService chartService;

	public ChartController(final ChartService chartService) {
		this.chartService = chartService;
	}

	/**
	 * Get all charts for a project
	 */
	@GetMapping("/charts")
	public ResponseEntity<List<ChartModelDTO>> getCharts(
		@PathVariable final UUID projectId,
		@RequestParam(name = "view", required = false, defaultValue = "summary") final String view
	) {
		final var charts = chartService.getCharts(projectId, view);
		return ResponseEntity.ok(charts);
	}

	/**
	 * Get a specific chart
	 */
	@GetMapping("/charts/{chartId}")
	public ResponseEntity<ChartModelDTO> getChart(
		@PathVariable final UUID projectId,
		@PathVariable final UUID chartId
	) {
		final var Chart = chartService.getChart(projectId, chartId);
		return ResponseEntity.ok(Chart);
	}

	/**
	 * Create a new chart
	 */
	@PostMapping("/charts")
	@SkipProjectAccessCheck
	public ResponseEntity<ChartModelDTO> createChart(
		@PathVariable final UUID projectId,
		@RequestBody final ChartModelDTO chart
	) {
		final var created = chartService.createChart(projectId, chart);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing chart
	 */
	@PutMapping("/charts/{chartId}")
	@SkipProjectAccessCheck
	public ResponseEntity<ChartModelDTO> updateChart(
		@PathVariable final UUID projectId,
		@PathVariable final UUID chartId,
		@RequestBody final ChartModelDTO chart
	) {
		final var updated = chartService.updateChart(projectId, chartId, chart);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a chart
	 */
	@DeleteMapping("/charts/{chartId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteChart(
		@PathVariable final UUID projectId,
		@PathVariable final UUID chartId
	) {
		chartService.deleteChart(projectId, chartId);
		return ResponseEntity.noContent().build();
	}
}
