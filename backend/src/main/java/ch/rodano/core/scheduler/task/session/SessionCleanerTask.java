package ch.rodano.core.scheduler.task.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.scheduler.task.ScheduledTask;
import ch.rodano.core.services.bll.session.SessionService;

@Component
@ConditionalOnProperty(value = "rodano.schedule.session-cleaner", havingValue = "true")
public class SessionCleanerTask implements ScheduledTask, DisposableBean {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final SessionService sessionService;
	private final Integer sessionDuration;

	public SessionCleanerTask(
		final SessionService sessionService,
		@Value("${rodano.schedule.session-cleaner.session.duration:15}") final int sessionDuration
	) {
		this.sessionService = sessionService;
		this.sessionDuration = sessionDuration;
		logger.info("Session cleaner started - session duration: " + sessionDuration + " minutes");
	}

	@Scheduled(zone = "UTC", cron = "${rodano.schedule.session-cleaner.cron}")
	@Transactional
	@Override
	public void run() {
		sessionService.deleteOldSessions(sessionDuration);
		logger.debug("Old user sessions deleted");
	}

	@Override
	public void destroy() {
		logger.info("Session cleaner stopped");
	}
}
