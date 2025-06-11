package ch.rodano.core.services.cron;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.rule.RuleService;

@Profile("!test & !migration & !database")
@Service
@ConditionalOnProperty(value = "rodano.schedule.cron-runner", havingValue = "true")
public class CronRunnerServiceImpl implements CronRunnerService, DisposableBean {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Map<String, ZonedDateTime> cronLastRun;

	private final PlatformTransactionManager transactionManager;
	private final RuleService ruleService;
	private final StudyService studyService;
	private final ScopeService scopeService;

	public CronRunnerServiceImpl(
		final PlatformTransactionManager transactionManager,
		final RuleService ruleService,
		final StudyService studyService,
		final ScopeService scopeService
	) {
		this.cronLastRun = new TreeMap<>();

		this.transactionManager = transactionManager;
		this.ruleService = ruleService;
		this.studyService = studyService;
		this.scopeService = scopeService;

		logger.info("Cron runner started");
	}

	/**
	 * Invoked by a BeanFactory on destruction of a singleton.
	 */
	@Override
	public void destroy() {
		logger.info("Cron runner stopped");
	}

	/**
	 * Execute the cron runner
	 */
	@Scheduled(zone = "UTC", cron = "${rodano.schedule.cron-runner.cron}")
	@Override
	public void run() {
		final var now = ZonedDateTime.now();

		final var transactionTemplate = new TransactionTemplate(transactionManager);

		// Manage periodic cron
		for(final var cron : studyService.getStudy().getCrons()) {
			if(cron.getInterval() != null && cron.getIntervalUnit() != null) {
				final var cronId = cron.getId();
				final var lastRun = cronLastRun.get(cronId);
				if(lastRun == null || lastRun.plusSeconds((int) (cron.getInterval() * cron.getIntervalUnit().getDuration().toMillis() / 1000)).isAfter(now)) {
					logger.info(String.format("Executing cron %s", cronId));

					cronLastRun.put(cronId, now);

					// Cron Service
					for(final var scope : scopeService.getAllIncludingRemoved()) {
						if(!scope.getDeleted()) {

							transactionTemplate.execute(_ -> {
								ruleService.execute(new DataState(scope), cron.getRules(), null);
								return true;
							});

						}
					}
				}
			}
		}
	}
}
