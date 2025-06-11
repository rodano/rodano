package ch.rodano.core.services.dao.strategy;

import java.util.Arrays;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.common.HardDeletableObject;
import ch.rodano.core.model.common.IdentifiableObject;
import ch.rodano.core.model.common.PersistentObject;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.cache.transaction.TransactionCacheDAOService;

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Service
public class DAOStrategyImpl implements DAOStrategy {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final DSLContext create;
	private final StudyService studyService;
	private final TransactionCacheDAOService transactionCacheDAOService;

	public DAOStrategyImpl(
		final DSLContext create,
		final StudyService studyService,
		final TransactionCacheDAOService transactionCacheDAOService
	) {
		this.create = create;
		this.studyService = studyService;
		this.transactionCacheDAOService = transactionCacheDAOService;
	}

	@Override
	public <R extends org.jooq.Record, T extends IdentifiableObject> List<T> executeQuery(final ResultQuery<R> query, final Class<T> clazz) {
		logger.trace("Executing untyped query: {}", query.toString());
		final var objects = query.fetch().into(clazz);
		if(PersistentObject.class.isAssignableFrom(clazz)) {
			for(final var o : objects) {
				((PersistentObject) o).onPostLoad(studyService.getStudy());
			}
		}
		return objects;
	}

	@Override
	public <R extends org.jooq.Record, T extends IdentifiableObject> List<T> find(final Table<R> table, final ResultQuery<R> query, final Class<T> clazz) {
		logger.trace("Executing untyped query: {}", query.toString());
		final var cache = transactionCacheDAOService.getCache();
		final var result = query.fetch();
		cache.storeRecords(table, result);
		final var objects = result.into(clazz);
		if(PersistentObject.class.isAssignableFrom(clazz)) {
			for(final var o : objects) {
				((PersistentObject) o).onPostLoad(studyService.getStudy());
			}
		}
		return cache.getOrAddObjects(objects);
	}

	@Override
	public <R extends Record, T extends IdentifiableObject> T insert(final Table<R> table, final T o) {
		final var record = create.newRecord(table, o);
		final var fields = Arrays.stream(record.fields()).filter(f -> !f.getName().equals("pk")).toList();
		final var values = fields.stream().map(f -> f.getValue(record)).toList();

		final var query = create.insertInto(table, fields)
			.values(values)
			.returningResult(DSL.field("pk"));

		logger.trace("Executing insert query: {}", query);

		final var result = query.fetchOne();

		final Long generatedPk = (Long) result.value1();
		o.setPk(generatedPk);

		final var cache = transactionCacheDAOService.getCache();
		cache.storeRecord(table, create.newRecord(table, o));
		return cache.getOrAddObject(o);
	}

	@Override
	public <R extends UpdatableRecord<R>> void insert(final Table<R> table, final R record) {
		final var fields = Arrays.asList(record.fields()).stream().filter(f -> !f.getName().equals("pk")).toList();
		final var values = fields.stream().map(f -> f.getValue(record)).toList();
		final var query = create.insertInto(table, fields).values(values);
		logger.trace("Executing insert query: {}", query);

		query.execute();
	}

	@Override
	public <R extends Record, T extends IdentifiableObject> T save(final Table<R> table, final T o) {
		final var record = create.newRecord(table, o);
		final var query = create.update(table).set(record).where("pk = " + o.getPk());
		//fields.forEach(f -> query.set(f, f.getValue(record)));

		logger.trace("Executing update query: {}", query);

		query.execute();

		//only a single instance of a single object should be present at any time during one HTTP request
		//in theory, there is no need to update the cache here
		return o;

		//final var transactionCache = transactionCacheDAOService.getCache();
		//return transactionCache.updateObject(o);
	}

	@Override
	public void executeHardDeleteQuery(final Query query) {
		logger.trace("Executing delete query: {}", query.toString());
		query.execute();
	}

	@Override
	public <R extends Record, T extends HardDeletableObject> void delete(final Table<R> table, final T o) {
		final var query = create.deleteFrom(table).where("pk = " + o.getPk());
		query.execute();

		final var cache = transactionCacheDAOService.getCache();
		cache.removeRecord(table, o.getPk());
		cache.removeObject(o);
	}

	@Override
	public <R extends Record> Record retrieveCachedRecord(final Table<R> table, final Long recordPk) {
		return transactionCacheDAOService.getCache().retrieveRecord(table, recordPk);
	}
}
