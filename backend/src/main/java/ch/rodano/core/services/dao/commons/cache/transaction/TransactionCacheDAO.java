package ch.rodano.core.services.dao.commons.cache.transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Record;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.core.model.common.IdentifiableObject;

public class TransactionCacheDAO {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * The java object cache stores java objects for when they need to be recovered several times over the course of a single request.
	 */
	private final Map<Class<?>, Map<Long, IdentifiableObject>> javaObjectCaches;
	/**
	 * The record cache stores the records as they are found in the database. Used to compare the java object to the database objects
	 * to see if any modifications have been performed to the object.
	 */
	private final Map<String, Map<Long, Record>> recordCaches;

	public TransactionCacheDAO() {
		// No need to create an thread safe map cause one session cache is attached to only one thread
		javaObjectCaches = new HashMap<>();
		recordCaches = new HashMap<>();
	}

	public Integer getJavaObjectCacheSize() {
		return javaObjectCaches.size();
	}

	public Integer getRecordCacheSize() {
		return recordCaches.size();
	}

	public void clear() {
		javaObjectCaches.clear();
		recordCaches.clear();
	}

	public <R extends Record> void storeRecord(final Table<R> table, final R record) {
		final var recordCache = getRecordCache(table.getName());
		recordCache.put((Long) record.get("pk"), record);
	}

	public <R extends Record> void storeRecords(final Table<R> table, final List<R> records) {
		final var recordCache = getRecordCache(table.getName());
		records.forEach(r -> recordCache.put((Long) r.get("pk"), r));
	}

	public <R extends Record> void removeRecord(final Table<R> table, final Long recordPk) {
		final var recordCache = getRecordCache(table.getName());
		recordCache.remove(recordPk);
	}

	public <R extends Record> Record retrieveRecord(final Table<R> table, final Long recordPk) {
		final var recordCache = getRecordCache(table.getName());
		final var record = recordCache.get(recordPk);
		if(record != null) {
			return record;
		}
		throw new IllegalArgumentException(String.format("No record in table %s with pk %s was found in the transaction cache", table.getName(), recordPk));
	}

	/**
	 * Add a newly created object to the cache
	 *
	 * @param object The new object to add
	 * @return The new object to add
	 */
	@SuppressWarnings("unchecked")
	public <T extends IdentifiableObject> T getOrAddObject(final T object) {
		final var javaClassCache = getJavaClassCache(object.getClass());

		final var cachedObject = javaClassCache.get(object.getPk());
		if(cachedObject != null) {
			logger.trace("Return {} with pk {} from the cache. The query that fetched this object could have been avoided", object.getClass().getSimpleName(), object.getPk());
			return (T) cachedObject;
		}

		javaClassCache.put(object.getPk(), object);
		return object;
	}

	/**
	 * Add a new list of objects in the cache
	 *
	 * @param objects Objects to add
	 * @return The list of objects stored and retrieved from the cache
	 */
	public <T extends IdentifiableObject> List<T> getOrAddObjects(final List<T> objects) {
		return objects.stream().map(this::getOrAddObject).toList();
	}

	/**
	 * Remove an object from the cache
	 *
	 * @param object Objects to remove
	 */
	public <T extends IdentifiableObject> void removeObject(final T object) {
		final var javaClassCache = getJavaClassCache(object.getClass());
		javaClassCache.remove(object.getPk());
	}

	private Map<Long, IdentifiableObject> getJavaClassCache(final Class<?> clazz) {
		return javaObjectCaches.computeIfAbsent(clazz, _ -> new HashMap<>());
	}

	private Map<Long, Record> getRecordCache(final String table) {
		return recordCaches.computeIfAbsent(table, _ -> new HashMap<>());
	}
}
