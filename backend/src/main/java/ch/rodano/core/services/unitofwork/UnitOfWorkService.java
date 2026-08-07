package ch.rodano.core.services.unitofwork;

import org.springframework.stereotype.Service;

/**
 * Binds the unit of work of the current operation, and gives access to it from anywhere below.
 * The binding is lexically scoped, so a unit of work cannot outlive its boundary and leak onto a pooled thread.
 */
@Service
public class UnitOfWorkService {
	private static final ScopedValue<UnitOfWork> CURRENT = ScopedValue.newInstance();

	/**
	 * Run an operation in its own unit of work, or in the one an outer boundary has already bound.
	 *
	 * @param readOnly Whether writing to the database must be rejected for the whole unit of work
	 */
	public <R, X extends Throwable> R run(final boolean readOnly, final ScopedValue.CallableOp<R, X> operation) throws X {
		//an inner boundary joins the outer one: a task triggered through the administration API must see the objects of that request
		if(CURRENT.isBound()) {
			return operation.call();
		}
		return ScopedValue.where(CURRENT, new UnitOfWork(readOnly, true)).call(operation);
	}

	/**
	 * Bind a single unit of work around a whole test run.
	 * This is the only entry point that opens a non single-operation unit of work, and the only caller is the JUnit
	 * launcher interceptor: a test needs the objects it reads in a @BeforeEach to still be there in the test method
	 * itself, and JUnit runs those as separate callbacks, so the binding cannot be per test.
	 */
	public <R, X extends Throwable> R runForTestRun(final ScopedValue.CallableOp<R, X> operations) throws X {
		return ScopedValue.where(CURRENT, new UnitOfWork(false, false)).call(operations);
	}

	public boolean isActive() {
		return CURRENT.isBound();
	}

	public UnitOfWork current() {
		if(!CURRENT.isBound()) {
			throw new IllegalStateException("The database can only be accessed from within a unit of work");
		}
		return CURRENT.get();
	}

	/**
	 * Release the objects held by the current unit of work without ending it.
	 */
	public void clear() {
		if(CURRENT.isBound()) {
			CURRENT.get().clear();
		}
	}

	/**
	 * Reject a database write performed by a read-only unit of work.
	 */
	public void checkWriteAllowed() {
		if(current().isReadOnly()) {
			throw new IllegalStateException("A read-only unit of work is not allowed to write to the database");
		}
	}
}
