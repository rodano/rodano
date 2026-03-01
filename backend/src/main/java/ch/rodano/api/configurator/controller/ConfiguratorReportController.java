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

import ch.rodano.api.config.ReportDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.ConfiguratorReportService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class ConfiguratorReportController {

	private final ConfiguratorReportService reportService;

	public ConfiguratorReportController(final ConfiguratorReportService reportService) {
		this.reportService = reportService;
	}

	/**
	 * Get all reports for a project
	 */
	@GetMapping("/reports")
	public ResponseEntity<List<ReportDTO>> getReports(@PathVariable final UUID projectId) {
		final var reports = reportService.getReports(projectId);
		return ResponseEntity.ok(reports);
	}

	/**
	 * Get a specific report
	 */
	@GetMapping("/reports/{reportId}")
	public ResponseEntity<ReportDTO> getReport(
		@PathVariable final UUID projectId,
		@PathVariable final UUID reportId
	) {
		final var Report = reportService.getReport(projectId, reportId);
		return ResponseEntity.ok(Report);
	}

	/**
	 * Create a new report
	 */
	@PostMapping("/reports")
	@SkipProjectAccessCheck
	public ResponseEntity<ReportDTO> createReport(
		@PathVariable final UUID projectId,
		@RequestBody final ReportDTO report
	) {
		final var created = reportService.createReport(projectId, report);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing report
	 */
	@PutMapping("/reports/{reportId}")
	@SkipProjectAccessCheck
	public ResponseEntity<ReportDTO> updateReport(
		@PathVariable final UUID projectId,
		@PathVariable final UUID reportId,
		@RequestBody final ReportDTO report
	) {
		final var updated = reportService.updateReport(projectId, reportId, report);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a report
	 */
	@DeleteMapping("/reports/{reportId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteReport(
		@PathVariable final UUID projectId,
		@PathVariable final UUID reportId
	) {
		reportService.deleteReport(projectId, reportId);
		return ResponseEntity.noContent().build();
	}
}
