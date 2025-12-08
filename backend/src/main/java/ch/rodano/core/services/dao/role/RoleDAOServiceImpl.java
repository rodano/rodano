package ch.rodano.core.services.dao.role;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.RoleAuditTrail;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.RoleAuditRecord;
import ch.rodano.core.model.jooq.tables.records.RoleRecord;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.role.RoleStatus;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AuditableDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.Tables.ROLE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;

@Service
public class RoleDAOServiceImpl extends AuditableDAOService<Role, RoleAuditTrail, RoleRecord, RoleAuditRecord> implements RoleDAOService {

	public RoleDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService
	) {
		super(create, strategy, studyService);
	}

	@Override
	protected Table<RoleRecord> getTable() {
		return Tables.ROLE;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Table<RoleAuditRecord> getAuditTable() {
		return Tables.ROLE_AUDIT;
	}

	@Override
	protected Class<RoleAuditTrail> getEntityAuditClass() {
		return RoleAuditTrail.class;
	}

	@Override
	protected Class<Role> getDAOClass() {
		return Role.class;
	}

	private UUID currentProjectId() {
		return studyService.isStudyLoaded() ? studyService.getStudy().getProjectId() : null;
	}

	@Override
	public Role getRoleByPk(final Long pk) {
		var condition = ROLE.PK.eq(pk);
		if (currentProjectId() != null) {
			condition = condition.and(ROLE.PROJECT_ID.eq(currentProjectId()));
		}
		final var query = create.selectFrom(ROLE).where(condition);
		return findUnique(query);
	}

	@Override
	public List<Role> getRolesByUserPk(final Long userPk) {
		var condition = ROLE.USER_FK.eq(userPk);
		if (currentProjectId() != null) {
			condition = condition.and(ROLE.PROJECT_ID.eq(currentProjectId()));
		}
		final var query = create.selectFrom(ROLE).where(condition);
		return find(query);
	}

	@Override
	public List<Role> getRolesByUserPks(final Collection<Long> userPks) {
		var condition = ROLE.USER_FK.in(userPks);
		if (currentProjectId() != null) {
			condition = condition.and(ROLE.PROJECT_ID.eq(currentProjectId()));
		}
		final var query = create.selectFrom(ROLE).where(condition);
		return find(query);
	}

	@Override
	public List<Role> getRolesByRobotPk(final Long robotPk) {
		var condition = ROLE.ROBOT_FK.eq(robotPk);
		if (currentProjectId() != null) {
			condition = condition.and(ROLE.PROJECT_ID.eq(currentProjectId()));
		}
		final var query = create.selectFrom(ROLE).where(condition);
		return find(query);
	}

	@Override
	public List<Role> getRolesByRobotPks(final Collection<Long> robotPks) {
		var condition = ROLE.ROBOT_FK.in(robotPks);
		if (currentProjectId() != null) {
			condition = condition.and(ROLE.PROJECT_ID.eq(currentProjectId()));
		}
		final var query = create.selectFrom(ROLE).where(condition);
		return find(query);
	}

	@Override
	public List<Role> getRolesByProfile(final UUID profileId) {
		var condition = ROLE.PROFILE_ID.eq(profileId);
		if (currentProjectId() != null) {
			condition = condition.and(ROLE.PROJECT_ID.eq(currentProjectId()));
		}
		final var query = create.selectFrom(ROLE).where(condition);
		return find(query);
	}

	@Override
	public List<Role> getRolesByScopePkAndProfiles(final Long scopePk, final Collection<String> profileIds) {
		var condition = ROLE.SCOPE_FK.eq(scopePk).and(ROLE.PROFILE_ID.in(profileIds));
		if (currentProjectId() != null) {
			condition = condition.and(ROLE.PROJECT_ID.eq(currentProjectId()));
		}
		final var query = create.selectFrom(ROLE).where(condition);
		return find(query);
	}

	@Override
	public List<Role> getActiveRolesByUserPkOverScopePk(final Long userPk, final Long scopePk) {
		var condition =
			ROLE.USER_FK.eq(userPk)
				.and(ROLE.STATUS.eq(RoleStatus.ENABLED))
				.and(ROLE.SCOPE_FK.eq(scopePk).or(SCOPE_ANCESTOR.SCOPE_FK.eq(scopePk)));

		if (currentProjectId() != null) {
			condition = condition.and(ROLE.PROJECT_ID.eq(currentProjectId()));
		}
		final var query = create.selectDistinct(ROLE.asterisk())
			.from(ROLE)
			.leftJoin(SCOPE_ANCESTOR).on(ROLE.SCOPE_FK.eq(SCOPE_ANCESTOR.ANCESTOR_FK))
			.where(condition)
			.coerce(ROLE);

		return find(query);
	}

	@Override
	public List<Role> getActiveRolesByRobotPkOverScopePk(final Long robotPk, final Long scopePk) {
		var condition =
			ROLE.ROBOT_FK.eq(robotPk)
				.and(ROLE.STATUS.eq(RoleStatus.ENABLED))
				.and(ROLE.SCOPE_FK.eq(scopePk).or(SCOPE_ANCESTOR.SCOPE_FK.eq(scopePk)));

		if (currentProjectId() != null) {
			condition = condition.and(ROLE.PROJECT_ID.eq(currentProjectId()));
		}
		final var query = create.selectDistinct(ROLE.asterisk())
			.from(ROLE)
			.leftJoin(SCOPE_ANCESTOR).on(ROLE.SCOPE_FK.eq(SCOPE_ANCESTOR.ANCESTOR_FK))
			.where(condition)
			.coerce(ROLE);

		return find(query);
	}

	@Override
	public void saveRole(final Role role, final DatabaseActionContext context, final String rationale) {
		if(role.getProjectId() == null) {
			role.setProjectId(studyService.getStudy().getProjectId());
		}

		save(role, context, rationale);
	}
}
