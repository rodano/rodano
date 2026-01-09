package ch.rodano.api.config;

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

import ch.rodano.api.configurator.ConfiguratorProjectDTO;
import ch.rodano.api.configurator.CreateProjectRequest;
import ch.rodano.api.configurator.ProjectConfigVersionDTO;
import ch.rodano.core.services.bll.configurator.ConfiguratorService;

@RestController
@RequestMapping("/api/configurator")
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
	public ResponseEntity<ConfiguratorProjectDTO> createProject(@RequestBody final CreateProjectRequest request) {
		final var project = configuratorService.createProject(request);
		return ResponseEntity.ok(project);
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
	 * Publish draft version, making it the active configuration
	 */
	@PutMapping("/projects/{projectId}/versions/{versionId}/publish")
	public ResponseEntity<Void> publishDraft(
		@PathVariable final UUID projectId,
		@PathVariable final Long versionId,
		@RequestBody final PublishRequest request
	) {
		configuratorService.publishDraft(projectId, versionId, request.changeSummary());
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
	 * Request record for publishing a draft
	 */
	public record PublishRequest(String changeSummary) {
	}
}
