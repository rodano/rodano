package ch.rodano.core.services.dao.commons.cache.transaction;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

@Service
public class TransactionCacheDAOServiceImpl implements TransactionCacheDAOService, DisposableBean {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ConcurrentMap<Thread, TransactionCacheDAO> cacheByThread;

	public TransactionCacheDAOServiceImpl() {
		this.cacheByThread = new ConcurrentHashMap<>();
	}

	/**
	 * Invoked by a BeanFactory on destruction of a singleton.
	 */
	@Override
	public void destroy() {
		cacheByThread.clear();
	}

	@Override
	public TransactionCacheDAO getCache() {
		final var currentThread = Thread.currentThread();

		var transactionCache = cacheByThread.get(currentThread);
		if(transactionCache != null) {
			return transactionCache;
		}

		transactionCache = new TransactionCacheDAO();
		cacheByThread.put(currentThread, transactionCache);

		logger.trace("Assign transaction cache {} to thread {}", transactionCache.hashCode(), currentThread.getId());

		return transactionCache;
	}

	/**
	 * Free the transaction cache used by the current thread
	 */
	@Override
	public void emptyCache() {
		final var currentThread = Thread.currentThread();
		final var transactionCache = cacheByThread.remove(currentThread);

		if(transactionCache == null) {
			return;
		}

		logger.trace("Free transaction cache {} from thread {}", transactionCache.hashCode(), currentThread.getId());

		transactionCache.clear();
	}
}
