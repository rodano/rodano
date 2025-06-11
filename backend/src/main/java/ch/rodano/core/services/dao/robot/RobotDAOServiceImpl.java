package ch.rodano.core.services.dao.robot;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.RobotAuditTrail;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.RobotAuditRecord;
import ch.rodano.core.model.jooq.tables.records.RobotRecord;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.model.robot.RobotSearch;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AuditableDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.tables.Robot.ROBOT;
import static ch.rodano.core.model.jooq.tables.Role.ROLE;

@Service
public class RobotDAOServiceImpl extends AuditableDAOService<Robot, RobotAuditTrail, RobotRecord, RobotAuditRecord> implements RobotDAOService {

	public RobotDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService
	) {
		super(create, strategy, studyService);
	}

	@Override
	protected Table<RobotRecord> getTable() {
		return Tables.ROBOT;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Table<RobotAuditRecord> getAuditTable() {
		return Tables.ROBOT_AUDIT;
	}

	@Override
	protected Class<RobotAuditTrail> getEntityAuditClass() {
		return RobotAuditTrail.class;
	}

	@Override
	protected Class<Robot> getDAOClass() {
		return Robot.class;
	}

	@Override
	public void saveRobot(final Robot robot, final DatabaseActionContext context, final String rationale) {
		save(robot, context, rationale);
	}

	@Override
	public void deleteRobot(final Robot robot, final DatabaseActionContext context, final String rationale) {
		delete(robot, context, rationale);
	}

	@Override
	public void restoreRobot(final Robot robot, final DatabaseActionContext context, final String rationale) {
		restore(robot, context, rationale);
	}

	@Override
	public Robot getRobotByPk(final Long pk) {
		final var query = create.selectFrom(ROBOT).where(ROBOT.PK.eq(pk));
		return findUnique(query);
	}

	@Override
	public Robot getRobotByName(final String name) {
		final var query = create.selectFrom(ROBOT).where(ROBOT.NAME.eq(name));
		return findUnique(query);
	}

	@Override
	public Robot getRobotByKey(final String key) {
		//do not include deleted robots, this method is used for the authentication and deleted robots must not have the right to use the API
		final var query = create.selectFrom(ROBOT).where(ROBOT.KEY.eq(key).and(ROBOT.DELETED.isFalse()));
		return findUnique(query);
	}

	@Override
	public Robot getRobotByNameAndKey(final String name, final String key) {
		//do not include deleted robots, this method is used for the authentication and deleted robots must not have the right to use the API
		final var query = create.selectFrom(ROBOT).where(ROBOT.NAME.eq(name).and(ROBOT.KEY.eq(key)).and(ROBOT.DELETED.isFalse()));
		return findUnique(query);
	}

	@Override
	public PagedResult<Robot> search(final RobotSearch search) {
		final List<Condition> conditions = new ArrayList<>();

		search.getName().ifPresent(name -> {
			conditions.add(ROBOT.NAME.containsIgnoreCase(name));
		});

		search.getProfileId().ifPresent(profileId -> {
			conditions.add(ROLE.PROFILE_ID.eq(profileId));
		});

		if(!search.getIncludeDeleted()) {
			conditions.add(ROBOT.DELETED.isFalse());
		}

		final var query = create.select(ROBOT.asterisk(), DSL.count().over().as("total"))
			.from(ROBOT)
			.innerJoin(ROLE).on(ROBOT.PK.eq(ROLE.ROBOT_FK))
			.where(conditions)
			.orderBy(search.getSortBy().getField().sort(search.getOrder()))
			.limit(search.getLimitField())
			.offset(search.getOffsetField());

		final var result = query.fetch();
		var total = 0;
		if(result.size() > 0) {
			total = result.get(0).getValue("total", Integer.class);
		}

		final var robots = result.into(Robot.class);
		return new PagedResult<>(robots, search.getPageSize(), search.getPageIndex(), total);
	}
}
