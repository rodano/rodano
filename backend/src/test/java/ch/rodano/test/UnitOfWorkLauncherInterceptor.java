package ch.rodano.test;

import org.junit.platform.launcher.LauncherInterceptor;

import ch.rodano.core.services.unitofwork.UnitOfWorkService;

/**
 * Binds a unit of work around the whole test run.
 * A test needs the objects it reads in a @BeforeEach to still be there in the test method itself, and JUnit runs those
 * as separate callbacks, so the binding cannot be per test. The database tests clear the objects between tests instead.
 */
public class UnitOfWorkLauncherInterceptor implements LauncherInterceptor {

	//the service holds no state of its own, the binding lives in a scoped value, and no Spring context exists here yet
	private final UnitOfWorkService unitOfWorkService = new UnitOfWorkService();

	@Override
	public <T> T intercept(final Invocation<T> invocation) {
		return unitOfWorkService.runForTestRun(invocation::proceed);
	}

	@Override
	public void close() {
		//nothing to do
	}
}
