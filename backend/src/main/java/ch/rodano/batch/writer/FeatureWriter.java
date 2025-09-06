package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Feature;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.Feature.FEATURE;

public class FeatureWriter extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureWriter.class);

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<Feature> wrapped = (ProjectScoped<Feature>) raw;
				final UUID projectId = wrapped.getProjectId();
				final Feature feature = wrapped.getPayload();

				final String featureCode = feature.getId();
				if(featureCode == null || featureCode.isBlank()) {
					LOGGER.warn("Skipping feature with null/blank code");
					continue;
				}
				final UUID featureId = deterministic(projectId, "FEATURE", featureCode);

				tx.insertInto(FEATURE)
					.set(FEATURE.PROJECT_ID, projectId)
					.set(FEATURE.FEATURE_ID, featureId)
					.set(FEATURE.CODE, featureCode)
					.set(FEATURE.OPTIONAL, feature.getOptional())
					.set(FEATURE.SHORTNAME, toJson(feature.getShortname()))
					.set(FEATURE.LONGNAME, toJson(feature.getLongname()))
					.set(FEATURE.DESCRIPTION, toJson(feature.getDescription()))
					.onDuplicateKeyUpdate()
					.set(FEATURE.OPTIONAL, feature.getOptional())
					.execute();
			}
		});
	}
}
