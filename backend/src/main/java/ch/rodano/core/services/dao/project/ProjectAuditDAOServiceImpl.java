package ch.rodano.core.services.dao.project;

import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.jooq.enums.ProjectAuditStatus;

import static ch.rodano.core.model.jooq.tables.Project.PROJECT;
import static ch.rodano.core.model.jooq.tables.ProjectAudit.PROJECT_AUDIT;

@Service
public class ProjectAuditDAOServiceImpl implements ProjectAuditDAOService {

	private final DSLContext dslContext;

	public ProjectAuditDAOServiceImpl(final DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Override
	public void createAudit(final UUID projectId, final DatabaseActionContext context) {
		final var auditAction = context.auditAction();

		final var projectRecord = dslContext.selectFrom(PROJECT)
			.where(PROJECT.PROJECT_ID.eq(projectId))
			.fetchOne();

		if(projectRecord == null) {
			throw new IllegalStateException("No project record found for project id: " + projectId);
		}

		dslContext.insertInto(PROJECT_AUDIT)
			.set(PROJECT_AUDIT.PROJECT_ID, projectRecord.get(PROJECT.PROJECT_ID))
			.set(PROJECT_AUDIT.AUDIT_ACTION_FK, auditAction.getPk())
			.set(PROJECT_AUDIT.AUDIT_DATETIME, auditAction.getDate())
			.set(PROJECT_AUDIT.AUDIT_ACTOR, context.getActorName())
			.set(PROJECT_AUDIT.AUDIT_USER_FK, auditAction.getUserFk())
			.set(PROJECT_AUDIT.AUDIT_ROBOT_FK, auditAction.getRobotFk())
			.set(PROJECT_AUDIT.AUDIT_CONTEXT, auditAction.getContext())
			.set(PROJECT_AUDIT.CODE, projectRecord.getCode())
			.set(PROJECT_AUDIT.STATUS, ProjectAuditStatus.valueOf(projectRecord.getStatus().getLiteral()))
			.set(PROJECT_AUDIT.SHORTNAME, projectRecord.getShortname())
			.set(PROJECT_AUDIT.CREATED, projectRecord.getCreated())
			.execute();
	}
}
