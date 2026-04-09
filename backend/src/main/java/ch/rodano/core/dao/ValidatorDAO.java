package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.validator.Validator;
import ch.rodano.core.model.jooq.enums.RuleConstraintOwnerType;
import ch.rodano.core.model.jooq.tables.records.ValidatorRecord;

import static ch.rodano.core.model.jooq.tables.Validator.VALIDATOR;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;
import static ch.rodano.core.model.jooq.tables.WorkflowState.WORKFLOW_STATE;

@Repository
public class ValidatorDAO implements BaseProjectDAO<Validator> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final RuleDAO ruleDAO;

	public ValidatorDAO(
		final DSLContext dslContext,
		final MappingHelper mappingHelper,
		final RuleDAO ruleDAO
	) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.ruleDAO = ruleDAO;
	}

	@Override
	public List<Validator> findByProject(final UUID projectId) {
		return dslContext.selectFrom(VALIDATOR)
			.where(VALIDATOR.PROJECT_ID.eq(projectId))
			.fetch(this::mapToModel);
	}

	@Override
	public Validator findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(VALIDATOR)
			.where(VALIDATOR.PROJECT_ID.eq(projectId))
			.and(VALIDATOR.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public Validator findById(final UUID id) {
		return dslContext.selectFrom(VALIDATOR)
			.where(VALIDATOR.VALIDATOR_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public Validator save(final Validator entity) {
		throw new UnsupportedOperationException("Not implemented yet - Phase 3");
	}

	@Override
	public void delete(final UUID id) {
		throw new UnsupportedOperationException("Not implemented yet - Phase 3");
	}

	private Validator mapToModel(final ValidatorRecord record) {
		if(record == null) {
			return null;
		}

		final Validator model = new Validator();

		model.setValidatorId(record.getValidatorId());
		model.setId(record.getCode());

		model.setRequired(record.getRequired() != null ? record.getRequired() : false);
		model.setScript(record.getIsScript() != null ? record.getIsScript() : false);

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));
		model.setMessage(mappingHelper.parseJsonToMap(record.getMessage()));

		if(record.getWorkflowId() != null) {
			model.setWorkflowId(getWorkflowCode(record.getWorkflowId()));
		}

		if(record.getInvalidStateId() != null) {
			model.setInvalidStateId(getWorkflowStateCode(record.getInvalidStateId()));
		}

		if(record.getValidStateId() != null) {
			model.setValidStateId(getWorkflowStateCode(record.getValidStateId()));
		}

		model.setConstraint(ruleDAO.loadConstraintForOwner(RuleConstraintOwnerType.VALIDATOR, record.getValidatorId()));

		return model;
	}

	private String getWorkflowCode(final UUID workflowId) {
		return dslContext.select(WORKFLOW.CODE)
			.from(WORKFLOW)
			.where(WORKFLOW.WORKFLOW_ID.eq(workflowId))
			.fetchOne(WORKFLOW.CODE);
	}

	private String getWorkflowStateCode(final UUID stateId) {
		return dslContext.select(WORKFLOW_STATE.CODE)
			.from(WORKFLOW_STATE)
			.where(WORKFLOW_STATE.WORKFLOW_STATE_ID.eq(stateId))
			.fetchOne(WORKFLOW_STATE.CODE);
	}
}
