package ch.rodano.api.configurator.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.rodano.api.configurator.dto.ConfigSnapshotDTO;
import ch.rodano.api.configurator.dto.ConfiguratorProjectDTO;
import ch.rodano.api.configurator.dto.ProjectConfigVersionDTO;
import ch.rodano.api.configurator.request.CreateProjectRequest;
import ch.rodano.api.configurator.request.UpdateProjectRequest;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.ConfiguratorService;

@RestController
@RequestMapping("/superuser/configurator")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class ConfiguratorController {

	private final ConfiguratorService configuratorService;

	public ConfiguratorController(final ConfiguratorService configuratorService) {
		this.configuratorService = configuratorService;
	}

	/**
	 * Get all projects with version info for configurator landing page
	 */
	@GetMapping("/projects")
	public ResponseEntity<List<ConfiguratorProjectDTO>> getAllProjects() {
		final var projects = configuratorService.getAllProjects();
		return ResponseEntity.ok(projects);
	}

	/**
	 * Get project details with version information
	 */
	@GetMapping("/projects/{projectId}")
	public ResponseEntity<ConfiguratorProjectDTO> getProject(@PathVariable final UUID projectId) {
		final var project = configuratorService.getProject(projectId);
		return ResponseEntity.ok(project);
	}

	/**
	 * Create new project with initial configuration
	 */
	@PostMapping("/projects")
	@SkipProjectAccessCheck
	public ResponseEntity<ConfiguratorProjectDTO> createProject(@RequestBody final CreateProjectRequest request) {
		final var project = configuratorService.createProject(request);
		return ResponseEntity.ok(project);
	}

	/**
	 * Update an existing project
	 */
	@PutMapping("/projects/{projectId}")
	@SkipProjectAccessCheck
	public ResponseEntity<ConfiguratorProjectDTO> updateProject(
		@PathVariable final UUID projectId,
		@RequestBody final UpdateProjectRequest request
	) {
		final var updatedProject = configuratorService.updateProject(projectId, request);
		return ResponseEntity.ok(updatedProject);
	}

	/**
	 * Get or create draft version for a project
	 * If a draft already exists, return it
	 * If not, create a new draft based on the active version
	 */
	@GetMapping("/projects/{projectId}/draft")
	public ResponseEntity<ProjectConfigVersionDTO> getOrCreateDraft(@PathVariable final UUID projectId) {
		final var draft = configuratorService.getOrCreateDraft(projectId);
		return ResponseEntity.ok(draft);
	}

	/**
	 * Publish draft version, making it an active project
	 */
	@PutMapping("/projects/{projectId}/versions/{versionId}/publish")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> publishDraft(
		@PathVariable final UUID projectId,
		@PathVariable final Long versionId,
		@RequestBody final PublishRequest request
	) {
		configuratorService.publishDraft(projectId, versionId, request.changeSummary());
		return ResponseEntity.ok().build();
	}

	/**
	 * Archive a draft configuration version
	 */
	@PutMapping("/projects/{projectId}/versions/{versionId}/archive")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> archiveDraft(
		@PathVariable final UUID projectId,
		@PathVariable final Long versionId
	) {
		configuratorService.archiveDraft(projectId, versionId);
		return ResponseEntity.ok().build();
	}

	/**
	 * Restore an archived draft configuration version
	 */
	@PutMapping("/projects/{projectId}/versions/{versionId}/restore")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> restoreDraftVersion(
		@PathVariable final UUID projectId,
		@PathVariable final Long versionId
	) {
		configuratorService.restoreDraft(projectId, versionId);
		return ResponseEntity.ok().build();
	}

	/**
	 * Get all configuration versions for a project
	 */
	@GetMapping("/projects/{projectId}/versions")
	public ResponseEntity<List<ProjectConfigVersionDTO>> getVersions(@PathVariable final UUID projectId) {
		final var versions = configuratorService.getVersions(projectId);
		return ResponseEntity.ok(versions);
	}

	/**
	 * Get a specific configuration version
	 */
	@GetMapping("/projects/{projectId}/versions/{versionId}")
	public ResponseEntity<ProjectConfigVersionDTO> getVersion(
		@PathVariable final UUID projectId,
		@PathVariable final Long versionId
	) {
		final var version = configuratorService.getVersion(projectId, versionId);
		return ResponseEntity.ok(version);
	}

	/**
	 * Create a snapshot of the current draft state
	 */
	@PostMapping("/projects/{projectId}/versions/{versionId}/snapshot")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> createSnapshot(
		@PathVariable final UUID projectId,
		@PathVariable final Long versionId,
		@RequestBody final SnapshotRequest request
	) {
		configuratorService.createSnapshot(projectId, versionId, request.summary());
		return ResponseEntity.ok().build();
	}

	/**
	 * Rollback to previous snapshot
	 */
	@PostMapping("/projects/{projectId}/versions/{versionId}/rollback")
	@SkipProjectAccessCheck
	public ResponseEntity<ConfiguratorProjectDTO> rollbackSnapshot(
		@PathVariable final UUID projectId,
		@PathVariable final Long versionId
	) {
		configuratorService.rollbackSnapshot(projectId, versionId);
		final var project = configuratorService.getProject(projectId);
		return ResponseEntity.ok(project);
	}

	/**
	 * Roll forward to next snapshot
	 */
	@PostMapping("/projects/{projectId}/versions/{versionId}/rollforward")
	@SkipProjectAccessCheck
	public ResponseEntity<ConfiguratorProjectDTO> rollForwardSnapshot(
		@PathVariable final UUID projectId,
		@PathVariable final Long versionId
	) {
		configuratorService.rollForwardSnapshot(projectId, versionId);
		final var project = configuratorService.getProject(projectId);
		return ResponseEntity.ok(project);
	}

	/**
	 * Get all snapshots
	 */
	@GetMapping("/projects/{projectId}/versions/{versionId}/snapshots")
	public ResponseEntity<ConfigSnapshotDTO> getSnapshots(
		@PathVariable final UUID projectId,
		@PathVariable final Long versionId
	) {
		final var snapshots = configuratorService.getSnapshots(projectId, versionId);
		return ResponseEntity.ok(snapshots);
	}

	/**
	 * Request record for publishing a draft
	 */
	public record PublishRequest(String changeSummary) {
	}

	/**
	 * Request record for creating a snapshot
	 */
	public record SnapshotRequest(String summary) {
	}
}
