package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Cron;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.Cron.CRON;

public class CronWriter extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(CronWriter.class);

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<Cron> wrapped = (ProjectScoped<Cron>) raw;
				final UUID projectId = wrapped.getProjectId();
				final Cron cron = wrapped.getPayload();

				final String cronCode = cron.getId();
				if(cronCode == null || cronCode.isBlank()) {
					LOGGER.warn("Skipping cron with null/blank id");
					continue;
				}

				final UUID cronId = deterministic(projectId, "CRON", cronCode);

				tx.insertInto(CRON)
					.set(CRON.PROJECT_ID, projectId)
					.set(CRON.CRON_ID, cronId)
					.set(CRON.CODE, cronCode)
					.set(CRON.INTERVAL_VALUE, cron.getInterval())
					.set(CRON.INTERVAL_UNIT, cron.getIntervalUnit())
					.set(CRON.DESCRIPTION, toJson(cron.getDescription()))
					.onDuplicateKeyUpdate()
					.set(CRON.INTERVAL_VALUE, cron.getInterval())
					.set(CRON.INTERVAL_UNIT, cron.getIntervalUnit())
					.set(CRON.DESCRIPTION, toJson(cron.getDescription()))
					.execute();
			}
		});
	}
}
