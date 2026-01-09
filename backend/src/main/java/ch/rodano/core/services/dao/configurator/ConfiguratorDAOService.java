package ch.rodano.core.services.dao.configurator;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import ch.rodano.api.configurator.ConfiguratorProjectDTO;
import ch.rodano.api.configurator.CreateProjectRequest;
import ch.rodano.api.configurator.ProjectConfigVersionDTO;

public interface ConfiguratorDAOService {

	/**
	 * Get all projects with their version information
	 */
	List<ConfiguratorProjectDTO> getAllProjects();

	/**
	 * Get a single project with version information
	 */
	ConfiguratorProjectDTO getProject(UUID projectId);

	/**
	 * Check if a project code already exists
	 */
	boolean projectCodeExists(String code);

	/**
	 * Create a new project
	 */
	ConfiguratorProjectDTO createProject(CreateProjectRequest request);

	/**
	 * Get the current draft version for a project (if any)
	 */
	ProjectConfigVersionDTO getDraftVersion(UUID projectId);

	/**
	 * Get the current active (published) version for a project
	 */
	ProjectConfigVersionDTO getActiveVersion(UUID projectId);

	/**
	 * Create a new configuration version based on an existing one
	 *
	 * @param draft            The new version to create
	 * @param basedOnVersionId The version to copy configuration from
	 */
	ProjectConfigVersionDTO createVersion(ProjectConfigVersionDTO draft, Long basedOnVersionId);

	/**
	 * Publish a draft version
	 */
	void publishVersion(Long versionId, Long publishedByUserId, ZonedDateTime publishedAt, String changeSummary);

	/**
	 * Archive a version (when a new version is published)
	 */
	void archiveVersion(Long versionId);

	/**
	 * Update the project's active config version pointer
	 */
	void updateProjectActiveVersion(UUID projectId, Long versionId);

	/**
	 * Get all versions for a project
	 */
	List<ProjectConfigVersionDTO> getVersions(UUID projectId);

	/**
	 * Get a specific version
	 */
	ProjectConfigVersionDTO getVersion(UUID projectId, Long versionId);
}
