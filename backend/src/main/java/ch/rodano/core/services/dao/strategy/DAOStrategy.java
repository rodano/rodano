package ch.rodano.core.services.dao.strategy;

import java.util.List;

import org.jooq.Query;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.Table;
import org.jooq.UpdatableRecord;

import ch.rodano.core.model.common.HardDeletableObject;
import ch.rodano.core.model.common.IdentifiableObject;

public interface DAOStrategy {

	/**
	 * Execute the given result query, and returns a list of the resulting objects, without using the cache
	 * Records and objects fetched from this query are NOT added to the cache and should therefore not be modified
	 *
	 * It's dangerous to fetch objects without caching them, except if these objects are not modified
	 * The code of the application relies on this cache, for example, in this code:
	 * <pre>
	 * var status = workflowStatusService.get(field, "ENROLLMENT");
	 * assertEquals("REGISTERED", status.getStateId());
	 * fieldService.saveValue(scope, Optional.of(event), dataset, field, "Y", context, TEST_RATIONALE);
	 * assertEquals("WITHDRAWN", status.getStateId());
	 * </pre>
	 * the status object must be unique in the whole application so the one instance updated by the field service is the same as the one that exists in the snippet
	 *
	 * @param query The result query to execute
	 * @param clazz The class to transform the resulting records into
	 * @return A list of result or null
	 */
	<R extends org.jooq.Record, T extends IdentifiableObject> List<T> executeQuery(ResultQuery<R> query, Class<T> clazz);

	/**
	 * Execute the given result query, and returns a list of the resulting objects, with use of the cache
	 * Records and objects fetched from this query are added to the cache
	 *
	 * @param table The table
	 * @param query The result query to execute
	 * @param clazz The class to transform the resulting records into
	 * @return A list of result or null
	 */
	<R extends org.jooq.Record, T extends IdentifiableObject> List<T> find(Table<R> table, ResultQuery<R> query, Class<T> clazz);

	/**
	 * Execute the given query
	 *
	 * @param query The query to execute
	 */
	void executeHardDeleteQuery(Query query);

	/**
	 * Insert the object o into the database
	 *
	 * @param table   The table where the object must be inserted into
	 * @param o       The object to insert
	 * @return The inserted object
	 */
	<R extends Record, T extends IdentifiableObject> T insert(Table<R> table, T o);

	/**
	 * Insert a jOOQ record into the database
	 *
	 * @param record       The record to insert
	 */
	<R extends UpdatableRecord<R>> void insert(Table<R> table, R record);

	/**
	 * Save the object o to the database
	 *
	 * @param o       The object to save
	 * @return The saved object
	 */
	<R extends Record, T extends IdentifiableObject> T save(Table<R> table, T o);

	/**
	 * Delete the object o from the database
	 *
	 * @param o   The object to delete
	 * @param <T> A hard deletable persistent object
	 */
	<R extends Record, T extends HardDeletableObject> void delete(Table<R> table, T o);

	/**
	 * Retrieve a cached record
	 * This method must only be used to check if a record is in the cache (to display a log when an object is fetched twice)
	 * @param <R>           Class extending the JOOQ Record class
	 * @param table         JOOQ table to query
	 * @param recordPk      The record's PK
	 * @return              Cached record
	 */
	<R extends Record> Record retrieveCachedRecord(Table<R> table, Long recordPk);

}
