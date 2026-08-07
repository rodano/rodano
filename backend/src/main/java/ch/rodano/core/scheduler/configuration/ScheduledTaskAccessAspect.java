package ch.rodano.core.scheduler.configuration;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.StopWatch;

import ch.rodano.core.services.unitofwork.UnitOfWorkService;

@Aspect
@Configuration
//run outside of the task's own @Transactional (which defaults to the lowest precedence), so the timing below covers the whole transaction
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ScheduledTaskAccessAspect {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final UnitOfWorkService unitOfWorkService;

	public ScheduledTaskAccessAspect(final UnitOfWorkService unitOfWorkService) {
		this.unitOfWorkService = unitOfWorkService;
	}

	@Pointcut("this(ch.rodano.core.scheduler.task.ScheduledTask)")
	public void taskPointcut() {
		// Nothing
	}

	@Pointcut("execution(* ch.rodano.core.scheduler.task.ScheduledTask.run(..))")
	public void runTaskPointcut() {
		// Nothing
	}

	/**
	 * Open the unit of work of the task, then log its execution and its execution time. Transaction demarcation is
	 * handled by @Transactional on each task's run() method.
	 *
	 * @param joinPoint The join point of the task
	 * @throws Throwable Thrown if an error occurred while executing the task, after it has been logged
	 */
	@Around("taskPointcut() && runTaskPointcut()")
	public void logTask(final ProceedingJoinPoint joinPoint) throws Throwable {
		final var taskName = joinPoint.getSignature().getDeclaringType().getSimpleName();

		logger.debug("Starting execution of task " + taskName);
		final var watch = new StopWatch();
		watch.start();

		//a task triggered manually through the administration API already runs in the unit of work of that HTTP request
		try {
			unitOfWorkService.run(false, () -> {
				joinPoint.proceed();
				return null;
			});
		}
		catch(final Throwable t) {
			logger.error("An exception occurred while executing task " + taskName, t);
			throw t;
		}

		watch.stop();
		logger.debug("Task " + taskName + " executed in " + watch.getTotalTimeMillis() + " ms");
	}
}
