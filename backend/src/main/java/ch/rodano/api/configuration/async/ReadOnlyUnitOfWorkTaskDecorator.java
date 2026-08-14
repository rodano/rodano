package ch.rodano.api.configuration.async;

import org.springframework.core.task.TaskDecorator;

import ch.rodano.core.services.unitofwork.UnitOfWorkService;

/**
 * Opens the read-only unit of work of an asynchronous response stream.
 * A streamed response is written on a pooled thread once the controller has returned and its transaction has committed,
 * so it cannot share the unit of work of the request that started it. It gets a read-only one instead: streaming is
 * only ever meant to serve reports and file downloads, and the check makes that a rule rather than a convention.
 * <p>
 * This decorator is not a Spring bean on purpose: Spring Boot applies any {@link TaskDecorator} bean to the
 * auto-configured {@code applicationTaskExecutor}, which would wrongly force background jobs submitted there into a
 * read-only unit of work. It is instead installed explicitly on the MVC streaming executor.
 */
public class ReadOnlyUnitOfWorkTaskDecorator implements TaskDecorator {
	private final UnitOfWorkService unitOfWorkService;

	public ReadOnlyUnitOfWorkTaskDecorator(final UnitOfWorkService unitOfWorkService) {
		this.unitOfWorkService = unitOfWorkService;
	}

	@Override
	public Runnable decorate(final Runnable runnable) {
		return () -> unitOfWorkService.run(true, () -> {
			runnable.run();
			return null;
		});
	}
}
