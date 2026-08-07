package ch.rodano.api.configuration.async;

import org.springframework.core.task.TaskDecorator;

import ch.rodano.core.services.unitofwork.UnitOfWorkService;

/**
 * Opens a writable unit of work around any job submitted to the general-purpose {@code applicationTaskExecutor}.
 * Background jobs such as random-data generation and CRF archive creation run on a pooled thread once the controller
 * has returned, so they cannot share the unit of work of the request that started them. They get their own writable one
 * so that the single-in-memory-copy guarantee still holds and they are allowed to write to the database.
 */
public class WritableUnitOfWorkTaskDecorator implements TaskDecorator {
	private final UnitOfWorkService unitOfWorkService;

	public WritableUnitOfWorkTaskDecorator(final UnitOfWorkService unitOfWorkService) {
		this.unitOfWorkService = unitOfWorkService;
	}

	@Override
	public Runnable decorate(final Runnable runnable) {
		return () -> unitOfWorkService.run(false, () -> {
			runnable.run();
			return null;
		});
	}
}
