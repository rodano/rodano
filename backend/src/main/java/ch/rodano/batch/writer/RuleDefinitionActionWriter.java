package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.RuleDefinitionAction;
import ch.rodano.batch.pojo.RuleDefinitionActionParameter;

import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.RuleDefinitionAction.RULE_DEFINITION_ACTION;
import static ch.rodano.core.model.jooq.tables.RuleDefinitionActionParameter.RULE_DEFINITION_ACTION_PARAMETER;

public class RuleDefinitionActionWriter extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RuleDefinitionActionWriter.class);

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<RuleDefinitionAction> wrapped = (ProjectScoped<RuleDefinitionAction>) raw;
				final UUID projectId = wrapped.getProjectId();
				final RuleDefinitionAction action = wrapped.getPayload();

				final String actionCode = action.getId();
				if(actionCode == null || actionCode.isBlank()) {
					LOGGER.warn("Skipping rule definition action with blank code");
					continue;
				}
				final UUID actionId = deterministic(projectId, "RULE_DEFINITION_ACTION", actionCode);
				tx.insertInto(RULE_DEFINITION_ACTION)
					.set(RULE_DEFINITION_ACTION.PROJECT_ID, projectId)
					.set(RULE_DEFINITION_ACTION.RULE_DEFINITION_ACTION_ID, actionId)
					.set(RULE_DEFINITION_ACTION.CODE, actionCode)
					.set(RULE_DEFINITION_ACTION.LABEL, action.getLabel())
					.set(RULE_DEFINITION_ACTION.ENTITY_ID, action.getEntityId())
					.onDuplicateKeyUpdate()
					.set(RULE_DEFINITION_ACTION.LABEL, action.getLabel())
					.set(RULE_DEFINITION_ACTION.ENTITY_ID, action.getEntityId())
					.execute();

				if(action.getParameters() != null && !action.getParameters().isEmpty()) {
					int sortOrder = 0;
					for(RuleDefinitionActionParameter parameter : action.getParameters()) {
						final String paramCode = parameter.getId();
						tx.insertInto(RULE_DEFINITION_ACTION_PARAMETER)
							.set(RULE_DEFINITION_ACTION_PARAMETER.PROJECT_ID, projectId)
							.set(RULE_DEFINITION_ACTION_PARAMETER.RULE_DEFINITION_ACTION_ID, actionId)
							.set(RULE_DEFINITION_ACTION_PARAMETER.PARAM_CODE, paramCode)
							.set(RULE_DEFINITION_ACTION_PARAMETER.LABEL, parameter.getLabel())
							.set(RULE_DEFINITION_ACTION_PARAMETER.DATA_ENTITY, parameter.getDataEntity())
							.set(RULE_DEFINITION_ACTION_PARAMETER.CONFIGURATION_ENTITY, parameter.getConfigurationEntity())
							.set(RULE_DEFINITION_ACTION_PARAMETER.OPTIONS, parameter.getOptions())
							.set(RULE_DEFINITION_ACTION_PARAMETER.SORT_ORDER, sortOrder)
							.onDuplicateKeyUpdate()
							.set(RULE_DEFINITION_ACTION_PARAMETER.PARAM_CODE, paramCode)
							.set(RULE_DEFINITION_ACTION_PARAMETER.LABEL, parameter.getLabel())
							.set(RULE_DEFINITION_ACTION_PARAMETER.DATA_ENTITY, parameter.getDataEntity())
							.set(RULE_DEFINITION_ACTION_PARAMETER.CONFIGURATION_ENTITY, parameter.getConfigurationEntity())
							.set(RULE_DEFINITION_ACTION_PARAMETER.OPTIONS, parameter.getOptions())
							.set(RULE_DEFINITION_ACTION_PARAMETER.SORT_ORDER, sortOrder)
							.execute();
						sortOrder++;
					}
				}
			}
		});
	}
}
