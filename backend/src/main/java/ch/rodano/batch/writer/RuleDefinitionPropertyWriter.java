package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.RuleDefinitionProperty;

import static ch.rodano.core.model.jooq.tables.RuleDefinitionProperty.RULE_DEFINITION_PROPERTY;

public class RuleDefinitionPropertyWriter extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RuleDefinitionPropertyWriter.class);

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<RuleDefinitionProperty> wrapped = (ProjectScoped<RuleDefinitionProperty>) raw;
				final UUID projectId = wrapped.getProjectId();
				final RuleDefinitionProperty property = wrapped.getPayload();

				final String code = property.getId();
				if(code == null || code.isBlank()) {
					LOGGER.warn("Skipping rule definition property with blank code");
					continue;
				}

				tx.insertInto(RULE_DEFINITION_PROPERTY)
					.set(RULE_DEFINITION_PROPERTY.PROJECT_ID, projectId)
					.set(RULE_DEFINITION_PROPERTY.CODE, code)
					.set(RULE_DEFINITION_PROPERTY.LABEL, property.getLabel())
					.set(RULE_DEFINITION_PROPERTY.ENTITY_ID, property.getEntityId())
					.set(RULE_DEFINITION_PROPERTY.TARGET, property.getTarget())
					.set(RULE_DEFINITION_PROPERTY.TYPE, property.getType())
					.set(RULE_DEFINITION_PROPERTY.CONFIGURATION_ENTITY, property.getConfigurationEntity())
					.onDuplicateKeyUpdate()
					.set(RULE_DEFINITION_PROPERTY.LABEL, property.getLabel())
					.set(RULE_DEFINITION_PROPERTY.ENTITY_ID, property.getEntityId())
					.set(RULE_DEFINITION_PROPERTY.TARGET, property.getTarget())
					.set(RULE_DEFINITION_PROPERTY.TYPE, property.getType())
					.set(RULE_DEFINITION_PROPERTY.CONFIGURATION_ENTITY, property.getConfigurationEntity())
					.execute();
			}
		});
	}
}
