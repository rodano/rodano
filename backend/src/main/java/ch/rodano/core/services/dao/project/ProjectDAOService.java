package ch.rodano.core.services.dao.project;

import java.util.List;
import java.util.UUID;

import ch.rodano.core.model.project.Project;

public interface ProjectDAOService {

	/**
	 * Get all projects in the system
	 */
	List<Project> getAllProjects();

	/**
	 * Get all projects where the actor has at least one role
	 *
	 * @param actorPk The actor's primary key (user or robot)
	 */
	List<Project> getProjectsForActor(Long actorPk);

	/**
	 * Get a specific project by its ID
	 *
	 * @param projectId The project's UUID
	 */
	Project getProjectById(UUID projectId);

	/**
	 * Update the project
	 *
	 * @param project Entire project
	 */
	void updateProject(Project project);
}
