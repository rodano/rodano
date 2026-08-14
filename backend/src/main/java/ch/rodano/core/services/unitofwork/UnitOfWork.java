package ch.rodano.core.services.unitofwork;

/**
 * Scope of a single logical operation: an HTTP request, a scheduled task execution or an asynchronous response stream.
 * It guarantees that a database object is represented by one and only one Java instance for its whole duration, and
 * that this instance is released when the operation ends.
 * This is deliberately not the transaction: an HTTP request reads from the database before its transaction begins
 * (token introspection, in the security filter chain) and after it has committed (response streaming, on another
 * thread), so a transaction is a narrower scope than the operation it belongs to.
 *
 * Why is a cache of database objects needed at all? Because the same object could be read multiple times in a single operation:
 * <pre>
 * var status = workflowStatusService.get(field, "ENROLLMENT");
 * assertEquals("REGISTERED", status.getStateId());
 * fieldService.saveValue(scope, Optional.of(event), dataset, field, "Y", context, TEST_RATIONALE);
 * assertEquals("WITHDRAWN", status.getStateId());
 * </pre>
 */
public class UnitOfWork {
	private final UnitOfWorkCache cache;
	private final boolean readOnly;
	private final boolean singleOperation;

	private int transactionCount;

	UnitOfWork(final boolean readOnly, final boolean singleOperation) {
		this.cache = new UnitOfWorkCache();
		this.readOnly = readOnly;
		this.singleOperation = singleOperation;
	}

	public UnitOfWorkCache getCache() {
		return cache;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Whether this unit of work covers a single logical operation, and is therefore expected to hold a single
	 * transaction.
	 */
	boolean isSingleOperation() {
		return singleOperation;
	}

	/**
	 * Record that a new physical transaction has begun in this unit of work.
	 *
	 * @return The number of physical transactions begun so far, including this one
	 */
	int registerTransaction() {
		return ++transactionCount;
	}

	void clear() {
		cache.clear();
	}
}
