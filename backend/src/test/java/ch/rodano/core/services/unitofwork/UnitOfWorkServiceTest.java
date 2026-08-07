package ch.rodano.core.services.unitofwork;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionExecution;

import ch.rodano.core.model.common.IdentifiableObject;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnitOfWorkServiceTest {
	private final UnitOfWorkService unitOfWorkService = new UnitOfWorkService();

	@Test
	@DisplayName("Database writes require an active unit of work")
	public void writeRequiresActiveUnitOfWork() throws Exception {
		runWithoutTestUnitOfWork(() -> {
			assertFalse(unitOfWorkService.isActive());
			assertThrows(IllegalStateException.class, unitOfWorkService::checkWriteAllowed);
			return null;
		});
	}

	@Test
	@DisplayName("A read-only unit of work rejects writes")
	public void readOnlyUnitOfWorkRejectsWrites() throws Exception {
		runWithoutTestUnitOfWork(() -> unitOfWorkService.run(true, () -> {
			assertThrows(IllegalStateException.class, unitOfWorkService::checkWriteAllowed);
			return null;
		}));
	}

	@Test
	@DisplayName("A writable unit of work allows writes")
	public void writableUnitOfWorkAllowsWrites() throws Exception {
		runWithoutTestUnitOfWork(() -> unitOfWorkService.run(false, () -> {
			unitOfWorkService.checkWriteAllowed();
			return null;
		}));
	}

	@Test
	@DisplayName("Nested boundaries join the active unit of work")
	public void nestedBoundaryJoinsActiveUnitOfWork() throws Exception {
		runWithoutTestUnitOfWork(() -> unitOfWorkService.run(true, () -> {
			final var outerUnitOfWork = unitOfWorkService.current();

			return unitOfWorkService.run(false, () -> {
				assertSame(outerUnitOfWork, unitOfWorkService.current());
				assertThrows(IllegalStateException.class, unitOfWorkService::checkWriteAllowed);
				return null;
			});
		}));
	}

	@Test
	@DisplayName("The binding ends after a successful operation")
	public void bindingEndsAfterSuccess() throws Exception {
		runWithoutTestUnitOfWork(() -> {
			unitOfWorkService.run(false, () -> {
				assertTrue(unitOfWorkService.isActive());
				return null;
			});

			assertFalse(unitOfWorkService.isActive());
			assertThrows(IllegalStateException.class, unitOfWorkService::current);
			return null;
		});
	}

	@Test
	@DisplayName("The binding ends after a failed operation")
	public void bindingEndsAfterFailure() throws Exception {
		runWithoutTestUnitOfWork(() -> {
			assertThrows(TestException.class, () -> unitOfWorkService.run(false, () -> {
				throw new TestException();
			}));

			assertFalse(unitOfWorkService.isActive());
			assertThrows(IllegalStateException.class, unitOfWorkService::current);
			return null;
		});
	}

	@Test
	@DisplayName("Clearing a unit of work releases its cached objects")
	public void clearReleasesCachedObjects() throws Exception {
		runWithoutTestUnitOfWork(() -> unitOfWorkService.run(false, () -> {
			final var cache = unitOfWorkService.current().getCache();
			cache.getOrAddObject(new TestObject(1L));
			assertFalse(cache.getJavaObjectCacheSize() == 0);

			unitOfWorkService.clear();

			assertTrue(cache.getJavaObjectCacheSize() == 0);
			assertTrue(cache.getRecordCacheSize() == 0);
			return null;
		}));
	}

	@Test
	@DisplayName("A single operation rejects a second physical transaction")
	public void secondPhysicalTransactionIsRejected() throws Exception {
		final var listener = new UnitOfWorkTransactionListener(null, unitOfWorkService);
		final var transaction = newTransaction("test transaction");

		runWithoutTestUnitOfWork(() -> unitOfWorkService.run(false, () -> {
			listener.beforeBegin(transaction);
			assertThrows(IllegalStateException.class, () -> listener.beforeBegin(transaction));
			return null;
		}));
	}

	@Test
	@DisplayName("Participating transactions and savepoints do not count as new physical transactions")
	public void participatingTransactionsAreAllowed() throws Exception {
		final var listener = new UnitOfWorkTransactionListener(null, unitOfWorkService);
		final var participatingTransaction = newTransaction(false, "participating transaction");
		final var physicalTransaction = newTransaction("physical transaction");

		runWithoutTestUnitOfWork(() -> unitOfWorkService.run(false, () -> {
			listener.beforeBegin(participatingTransaction);
			listener.beforeBegin(participatingTransaction);
			listener.beforeBegin(physicalTransaction);
			return null;
		}));
	}

	private static TransactionExecution newTransaction(final String name) {
		return newTransaction(true, name);
	}

	private static TransactionExecution newTransaction(final boolean newTransaction, final String name) {
		return new TransactionExecution() {
			@Override
			public String getTransactionName() {
				return name;
			}

			@Override
			public boolean isNewTransaction() {
				return newTransaction;
			}
		};
	}

	private static <T> T runWithoutTestUnitOfWork(final Callable<T> operation) throws Exception {
		try(final var executor = Executors.newSingleThreadExecutor()) {
			return executor.submit(operation).get();
		}
	}

	private static class TestObject implements IdentifiableObject {
		private Long pk;

		TestObject(final Long pk) {
			this.pk = pk;
		}

		@Override
		public Long getPk() {
			return pk;
		}

		@Override
		public void setPk(final Long pk) {
			this.pk = pk;
		}
	}

	private static class TestException extends RuntimeException {
		private static final long serialVersionUID = 8264047222503027339L;
	}
}
