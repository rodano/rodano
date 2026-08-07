package ch.rodano.core.scheduler.task.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.scheduler.task.ScheduledTask;
import ch.rodano.core.services.bll.session.PendingSessionUpdates;
import ch.rodano.core.services.bll.session.SessionService;

@Component
public class SessionUpdaterTask implements ScheduledTask, DisposableBean {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final SessionService sessionService;
	private final PendingSessionUpdates pendingSessionUpdates;

	public SessionUpdaterTask(
		final SessionService sessionService,
		final PendingSessionUpdates pendingSessionUpdates
	) {
		this.sessionService = sessionService;
		this.pendingSessionUpdates = pendingSessionUpdates;
		logger.info("Session updater started");
	}

	@Scheduled(zone = "UTC", cron = "${rodano.schedule.session-updater.cron}")
	@Transactional
	@Override
	public void run() {
		final var updates = pendingSessionUpdates.drain();
		if(updates.isEmpty()) {
			return;
		}

		updates.forEach(sessionService::updateLastAccessTime);
		logger.debug("Updated last access time for {} sessions", updates.size());
	}

	@Override
	public void destroy() {
		logger.info("Session updater stopped");
	}
}
