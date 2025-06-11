package ch.rodano.core.services.dao.commons.cache.transaction;

public interface TransactionCacheDAOService {
	/**
	 * Get the transaction cache of the current thread
	 *
	 * @return The transaction cache actually used in the current thread (http request) or a new one (scheduler for instance)
	 */
	TransactionCacheDAO getCache();

	/**
	 * Empty the transaction cache used by the current thread
	 */
	void emptyCache();
}
