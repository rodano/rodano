package ch.rodano.core.services.bll.project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ch.rodano.core.constants.SystemConstants;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.project.Project;
import ch.rodano.core.services.dao.audit.AuditActionService;
import ch.rodano.core.services.dao.project.ProjectAuditDAOService;
import ch.rodano.core.services.dao.project.ProjectDAOService;
import ch.rodano.core.services.dao.user.UserDAOService;

@Service
public class ProjectServiceImpl implements ProjectService {

	private final ProjectDAOService projectDAOService;
	private final AuditActionService auditActionService;
	private final ProjectAuditDAOService projectAuditDAOService;
	private final UserDAOService userDAOService;

	public ProjectServiceImpl(final ProjectDAOService projectDAOService,
							  final AuditActionService auditActionService,
							  final ProjectAuditDAOService projectAuditDAOService,
							  final UserDAOService userDAOService) {
		this.projectDAOService = projectDAOService;
		this.auditActionService = auditActionService;
		this.projectAuditDAOService = projectAuditDAOService;
		this.userDAOService = userDAOService;
	}

	@Override
	public List<Project> getAllProjects() {
		return projectDAOService.getAllProjects()
			.stream()
			.filter(p -> !SystemConstants.SYSTEM_PROJECT_ID.equals(p.getProjectId()))
			.collect(Collectors.toList());
	}

	@Override
	public List<Project> getProjectsForActor(final Long actorPk) {
		final var user = userDAOService.getUserByPk(actorPk);

		final List<Project> projects;
		if(user.isSuperuser()) {
			projects = projectDAOService.getAllProjects();
		}
		else {
			projects = projectDAOService.getProjectsForActor(actorPk);
		}

		return projects.stream()
			.filter(p -> !SystemConstants.SYSTEM_PROJECT_ID.equals(p.getProjectId()))
			.collect(Collectors.toList());
	}

	@Override
	public Project getProjectById(final UUID projectId) {
		return projectDAOService.getProjectById(projectId);
	}

	@Override
	public void updateProject(final Project project) {
		projectDAOService.updateProject(project);
	}

	@Override
	public void updateProjectWithActor(final Project project, final Optional<Actor> actor, final String auditContext) {
		projectDAOService.updateProject(project);

		final var actionContext = auditActionService.createAuditActionAndGenerateContext(
			actor,
			auditContext,
			project.getProjectId()
		);

		projectAuditDAOService.createAudit(project.getProjectId(), actionContext);
	}
}
