package ch.rodano.core.scheduler.task.test;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ch.rodano.core.database.initializer.DatabaseInitializer;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.scheduler.task.ScheduledTask;
import ch.rodano.core.services.bll.user.UserService;
import ch.rodano.core.services.dao.audit.AuditActionService;

@Component
@ConditionalOnProperty(value = "rodano.schedule.hello", havingValue = "true")
public class HelloTask implements ScheduledTask {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final PlatformTransactionManager transactionManager;
	private final UserService userService;
	private final AuditActionService auditActionService;

	public HelloTask(
		final PlatformTransactionManager transactionManager,
		final UserService userService,
		final AuditActionService auditActionService
	) {
		this.transactionManager = transactionManager;
		this.userService = userService;
		this.auditActionService = auditActionService;
	}

	@Scheduled(zone = "UTC", cron = "${rodano.schedule.hello.cron}")
	@Override
	public void run() {
		logger.info("Hello from hello task :)");

		final var transactionTemplate = new TransactionTemplate(transactionManager);

		transactionTemplate.execute(_ -> {
			final var rationale = "Hello scheduled task";
			final var context = auditActionService.createAuditActionAndGenerateContext(Actor.SYSTEM, rationale);

			//update something in the database to check that the environment works properly
			final var user = userService.getUserByEmail(DatabaseInitializer.TEST_USER_EMAIL);
			user.setUserAgent(RandomStringUtils.randomAlphanumeric(8));
			userService.saveUser(user, context, "Updating user from a task");

			return true;
		});
	}
}
