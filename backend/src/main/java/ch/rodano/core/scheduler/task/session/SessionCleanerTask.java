package ch.rodano.core.scheduler.task.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ch.rodano.core.scheduler.task.ScheduledTask;
import ch.rodano.core.services.bll.session.SessionService;

@Component
@ConditionalOnProperty(value = "rodano.schedule.session-cleaner", havingValue = "true")
public class SessionCleanerTask implements ScheduledTask, DisposableBean {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final PlatformTransactionManager transactionManager;
	private final SessionService sessionService;
	private final Integer sessionDuration;

	public SessionCleanerTask(
		final PlatformTransactionManager transactionManager,
		final SessionService sessionService,
		@Value("${rodano.schedule.session-cleaner.session.duration:15}") final int sessionDuration
	) {
		this.transactionManager = transactionManager;
		this.sessionService = sessionService;
		this.sessionDuration = sessionDuration;
		logger.info("Session cleaner started - session duration: " + sessionDuration + " minutes");
	}

	@Scheduled(zone = "UTC", cron = "${rodano.schedule.session-cleaner.cron}")
	@Override
	public void run() {
		logger.debug("Deleting old user sessions...");
		final var transactionTemplate = new TransactionTemplate(transactionManager);

		transactionTemplate.execute(_ -> {
			sessionService.deleteOldSessions(sessionDuration);
			return true;
		});
	}

	@Override
	public void destroy() {
		logger.info("Session cleaner stopped");
	}
}
