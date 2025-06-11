package ch.rodano.core.services.dao.user;

import java.util.ArrayList;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.UserAuditTrail;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.UserAuditRecord;
import ch.rodano.core.model.jooq.tables.records.UserRecord;
import ch.rodano.core.model.role.RoleStatus;
import ch.rodano.core.model.user.User;
import ch.rodano.core.model.user.UserSearch;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AuditableDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.Tables.ROLE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;
import static ch.rodano.core.model.jooq.Tables.USER;

@Service
public class UserDAOServiceImpl extends AuditableDAOService<User, UserAuditTrail, UserRecord, UserAuditRecord> implements UserDAOService {
	public UserDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService
	) {
		super(create, strategy, studyService);
	}

	@Override
	protected Table<UserRecord> getTable() {
		return Tables.USER;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Table<UserAuditRecord> getAuditTable() {
		return Tables.USER_AUDIT;
	}

	@Override
	protected Class<UserAuditTrail> getEntityAuditClass() {
		return UserAuditTrail.class;
	}

	@Override
	protected Class<User> getDAOClass() {
		return User.class;
	}

	@Override
	public void saveUser(final User user, final DatabaseActionContext context, final String rationale) {
		save(user, context, rationale);
	}

	@Override
	public void deleteUser(final User user, final DatabaseActionContext context, final String rationale) {
		delete(user, context, rationale);
	}

	@Override
	public void restoreUser(final User user, final DatabaseActionContext context, final String rationale) {
		restore(user, context, rationale);
	}

	@Override
	public User getUserByPendingEmail(final String pendingEmail) {
		final var query = create.selectFrom(USER).where(USER.PENDING_EMAIL.likeIgnoreCase(pendingEmail));
		return findUnique(query);
	}

	@Override
	public User getUserByEmail(final String email) {
		final var query = create.selectFrom(USER).where(USER.EMAIL.likeIgnoreCase(email));
		return findUnique(query);
	}

	@Override
	public User getUserByActivationCode(final String activationCode) {
		final var query = create.selectFrom(USER).where(USER.ACTIVATION_CODE.eq(activationCode));
		return findUnique(query);
	}

	@Override
	public User getUserByVerificationCode(final String verificationCode) {
		final var query = create.selectFrom(USER).where(USER.EMAIL_VERIFICATION_CODE.eq(verificationCode));
		return findUnique(query);
	}

	@Override
	public User getUserByRecoveryCode(final String recoveryCode) {
		final var query = create.selectFrom(USER).where(USER.RECOVERY_CODE.eq(recoveryCode));
		return findUnique(query);
	}

	@Override
	public User getUserByResetCode(final String resetCode) {
		final var query = create.selectFrom(USER).where(USER.PASSWORD_RESET_CODE.eq(resetCode));
		return findUnique(query);
	}

	@Override
	public User getUserByPk(final Long pk) {
		final var query = create.selectFrom(USER).where(USER.PK.eq(pk));
		return findUnique(query);
	}

	@Override
	public PagedResult<User> search(final UserSearch search) {
		final var conditions = new ArrayList<Condition>();

		final var scopeDescendant = SCOPE_ANCESTOR.as("scope_descendant");
		search.getScopePks().ifPresent(scopePks -> {
			final var roleConditions = new ArrayList<Condition>();

			roleConditions.add(ROLE.SCOPE_FK.in(scopePks));

			search.getExtension().ifPresent(extension -> {
				if(extension.isGoingUp()) {
					roleConditions.add(scopeDescendant.SCOPE_FK.in(scopePks).and(ROLE.STATUS.eq(RoleStatus.ENABLED)));
				}
				if(extension.isGoingDown()) {
					roleConditions.add(SCOPE_ANCESTOR.ANCESTOR_FK.in(scopePks));
				}
			});

			conditions.add(DSL.or(roleConditions));
		});

		//select the user status
		search.getStates().ifPresent(states -> {
			conditions.add(ROLE.STATUS.in(states));
		});

		//select the allowed profiles
		search.getProfileIds().ifPresent(profileIds -> {
			conditions.add(ROLE.PROFILE_ID.in(profileIds));
		});

		//select the allowed features
		search.getFeatureId().ifPresent(featureId -> {
			//retrieve the profile containing the feature
			final var profileIds = studyService.getStudy().getProfiles().stream().filter(p -> p.hasFeature(featureId)).map(Profile::getId).toList();
			conditions.add(ROLE.PROFILE_ID.in(profileIds));
		});

		if(!search.getIncludeDeleted()) {
			conditions.add(USER.DELETED.isFalse());
		}

		if(search.getEnabled().isPresent()) {
			conditions.add(ROLE.STATUS.eq(RoleStatus.ENABLED));
		}

		//externally managed predicate
		search.getExternallyManaged().ifPresent(externallyManaged -> {
			conditions.add(USER.EXTERNALLY_MANAGED.eq(externallyManaged));
		});

		search.getEmail().ifPresent(email -> {
			conditions.add(USER.EMAIL.eq(email));
		});

		search.getFullText().ifPresent(fullText -> {
			conditions.add(USER.NAME.containsIgnoreCase(fullText).or(USER.EMAIL.containsIgnoreCase(fullText)));
		});

		final var query = create.select(USER.asterisk(), DSL.count().over().as("total"))
			.from(USER)
			.innerJoin(ROLE).on(ROLE.USER_FK.eq(USER.PK))
			.leftJoin(SCOPE_ANCESTOR).on(ROLE.SCOPE_FK.eq(SCOPE_ANCESTOR.SCOPE_FK))
			.leftJoin(scopeDescendant).on(ROLE.SCOPE_FK.eq(scopeDescendant.ANCESTOR_FK))
			.where(conditions)
			.groupBy(USER.PK)
			.orderBy(search.getSortBy().getField().sort(search.getOrder()))
			.limit(search.getLimitField())
			.offset(search.getOffsetField());

		final var result = query.fetch();
		var total = 0;
		if(result.size() > 0) {
			total = result.get(0).getValue("total", Integer.class);
		}

		final var users = result.into(User.class);
		return new PagedResult<>(users, search.getPageSize(), search.getPageIndex(), total);
	}

}
