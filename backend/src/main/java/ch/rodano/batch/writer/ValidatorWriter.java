package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Validator;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowStateId;
import static ch.rodano.batch.helper.RuleHelper.insertConstraintForOwner;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.Validator.VALIDATOR;

public class ValidatorWriter extends BaseWriter {

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<Validator> wrapped = (ProjectScoped<Validator>) raw;
				final UUID projectId = wrapped.getProjectId();
				final Validator validator = wrapped.getPayload();

				final String workflowCode = validator.getWorkflowId();
				final UUID workflowId = resolveWorkflowId(tx, projectId, workflowCode);

				final String validatorCode = validator.getId();
				final UUID validatorId = deterministic(projectId, "VALIDATOR", validatorCode);

				final UUID invalidStateId = resolveWorkflowStateId(tx, projectId, workflowId, validator.getInvalidStateId());
				final UUID validStateId = resolveWorkflowStateId(tx, projectId, workflowId, validator.getValidStateId());

				tx.insertInto(VALIDATOR)
					.set(VALIDATOR.PROJECT_ID, projectId)
					.set(VALIDATOR.VALIDATOR_ID, validatorId)
					.set(VALIDATOR.CODE, validatorCode)
					.set(VALIDATOR.REQUIRED, validator.isRequired())
					.set(VALIDATOR.IS_SCRIPT, validator.isScript())
					.set(VALIDATOR.WORKFLOW_ID, workflowId)
					.set(VALIDATOR.INVALID_STATE_ID, invalidStateId)
					.set(VALIDATOR.VALID_STATE_ID, validStateId)
					.set(VALIDATOR.SHORTNAME, toJson(validator.getShortname()))
					.set(VALIDATOR.LONGNAME, toJson(validator.getLongname()))
					.set(VALIDATOR.DESCRIPTION, toJson(validator.getDescription()))
					.set(VALIDATOR.MESSAGE, toJson(validator.getMessage()))
					.onDuplicateKeyUpdate()
					.set(VALIDATOR.REQUIRED, validator.isRequired())
					.set(VALIDATOR.IS_SCRIPT, validator.isScript())
					.set(VALIDATOR.WORKFLOW_ID, workflowId)
					.set(VALIDATOR.INVALID_STATE_ID, invalidStateId)
					.set(VALIDATOR.VALID_STATE_ID, validStateId)
					.set(VALIDATOR.SHORTNAME, toJson(validator.getShortname()))
					.set(VALIDATOR.LONGNAME, toJson(validator.getLongname()))
					.set(VALIDATOR.DESCRIPTION, toJson(validator.getDescription()))
					.set(VALIDATOR.MESSAGE, toJson(validator.getMessage()))
					.execute();

				if(validator.getConstraint() != null) {
					insertConstraintForOwner(tx, projectId, "VALIDATOR", validatorId, validator.getConstraint());
				}
			}
		});
	}
}
