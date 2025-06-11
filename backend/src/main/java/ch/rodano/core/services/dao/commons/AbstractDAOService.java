package ch.rodano.core.services.dao.commons;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.common.AuditableObject;
import ch.rodano.core.model.common.DeletableObject;
import ch.rodano.core.model.common.HardDeletableObject;
import ch.rodano.core.model.common.IdentifiableObject;
import ch.rodano.core.model.common.PersistentObject;
import ch.rodano.core.model.common.TimestampableObject;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.exception.NotUniqueResultException;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

public abstract class AbstractDAOService<U extends IdentifiableObject, V extends Record> {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected final DSLContext create;
	protected final DAOStrategy strategy;
	protected final StudyService studyService;

	public AbstractDAOService(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService
	) {
		this.create = create;
		this.strategy = strategy;
		this.studyService = studyService;
	}

	protected abstract Class<U> getDAOClass();

	protected abstract Table<V> getTable();

	@SuppressWarnings("unused")
	protected void audit(
		final U o,
		final DatabaseActionContext context,
		final String rationale
	) {
		throw new UnsupportedOperationException("Not supported by this DAO");
	}

	/**
	 * Persist an object in database
	 *
	 * @param o         The object to persist
	 * @param context   The context in which this action takes place
	 * @param rationale The rationale for the operation
	 * @return True if the object has actually been saved, false if the save was not necessary because the object has not been modified
	 */
	protected boolean save(final U o, final DatabaseActionContext context, final String rationale) {
		final var table = getTable();
		final var isInsert = o.getPk() == null;

		// Pre-update must be executed before the record comparison.
		// Some object parameters are updated in the preUpdate step and need to be updated before record comparison.
		if(o instanceof final PersistentObject po) {
			po.onPreUpdate();
		}

		//do not update the object if it has not changed
		if(!isInsert) {
			final var newRecord = create.newRecord(table, o);
			final var existingRecord = strategy.retrieveCachedRecord(table, o.getPk());
			if(existingRecord.equals(newRecord)) {
				logger.debug("Redundant save of {} with rationale: {}", o.getClass().getSimpleName(), rationale);
				return false;
			}
		}

		//set creation date and last update time
		if(o instanceof final TimestampableObject to) {
			if(isInsert && to.getCreationTime() == null) {
				to.setCreationTime(context.auditAction().getDate());
			}
			final var lastUpdateTime = to.getLastUpdateTime();
			if(context.auditAction().getDate().isBefore(to.getCreationTime()) || lastUpdateTime != null && context.auditAction().getDate().isBefore(lastUpdateTime)) {
				throw new UnsupportedOperationException("An object cannot be updated with a context operation date that is before the creation time or the last update time of the object");
			}
			to.setLastUpdateTime(context.auditAction().getDate());
		}

		//save or insert
		if(isInsert) {
			strategy.insert(table, o);
		}
		else {
			strategy.save(table, o);
		}

		//warning, in a very few cases, the context could be null (for example, in interceptors)
		logger.debug("Actor {} is {} {} with rationale: {}", context.getActorName(), isInsert ? "inserting" : "updating", o.getClass().getSimpleName(), rationale);

		if(o instanceof final PersistentObject po) {
			po.onPostUpdate(studyService.getStudy());
		}

		if(o instanceof AuditableObject) {
			audit(o, context, rationale);
		}

		return true;
	}

	/**
	 * Hard delete an object
	 *
	 * @param o       The object to delete
	 */
	protected void delete(final U o) {
		//TODO find a solution to enforce this check at compile time
		if(o instanceof final HardDeletableObject hdo) {

			if(hdo instanceof final PersistentObject po) {
				po.onPreUpdate();
			}

			strategy.delete(getTable(), hdo);

			// Log
			logger.debug("Hard delete a {}", o.getClass().getSimpleName());
		}
		else {
			throw new UnsupportedOperationException("Object must be an HardDeletableObject");
		}
	}

	/**
	 * Mark an object as deleted within the database
	 *
	 * @param o       The object to flag as deleted
	 * @param context The context in which this action takes place
	 * @param rationale The rationale for the operation
	 */
	protected void delete(final U o, final DatabaseActionContext context, final String rationale) {
		//TODO find a solution to enforce this check at compile time
		if(o instanceof final DeletableObject dao) {
			if(dao instanceof final PersistentObject po) {
				po.onPreUpdate();
			}

			// Set the last update time
			if(dao instanceof final TimestampableObject to) {
				to.setLastUpdateTime(context.auditAction().getDate());
			}

			// Set the deleted flag
			dao.delete();

			strategy.save(getTable(), dao);

			if(dao instanceof final PersistentObject po) {
				po.onPostUpdate(studyService.getStudy());
			}

			if(dao instanceof AuditableObject) {
				audit(o, context, rationale);
			}

			// Log
			logger.debug("Actor {} soft deleted a {} with rationale: {}", context.getActorName(), dao.getClass().getSimpleName(), rationale);
		}
		else {
			throw new UnsupportedOperationException("Object must be an DeletableObject");
		}
	}

	/**
	 * Restore an object in the database
	 *
	 * @param o       The object to restore
	 * @param context The context in which this action takes place
	 * @param rationale The rationale for the operation
	 */
	protected void restore(final U o, final DatabaseActionContext context, final String rationale) {
		//TODO find a solution to enforce this check at compile time
		if(o instanceof final DeletableObject dao) {
			if(dao instanceof final PersistentObject po) {
				po.onPreUpdate();
			}

			// Set the last update time
			if(dao instanceof final TimestampableObject to) {
				to.setLastUpdateTime(context.auditAction().getDate());
			}

			// Remove the deleted flag
			dao.restore();

			strategy.save(getTable(), dao);

			if(dao instanceof final PersistentObject po) {
				po.onPostUpdate(studyService.getStudy());
			}

			if(dao instanceof AuditableObject) {
				audit(o, context, rationale);
			}

			// Log
			logger.debug("Actor {} restored a {} with rationale: {}", context.getActorName(), dao.getClass().getSimpleName(), rationale);
		}
		else {
			throw new UnsupportedOperationException("Object must be an DeletableObject");
		}
	}

	/**
	 * Find all result for the given query
	 *
	 * @param query The query to use
	 * @return A list of found object
	 */
	protected List<U> find(final ResultQuery<V> query) {
		logger.debug("Search database for {} using a find query", getDAOClass().getSimpleName());
		return strategy.find(getTable(), query, getDAOClass());
	}

	/**
	 * Find a unique result for the given query
	 *
	 * @param query The query to use
	 * @return A unique object or null if no object have been found
	 */
	protected U findUnique(final ResultQuery<V> query) {
		final var results = find(query);
		if(results.isEmpty()) {
			return null;
		}
		if(results.size() > 1) {
			throw new NotUniqueResultException("More than one result has been found with the query: " + query);
		}
		return results.get(0);
	}
}
