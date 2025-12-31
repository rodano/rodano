package ch.rodano.core.services.bll.project;

import java.util.List;
import java.util.UUID;

import ch.rodano.core.model.project.Project;

public interface ProjectService {

	List<Project> getAllProjects();

	List<Project> getProjectsForActor(Long actorPk);

	Project getProjectById(UUID projectId);

	void updateProject(Project project);
}
