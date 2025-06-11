package ch.rodano.core.services.dao.role;

import java.util.Collection;
import java.util.List;

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

	@Override
	public Role getRoleByPk(final Long pk) {
		final var query = create.selectFrom(ROLE).where(ROLE.PK.eq(pk));
		return findUnique(query);
	}

	@Override
	public List<Role> getRolesByUserPk(final Long userPk) {
		final var query = create.selectFrom(ROLE).where(ROLE.USER_FK.eq(userPk));
		return find(query);
	}

	@Override
	public List<Role> getRolesByUserPks(final Collection<Long> userPks) {
		final var query = create.selectFrom(ROLE).where(ROLE.USER_FK.in(userPks));
		return find(query);
	}

	@Override
	public List<Role> getRolesByRobotPk(final Long robotPk) {
		final var query = create.selectFrom(ROLE).where(ROLE.ROBOT_FK.eq(robotPk));
		return find(query);
	}

	@Override
	public List<Role> getRolesByRobotPks(final Collection<Long> robotPks) {
		final var query = create.selectFrom(ROLE).where(ROLE.ROBOT_FK.in(robotPks));
		return find(query);
	}

	@Override
	public List<Role> getRolesByProfile(final String profileId) {
		final var query = create.selectFrom(ROLE).where(ROLE.PROFILE_ID.eq(profileId));
		return find(query);
	}

	@Override
	public List<Role> getRolesByScopePkAndProfiles(final Long scopePk, final Collection<String> profileIds) {
		final var query = create.selectFrom(ROLE).where(ROLE.SCOPE_FK.eq(scopePk).and(ROLE.PROFILE_ID.in(profileIds)));
		return find(query);
	}

	@Override
	public List<Role> getActiveRolesByUserPkOverScopePk(final Long userPk, final Long scopePk) {
		final var query = create.selectDistinct(ROLE.asterisk())
			.from(ROLE)
			.leftJoin(SCOPE_ANCESTOR).on(ROLE.SCOPE_FK.eq(SCOPE_ANCESTOR.ANCESTOR_FK))
			.where(
				ROLE.USER_FK.eq(userPk)
					.and(ROLE.STATUS.eq(RoleStatus.ENABLED))
					.and(ROLE.SCOPE_FK.eq(scopePk).or(SCOPE_ANCESTOR.SCOPE_FK.eq(scopePk)))
			)
			.coerce(ROLE);
		return find(query);
	}

	@Override
	public List<Role> getActiveRolesByRobotPkOverScopePk(final Long robotPk, final Long scopePk) {
		final var query = create.selectDistinct(ROLE.asterisk())
			.from(ROLE)
			.leftJoin(SCOPE_ANCESTOR).on(ROLE.SCOPE_FK.eq(SCOPE_ANCESTOR.ANCESTOR_FK))
			.where(
				ROLE.ROBOT_FK.eq(robotPk)
					.and(ROLE.STATUS.eq(RoleStatus.ENABLED))
					.and(ROLE.SCOPE_FK.eq(scopePk).or(SCOPE_ANCESTOR.SCOPE_FK.eq(scopePk)))
			)
			.coerce(ROLE);
		return find(query);
	}

	@Override
	public void saveRole(final Role role, final DatabaseActionContext context, final String rationale) {
		save(role, context, rationale);
	}
}
