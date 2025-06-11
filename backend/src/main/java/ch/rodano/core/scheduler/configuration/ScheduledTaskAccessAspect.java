package ch.rodano.core.scheduler.configuration;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;

import ch.rodano.core.services.dao.commons.cache.transaction.TransactionCacheDAOService;

@Aspect
@Configuration
public class ScheduledTaskAccessAspect {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final TransactionCacheDAOService transactionCacheDAOService;

	public ScheduledTaskAccessAspect(
		final TransactionCacheDAOService transactionCacheDAOService
	) {
		this.transactionCacheDAOService = transactionCacheDAOService;
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
	 * Log a task execution and its execution time
	 *
	 * @param joinPoint The join point of the task
	 * @throws Throwable Thrown if an error occurred while executing the task
	 */
	@Around("taskPointcut() && runTaskPointcut()")
	public void logTask(final ProceedingJoinPoint joinPoint) throws Throwable {
		final var taskName = joinPoint.getSignature().getDeclaringType().getSimpleName();

		logger.debug("Starting execution of task " + taskName);
		final var watch = new StopWatch();
		watch.start();

		joinPoint.proceed();

		watch.stop();
		logger.debug("Task " + taskName + " executed in " + watch.getTotalTimeMillis() + " ms");
	}

	/**
	 * Free the transaction cache after the task has completed
	 */
	@AfterReturning("taskPointcut() && runTaskPointcut()")
	public void emptyCache(final JoinPoint joinPoint) {
		final var taskName = joinPoint.getSignature().getDeclaringType().getSimpleName();

		logger.trace("Free transaction cache for task " + taskName);
		transactionCacheDAOService.emptyCache();
	}

	/**
	 * If an exception has been thrown, log the error and free the transaction cache
	 */
	@AfterThrowing(value = "taskPointcut() && runTaskPointcut()", throwing = "e")
	public void emptyCacheAndLogError(final JoinPoint joinPoint, final Exception e) {
		final var taskName = joinPoint.getSignature().getDeclaringType().getSimpleName();

		logger.error("An exception occurred while executing task " + taskName, e);

		logger.trace("Free transaction cache for task " + taskName);
		transactionCacheDAOService.emptyCache();
	}
}
