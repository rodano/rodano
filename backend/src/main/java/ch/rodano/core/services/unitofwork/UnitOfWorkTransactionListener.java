package ch.rodano.core.services.unitofwork;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.ConfigurableTransactionManager;
import org.springframework.transaction.TransactionExecution;
import org.springframework.transaction.TransactionExecutionListener;

/**
 * Enforces that a unit of work contains a single physical transaction.
 * <p>
 * A second transaction would generate a second audit action for the same HTTP request, which breaks the ability to
 * trace a modification back to the request that caused it.
 */
@Component
public class UnitOfWorkTransactionListener implements TransactionExecutionListener, InitializingBean {
	private final ConfigurableTransactionManager transactionManager;
	private final UnitOfWorkService unitOfWorkService;

	public UnitOfWorkTransactionListener(
		final ConfigurableTransactionManager transactionManager,
		final UnitOfWorkService unitOfWorkService
	) {
		this.transactionManager = transactionManager;
		this.unitOfWorkService = unitOfWorkService;
	}

	/**
	 * Register on the transaction manager from here rather than where it is declared: injecting this listener into the
	 * transaction manager bean makes the whole DAO layer be instantiated before the context is ready for it.
	 */
	@Override
	public void afterPropertiesSet() {
		transactionManager.addListener(this);
	}

	@Override
	public void beforeBegin(final TransactionExecution transaction) {
		//savepoints are notified here as well, but they run inside the transaction that opened them
		if(!transaction.isNewTransaction() || !unitOfWorkService.isActive()) {
			return;
		}

		final var unitOfWork = unitOfWorkService.current();
		if(!unitOfWork.isSingleOperation()) {
			return;
		}

		final var transactionCount = unitOfWork.registerTransaction();
		if(transactionCount > 1) {
			//thrown before the transaction actually begins, so nothing is left dangling
			throw new IllegalStateException(
				String.format(
					"Transaction %s is the transaction number %s of its unit of work, which must contain a single one",
					transaction.getTransactionName(),
					transactionCount
				)
			);
		}
	}
}
