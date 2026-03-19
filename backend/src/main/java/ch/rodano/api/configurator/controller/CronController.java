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

import ch.rodano.api.config.CronDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.CronService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class CronController {

	private final CronService cronService;

	public CronController(final CronService cronService) {
		this.cronService = cronService;
	}

	/**
	 * Get all crons for a project
	 */
	@GetMapping("/crons")
	public ResponseEntity<List<CronDTO>> getCrons(@PathVariable final UUID projectId) {
		final var crons = cronService.getCrons(projectId);
		return ResponseEntity.ok(crons);
	}

	/**
	 * Get a specific cron
	 */
	@GetMapping("/crons/{cronId}")
	public ResponseEntity<CronDTO> getCron(
		@PathVariable final UUID projectId,
		@PathVariable final UUID cronId
	) {
		final var cron = cronService.getCron(projectId, cronId);
		return ResponseEntity.ok(cron);
	}

	/**
	 * Create a new cron
	 */
	@PostMapping("/crons")
	@SkipProjectAccessCheck
	public ResponseEntity<CronDTO> createCron(
		@PathVariable final UUID projectId,
		@RequestBody final CronDTO cron
	) {
		final var created = cronService.createCron(projectId, cron);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing cron
	 */
	@PutMapping("/crons/{cronId}")
	@SkipProjectAccessCheck
	public ResponseEntity<CronDTO> updateCron(
		@PathVariable final UUID projectId,
		@PathVariable final UUID cronId,
		@RequestBody final CronDTO cron
	) {
		final var updated = cronService.updateCron(projectId, cronId, cron);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete a cron
	 */
	@DeleteMapping("/crons/{cronId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteCron(
		@PathVariable final UUID projectId,
		@PathVariable final UUID cronId
	) {
		cronService.deleteCron(projectId, cronId);
		return ResponseEntity.noContent().build();
	}
}
