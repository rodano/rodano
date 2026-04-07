package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.configurator.dto.ConfigSnapshotDTO;
import ch.rodano.api.configurator.dto.ConfiguratorProjectDTO;
import ch.rodano.api.configurator.dto.ProjectConfigVersionDTO;
import ch.rodano.api.configurator.request.CreateProjectRequest;
import ch.rodano.api.configurator.request.UpdateProjectRequest;

public interface ConfiguratorService {

	/**
	 * Get all projects for the configurator landing page
	 */
	List<ConfiguratorProjectDTO> getAllProjects();

	/**
	 * Get a single project by ID
	 */
	ConfiguratorProjectDTO getProject(UUID projectId);

	/**
	 * Create a new project
	 */
	ConfiguratorProjectDTO createProject(CreateProjectRequest createProjectRequest);

	/**
	 * Update an existing project
	 */
	ConfiguratorProjectDTO updateProject(UUID projectId, UpdateProjectRequest updateProjectRequest);

	/**
	 * Get or create a draft configuration version for a project
	 * If a draft already exists, return it
	 * If not, create a new draft based on the active version
	 */
	ProjectConfigVersionDTO getOrCreateDraft(UUID projectId);

	/**
	 * Publish a draft version, making it the active configuration
	 */
	void publishDraft(UUID projectId, Long versionId, String changeSummary);

	/**
	 * Archive a draft configuration version
	 */
	void archiveDraft(UUID projectId, Long versionId);

	/**
	 * Restore an archived draft configuration version
	 */
	void restoreDraft(UUID projectId, Long versionId);

	/**
	 * Get all configuration versions for a project
	 */
	List<ProjectConfigVersionDTO> getVersions(UUID projectId);

	/**
	 * Get a specific configuration version
	 */
	ProjectConfigVersionDTO getVersion(UUID projectId, Long versionId);

	/**
	 * Create a snapshot of the current draft state
	 */
	void createSnapshot(UUID projectId, Long versionId, String summary);

	/**
	 * Rollback to previous snapshot
	 */
	void rollbackSnapshot(UUID projectId, Long versionId);

	/**
	 * Roll forward to next snapshot
	 */
	void rollForwardSnapshot(UUID projectId, Long versionId);

	/**
	 * Get all snapshots for a version
	 */
	ConfigSnapshotDTO getSnapshots(UUID projectId, Long versionId);
}
