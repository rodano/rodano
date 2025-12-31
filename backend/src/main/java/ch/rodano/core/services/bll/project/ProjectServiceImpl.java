package ch.rodano.core.services.bll.project;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ch.rodano.core.model.project.Project;
import ch.rodano.core.services.dao.project.ProjectDAOService;

@Service
public class ProjectServiceImpl implements ProjectService {

	private final ProjectDAOService projectDAOService;

	public ProjectServiceImpl(final ProjectDAOService projectDAOService) {
		this.projectDAOService = projectDAOService;
	}

	@Override
	public List<Project> getAllProjects() {
		return projectDAOService.getAllProjects();
	}

	@Override
	public List<Project> getProjectsForActor(final Long actorPk) {
		return projectDAOService.getProjectsForActor(actorPk);
	}

	@Override
	public Project getProjectById(final UUID projectId) {
		return projectDAOService.getProjectById(projectId);
	}

	@Override
	public void updateProject(final Project project) {
		projectDAOService.updateProject(project);
	}
}
