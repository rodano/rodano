package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.configurator.ConfiguratorProjectDTO;
import ch.rodano.api.configurator.CreateProjectRequest;
import ch.rodano.api.configurator.ProjectConfigVersionDTO;

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
	 * Get all configuration versions for a project
	 */
	List<ProjectConfigVersionDTO> getVersions(UUID projectId);

	/**
	 * Get a specific configuration version
	 */
	ProjectConfigVersionDTO getVersion(UUID projectId, Long versionId);
}
