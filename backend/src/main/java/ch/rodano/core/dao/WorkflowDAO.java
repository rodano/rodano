package ch.rodano.core.dao;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.configuration.model.workflow.Action;
import ch.rodano.configuration.model.workflow.StateMatcher;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowState;
import ch.rodano.core.model.jooq.tables.records.WorkflowRecord;

import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;
import static ch.rodano.core.model.jooq.tables.WorkflowAction.WORKFLOW_ACTION;
import static ch.rodano.core.model.jooq.tables.WorkflowState.WORKFLOW_STATE;
import static ch.rodano.core.model.jooq.tables.WorkflowStatePossibleAction.WORKFLOW_STATE_POSSIBLE_ACTION;

@Repository
public class WorkflowDAO implements BaseProjectDAO<Workflow> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;

	public WorkflowDAO(
		final DSLContext dslContext,
		final MappingHelper mappingHelper
	) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
	}

	@Override
	public List<Workflow> findByProject(final UUID projectId) {
		return dslContext.selectFrom(WORKFLOW)
			.where(WORKFLOW.PROJECT_ID.eq(projectId))
			.orderBy(WORKFLOW.ORDER_BY)
			.fetch(this::mapToModel);
	}

	@Override
	public Workflow findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(WORKFLOW)
			.where(WORKFLOW.PROJECT_ID.eq(projectId))
			.and(WORKFLOW.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public Workflow findById(final UUID id) {
		return dslContext.selectFrom(WORKFLOW)
			.where(WORKFLOW.WORKFLOW_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public Workflow save(final Workflow entity) {
		throw new UnsupportedOperationException("Not implemented yet - Phase 3");
	}

	@Override
	public void delete(final UUID id) {
		throw new UnsupportedOperationException("Not implemented yet - Phase 3");
	}

	private Workflow mapToModel(final WorkflowRecord record) {
		if(record == null) {
			return null;
		}

		final Workflow model = new Workflow();

		model.setWorkflowId(record.getWorkflowId());
		model.setId(record.getCode());

		model.setOrderBy(record.getOrderBy());
		model.setMandatory(record.getMandatory() != null ? record.getMandatory() : true);
		model.setUnique(record.getIsUnique() != null ? record.getIsUnique() : false);
		model.setIcon(record.getIcon());

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));
		model.setMessage(mappingHelper.parseJsonToMap(record.getMessage()));

		if(record.getInitialStateId() != null) {
			model.setInitialStateId(getWorkflowStateCode(record.getInitialStateId()));
		}

		if(record.getAggregateWorkflowId() != null) {
			model.setAggregateWorkflowId(getWorkflowCode(record.getAggregateWorkflowId()));
		}

		model.setStates(loadWorkflowStates(record.getWorkflowId()));
		model.setActions(loadWorkflowActions(record.getWorkflowId()));

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

	private List<WorkflowState> loadWorkflowStates(final UUID workflowId) {
		final var records = dslContext.selectFrom(WORKFLOW_STATE)
			.where(WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId))
			.orderBy(WORKFLOW_STATE.STATE_ORDER)
			.fetch();

		return records.map(record -> {
			final WorkflowState state = new WorkflowState();

			state.setId(record.getCode());
			state.setWorkflowStateId(record.getWorkflowStateId());

			state.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
			state.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
			state.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

			state.setColor(record.getColor());
			state.setImportant(record.getImportant() != null ? record.getImportant() : false);
			state.setIcon(record.getIcon());

			if(record.getAggregateStateMatcher() != null) {
				state.setAggregateStateMatcher(
					mappingHelper.parseEnum(StateMatcher.class, record.getAggregateStateMatcher(), "aggregateStateMatcher")
				);
			}

			if(record.getAggregateStateId() != null) {
				state.setAggregateStateId(getWorkflowStateCode(record.getAggregateStateId()));
			}

			state.setPossibleActionIds(loadStatePossibleActions(record.getWorkflowStateId()));

			return state;
		});
	}

	private List<String> loadStatePossibleActions(final UUID stateId) {
		return dslContext.select(WORKFLOW_ACTION.CODE)
			.from(WORKFLOW_STATE_POSSIBLE_ACTION)
			.join(WORKFLOW_ACTION).on(WORKFLOW_ACTION.WORKFLOW_ACTION_ID.eq(WORKFLOW_STATE_POSSIBLE_ACTION.WORKFLOW_ACTION_ID))
			.where(WORKFLOW_STATE_POSSIBLE_ACTION.WORKFLOW_STATE_ID.eq(stateId))
			.fetch(WORKFLOW_ACTION.CODE);
	}

	private SortedSet<Action> loadWorkflowActions(final UUID workflowId) {
		final var records = dslContext.selectFrom(WORKFLOW_ACTION)
			.where(WORKFLOW_ACTION.WORKFLOW_ID.eq(workflowId))
			.orderBy(WORKFLOW_ACTION.ACTION_ORDER)
			.fetch();

		final SortedSet<Action> actions = new TreeSet<>();

		records.forEach(record -> {
			final Action action = new Action();

			action.setId(record.getCode());
			action.setWorkflowActionId(record.getWorkflowActionId());

			action.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
			action.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
			action.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

			action.setDocumentable(record.getDocumentable() != null ? record.getDocumentable() : false);
			if(record.getDocumentableOptions() != null && !record.getDocumentableOptions().isBlank()) {
				action.setDocumentableOptions(
					mappingHelper.parseJson(
						record.getDocumentableOptions(),
						new TypeReference<>() {}
					)
				);
			}

			action.setRequireSignature(record.getRequireSignature() != null ? record.getRequireSignature() : false);
			action.setRequiredSignatureText(mappingHelper.parseJsonToMap(record.getRequiredSignatureText()));

			actions.add(action);
		});

		return actions;
	}
}
