package ch.rodano.core.services.dao.commons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Function;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DSL;

import ch.rodano.core.model.audit.AuditTrail;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.common.AuditableObject;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.jooqutils.AuditRecord;
import ch.rodano.core.model.jooqutils.AuditTable;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

public abstract class AuditableDAOService<U extends AuditableObject, V extends AuditTrail, R extends Record, S extends UpdatableRecord<S> & AuditRecord> extends AbstractDAOService<U, R> {

	public AuditableDAOService(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService
	) {
		super(create, strategy, studyService);
	}

	protected abstract Class<V> getEntityAuditClass();

	protected abstract <T extends Table<S> & AuditTable> T getAuditTable();

	public NavigableSet<V> getAuditTrails(final U o, final Optional<Timeframe> timeframe, final Optional<Long> actorPk) {
		final var table = getAuditTable();

		final List<Condition> conditions = new ArrayList<>();
		//add trailed object condition
		conditions.add(table.AUDIT_OBJECT_FK().eq(o.getPk()));

		//add user/robot condition
		actorPk.ifPresent(actorFk -> {
			conditions.add(table.AUDIT_USER_FK().eq(actorFk).or(table.AUDIT_ROBOT_FK().eq(actorFk)));
		});

		//add timeframe condition
		timeframe.ifPresent(tf -> {
			tf.startDate().ifPresent(startDate -> {
				conditions.add(table.AUDIT_DATETIME().greaterThan(startDate));
			});
			tf.stopDate().ifPresent(stopDate -> {
				conditions.add(table.AUDIT_DATETIME().lessThan(stopDate));
			});
		});

		final ResultQuery<S> query = create.selectFrom(table).where(conditions).orderBy(table.AUDIT_DATETIME(), table.PK());
		final var auditTrails = strategy.find(table, query, getEntityAuditClass());
		return new TreeSet<V>(auditTrails);
	}

	public NavigableSet<V> getAuditTrailsForProperties(
		final U o,
		final Optional<Timeframe> timeframe,
		final List<Function<V, Object>> properties
	) {
		final var auditTrails = getAuditTrails(o, timeframe, Optional.empty());

		List<Object> propertyValues = new ArrayList<>();
		final var filterAuditTrails = new TreeSet<V>();
		for(final V auditTrail : auditTrails) {
			final List<Object> newPropertyValues = properties.stream().map(f -> f.apply(auditTrail)).toList();
			if(!newPropertyValues.equals(propertyValues)) {
				filterAuditTrails.add(auditTrail);
				propertyValues = newPropertyValues;
			}
		}

		return filterAuditTrails;
	}

	public NavigableSet<V> getAuditTrailsForProperty(final U o, final Optional<Timeframe> timeframe, final Function<V, Object> property) {
		return getAuditTrailsForProperties(o, timeframe, Collections.singletonList(property));
	}

	@Override
	protected void audit(
		final U o,
		final DatabaseActionContext context,
		final String rationale
	) {
		//this is a hack, we create the audit trail record directly from the object
		//this works because almost all fields are the same
		final var auditTrail = create.newRecord(getAuditTable(), o);
		//we just need to adjust some columns in the code below
		//remove object pk
		auditTrail.setValue(DSL.field("pk"), null);
		auditTrail.setAuditObjectFk(o.getPk());
		auditTrail.setAuditActionFk(context.auditAction().getPk());
		auditTrail.setAuditDatetime(context.auditAction().getDate());
		auditTrail.setAuditContext(rationale);
		auditTrail.setAuditActor(context.getActorName());
		final var actor = context.actor();
		actor.ifPresent(a -> {
			if(a instanceof User) {
				auditTrail.setAuditUserFk(a.getPk());
			}
			else {
				auditTrail.setAuditRobotFk(a.getPk());
			}
		});

		//store the record
		//no need to use the strategy here, because we don't care about the cache
		auditTrail.store();

		// Log
		logger.trace("{} has been audited", o.getClass().getSimpleName());
	}
}
