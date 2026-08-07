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
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.scheduler.task.ScheduledTask;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.rule.RuleService;

@Profile("!test & !migration & !database")
@Service
@ConditionalOnProperty(value = "rodano.schedule.cron-runner", havingValue = "true")
public class CronRunnerServiceImpl implements CronRunnerService, ScheduledTask, DisposableBean {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Map<String, ZonedDateTime> cronLastRun;

	private final RuleService ruleService;
	private final StudyService studyService;
	private final ScopeService scopeService;

	public CronRunnerServiceImpl(
		final RuleService ruleService,
		final StudyService studyService,
		final ScopeService scopeService
	) {
		this.cronLastRun = new TreeMap<>();

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
	@Transactional
	@Override
	public void run() {
		final var now = ZonedDateTime.now();

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
						if(!scope.isRemoved()) {
							ruleService.execute(new DataState(scope), cron.getRules(), null);
						}
					}
				}
			}
		}
	}
}
