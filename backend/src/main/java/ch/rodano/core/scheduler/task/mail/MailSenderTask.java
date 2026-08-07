package ch.rodano.core.scheduler.task.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.configuration.core.Configurator;
import ch.rodano.core.configuration.core.Environment;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.mail.MailStatus;
import ch.rodano.core.scheduler.task.ScheduledTask;
import ch.rodano.core.services.bll.mail.MailService;
import ch.rodano.core.services.dao.audit.AuditActionService;

@Component
@ConditionalOnProperty(value = "rodano.schedule.mail-sender", havingValue = "true")
public class MailSenderTask implements ScheduledTask, DisposableBean {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final MailService mailService;
	private final AuditActionService auditActionService;
	private final Configurator configurator;

	private final Integer maxBatchSize;

	public MailSenderTask(
		final MailService mailService,
		final AuditActionService auditActionService,
		@Value("${rodano.schedule.mail-sender.max-batch-size:100}") final Integer maxBatchSize,
		final Configurator configurator
	) {
		this.mailService = mailService;
		this.auditActionService = auditActionService;
		this.configurator = configurator;
		this.maxBatchSize = maxBatchSize;
		logger.info("Mail sender started");
	}

	@Scheduled(zone = "UTC", cron = "${rodano.schedule.mail-sender.cron}")
	@Transactional
	@Override
	public void run() {
		final var mails = mailService.getMails(MailStatus.PENDING, maxBatchSize);
		if(mails.isEmpty()) {
			return;
		}

		final var context = auditActionService.createAuditActionAndGenerateContext(Actor.SYSTEM, "Running mail sender task");
		final var simulate = !Environment.PROD.equals(configurator.getEnvironment());

		for(final var mail : mails) {
			mailService.sendMail(mail, context, simulate);
		}
		logger.debug("Sent {} pending mails", mails.size());
	}

	@Override
	public void destroy() {
		logger.info("Mail sender stopped");
	}
}
