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

import ch.rodano.core.services.bll.configurator.ReportGrantsService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class ReportGrantsController {

	private final ReportGrantsService reportGrantsService;

	public ReportGrantsController(final ReportGrantsService reportGrantsService) {
		this.reportGrantsService = reportGrantsService;
	}

	@GetMapping("/report-grants")
	public ResponseEntity<Map<UUID, List<UUID>>> getReportGrants(@PathVariable final UUID projectId) {
		return ResponseEntity.ok(reportGrantsService.getReportGrants(projectId));
	}

	@PutMapping("/report-grants")
	public ResponseEntity<Void> saveReportGrants(
		@PathVariable final UUID projectId,
		@RequestBody final Map<UUID, List<UUID>> profileReportMap
	) {
		reportGrantsService.saveReportGrants(projectId, profileReportMap);
		return ResponseEntity.noContent().build();
	}
}
