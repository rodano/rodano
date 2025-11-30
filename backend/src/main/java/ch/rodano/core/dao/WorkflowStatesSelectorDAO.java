package ch.rodano.core.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.reports.WorkflowStatesSelector;

import static ch.rodano.core.model.jooq.tables.ScopeModelWorkflowStateSelector.SCOPE_MODEL_WORKFLOW_STATE_SELECTOR;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;
import static ch.rodano.core.model.jooq.tables.WorkflowState.WORKFLOW_STATE;
import static ch.rodano.core.model.jooq.tables.WorkflowWidgetStateSelector.WORKFLOW_WIDGET_STATE_SELECTOR;

@Repository
public class WorkflowStatesSelectorDAO {

	private final DSLContext dslContext;

	public WorkflowStatesSelectorDAO(final DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	public List<WorkflowStatesSelector> findByScopeModel(final UUID scopeModelId) {
		return dslContext.selectDistinct(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.WORKFLOW_ID)
			.from(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR)
			.where(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetch(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.WORKFLOW_ID)
			.stream()
			.map(workflowId -> createSelector(workflowId, loadStateCodesForScopeModel(scopeModelId, workflowId)))
			.toList();
	}

	public List<WorkflowStatesSelector> findByWorkflowWidget(final UUID workflowWidgetId) {
		return dslContext.selectDistinct(WORKFLOW_WIDGET_STATE_SELECTOR.WORKFLOW_ID)
			.from(WORKFLOW_WIDGET_STATE_SELECTOR)
			.where(WORKFLOW_WIDGET_STATE_SELECTOR.WORKFLOW_WIDGET_ID.eq(workflowWidgetId))
			.fetch(WORKFLOW_WIDGET_STATE_SELECTOR.WORKFLOW_ID)
			.stream()
			.map(workflowId -> createSelector(workflowId, loadStateCodesForWorkflowWidget(workflowWidgetId, workflowId)))
			.toList();
	}

	private WorkflowStatesSelector createSelector(final UUID workflowId, final Set<String> stateCodes) {
		final WorkflowStatesSelector model = new WorkflowStatesSelector();
		model.setWorkflowId(getWorkflowCode(workflowId));
		model.setStateIds(stateCodes);
		return model;
	}

	private Set<String> loadStateCodesForScopeModel(final UUID scopeModelId, final UUID workflowId) {
		final var codes = dslContext.select(WORKFLOW_STATE.CODE)
			.from(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR)
			.join(WORKFLOW_STATE)
			.on(WORKFLOW_STATE.WORKFLOW_STATE_ID.eq(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.WORKFLOW_STATE_ID))
			.where(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.SCOPE_MODEL_ID.eq(scopeModelId))
			.and(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.WORKFLOW_ID.eq(workflowId))
			.fetch(WORKFLOW_STATE.CODE);

		return new HashSet<>(codes);
	}

	private Set<String> loadStateCodesForWorkflowWidget(final UUID workflowWidgetId, final UUID workflowId) {
		final var codes = dslContext.select(WORKFLOW_STATE.CODE)
			.from(WORKFLOW_WIDGET_STATE_SELECTOR)
			.join(WORKFLOW_STATE)
			.on(WORKFLOW_STATE.WORKFLOW_STATE_ID.eq(WORKFLOW_WIDGET_STATE_SELECTOR.WORKFLOW_STATE_ID))
			.where(WORKFLOW_WIDGET_STATE_SELECTOR.WORKFLOW_WIDGET_ID.eq(workflowWidgetId))
			.and(WORKFLOW_WIDGET_STATE_SELECTOR.WORKFLOW_ID.eq(workflowId))
			.fetch(WORKFLOW_STATE.CODE);

		return new HashSet<>(codes);
	}

	private String getWorkflowCode(final UUID workflowId) {
		return dslContext.select(WORKFLOW.CODE)
			.from(WORKFLOW)
			.where(WORKFLOW.WORKFLOW_ID.eq(workflowId))
			.fetchOne(WORKFLOW.CODE);
	}
}
