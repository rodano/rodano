package ch.rodano.core.services.dao.project;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import ch.rodano.core.constants.SystemConstants;
import ch.rodano.core.dao.MappingHelper;
import ch.rodano.core.model.jooq.tables.records.ProjectRecord;
import ch.rodano.core.model.project.Project;

import static ch.rodano.core.model.jooq.Tables.PROJECT;
import static ch.rodano.core.model.jooq.Tables.ROLE;

@Service
public class ProjectDAOServiceImpl implements ProjectDAOService {

	private final DSLContext create;
	private final MappingHelper mappingHelper;

	public ProjectDAOServiceImpl(final DSLContext create, final MappingHelper mappingHelper) {
		this.create = create;
		this.mappingHelper = mappingHelper;
	}

	@Override
	public List<Project> getAllProjects() {
		return create.selectFrom(PROJECT)
			.where(PROJECT.PROJECT_ID.ne(SystemConstants.SYSTEM_PROJECT_ID))
			.fetch(this::mapToProject);
	}

	@Override
	public List<Project> getProjectsForActor(final Long actorPk) {
		return create.selectDistinct(PROJECT.asterisk())
			.from(PROJECT)
			.innerJoin(ROLE).on(ROLE.PROJECT_ID.eq(PROJECT.PROJECT_ID))
			.where(ROLE.USER_FK.eq(actorPk)
				.or(ROLE.ROBOT_FK.eq(actorPk))
			)
			.and(PROJECT.PROJECT_ID.ne(SystemConstants.SYSTEM_PROJECT_ID))
			.fetchInto(PROJECT)
			.stream()
			.map(this::mapToProject)
			.toList();
	}

	@Override
	public Project getProjectById(final UUID projectId) {
		return create.selectFrom(PROJECT)
			.where(PROJECT.PROJECT_ID.eq(projectId))
			.fetchOne(this::mapToProject);
	}

	private Project mapToProject(final ProjectRecord record) {
		if(record == null) {
			return null;
		}

		final var project = new Project();
		project.setProjectId(record.getProjectId());
		project.setCode(record.getCode());
		project.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		project.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		project.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));
		project.setUrl(record.getUrl());
		project.setEmail(record.getEmail());
		project.setColor(record.getColor());
		project.setIntroductionText(record.getIntroductionText());
		project.setVersionDate(record.getVersionDate());
		project.setStatus(record.getStatus());
		project.setCreatedDate(record.getCreated());
		project.setActiveConfigVersionFk(record.getActiveConfigVersionFk());

		return project;
	}

	@Override
	public void updateProject(final Project project) {
		create.update(PROJECT)
			.set(PROJECT.STATUS, project.getStatus())
			.where(PROJECT.PROJECT_ID.eq(project.getProjectId()))
			.execute();
	}
}
