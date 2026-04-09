package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Validator;
import ch.rodano.core.model.jooq.enums.RuleConstraintConstraintType;

import static ch.rodano.batch.helper.RuleHelper.insertConstraintForOwner;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.Validator.VALIDATOR;

public class ValidatorRuleWriter extends BaseWriter {

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<Validator> wrapped = (ProjectScoped<Validator>) raw;
				final UUID projectId = wrapped.getProjectId();
				final Validator validator = wrapped.getPayload();

				final UUID validatorId = deterministic(projectId, "VALIDATOR", validator.getId());

				final UUID validatorWorkflowId = tx.select(VALIDATOR.WORKFLOW_ID)
					.from(VALIDATOR)
					.where(VALIDATOR.VALIDATOR_ID.eq(validatorId))
					.fetchOne(VALIDATOR.WORKFLOW_ID);

				if(validator.getConstraint() != null) {
					insertConstraintForOwner(tx, projectId, "VALIDATOR", validatorId,
						validator.getConstraint(), RuleConstraintConstraintType.VALIDATION, validatorWorkflowId);
				}
			}
		});
	}
}
