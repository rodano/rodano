package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.WorkflowSummary;
import ch.rodano.batch.pojo.WorkflowSummaryColumn;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveEventModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveScopeModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowStateId;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.WorkflowSummary.WORKFLOW_SUMMARY;
import static ch.rodano.core.model.jooq.tables.WorkflowSummaryColumn.WORKFLOW_SUMMARY_COLUMN;
import static ch.rodano.core.model.jooq.tables.WorkflowSummaryColumnState.WORKFLOW_SUMMARY_COLUMN_STATE;
import static ch.rodano.core.model.jooq.tables.WorkflowSummaryFilterEventModel.WORKFLOW_SUMMARY_FILTER_EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.WorkflowSummaryWorkflow.WORKFLOW_SUMMARY_WORKFLOW;

public class WorkflowSummaryWriter extends BaseWriter {

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<WorkflowSummary> wrapped = (ProjectScoped<WorkflowSummary>) raw;
				final UUID projectId = wrapped.getProjectId();
				final WorkflowSummary summary = wrapped.getPayload();

				final String summaryCode = summary.getId();
				final UUID summaryId = deterministic(projectId, "WORKFLOW_SUMMARY", summaryCode);

				final UUID leafScopeModelId = resolveScopeModelId(tx, projectId, summary.getLeafScopeModelId());

				tx.insertInto(WORKFLOW_SUMMARY)
					.set(WORKFLOW_SUMMARY.PROJECT_ID, projectId)
					.set(WORKFLOW_SUMMARY.WORKFLOW_SUMMARY_ID, summaryId)
					.set(WORKFLOW_SUMMARY.CODE, summaryCode)
					.set(WORKFLOW_SUMMARY.WORKFLOW_ENTITY, summary.getWorkflowEntity())
					.set(WORKFLOW_SUMMARY.LEAF_SCOPE_MODEL_ID, leafScopeModelId)
					.set(WORKFLOW_SUMMARY.FILTER_EXPECTED_EVENTS, summary.getFilterExpectedEvents())
					.set(WORKFLOW_SUMMARY.DISPLAY_LEGEND, summary.getDisplayLegend())
					.set(WORKFLOW_SUMMARY.DISPLAY_COLUMN_EXPORT, summary.getDisplayColumnExport())
					.set(WORKFLOW_SUMMARY.TITLE, toJson(summary.getTitle()))
					.onDuplicateKeyUpdate()
					.set(WORKFLOW_SUMMARY.WORKFLOW_ENTITY, summary.getWorkflowEntity())
					.set(WORKFLOW_SUMMARY.LEAF_SCOPE_MODEL_ID, leafScopeModelId)
					.set(WORKFLOW_SUMMARY.FILTER_EXPECTED_EVENTS, summary.getFilterExpectedEvents())
					.set(WORKFLOW_SUMMARY.DISPLAY_LEGEND, summary.getDisplayLegend())
					.set(WORKFLOW_SUMMARY.DISPLAY_COLUMN_EXPORT, summary.getDisplayColumnExport())
					.set(WORKFLOW_SUMMARY.TITLE, toJson(summary.getTitle()))
					.execute();

				if(summary.getFilterEventModelIds() != null && !summary.getFilterEventModelIds().isEmpty()) {
					for(String filterEventCode : summary.getFilterEventModelIds()) {
						final UUID filterEventId = resolveEventModelId(tx, projectId, filterEventCode);
						tx.insertInto(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL)
							.set(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL.PROJECT_ID, projectId)
							.set(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL.WORKFLOW_SUMMARY_ID, summaryId)
							.set(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL.EVENT_MODEL_ID, filterEventId)
							.onDuplicateKeyIgnore()
							.execute();
					}
				}

				if(summary.getWorkflowIds() != null && !summary.getWorkflowIds().isEmpty()) {
					for(String workflowCode : summary.getWorkflowIds()) {
						final UUID workflowId = resolveWorkflowId(tx, projectId, workflowCode);
						tx.insertInto(WORKFLOW_SUMMARY_WORKFLOW)
							.set(WORKFLOW_SUMMARY_WORKFLOW.PROJECT_ID, projectId)
							.set(WORKFLOW_SUMMARY_WORKFLOW.WORKFLOW_SUMMARY_ID, summaryId)
							.set(WORKFLOW_SUMMARY_WORKFLOW.WORKFLOW_ID, workflowId)
							.onDuplicateKeyIgnore()
							.execute();
					}
				}

				if(summary.getColumns() != null && !summary.getColumns().isEmpty()) {
					int colOrder = 0;
					for(WorkflowSummaryColumn column : summary.getColumns()) {
						final UUID columnId = deterministic(projectId, "WORKFLOW_SUMMARY_COLUMN", summaryCode + "|" + colOrder);
						tx.insertInto(WORKFLOW_SUMMARY_COLUMN)
							.set(WORKFLOW_SUMMARY_COLUMN.PROJECT_ID, projectId)
							.set(WORKFLOW_SUMMARY_COLUMN.WORKFLOW_SUMMARY_ID, summaryId)
							.set(WORKFLOW_SUMMARY_COLUMN.SUMMARY_COLUMN_ID, columnId)
							.set(WORKFLOW_SUMMARY_COLUMN.SORT_ORDER, colOrder)
							.set(WORKFLOW_SUMMARY_COLUMN.TOTAL, column.getTotal())
							.set(WORKFLOW_SUMMARY_COLUMN.PERCENT, column.getPercent())
							.set(WORKFLOW_SUMMARY_COLUMN.NON_NULL_COLOR, column.getNonNullColor())
							.set(WORKFLOW_SUMMARY_COLUMN.NON_NULL_BG_COLOR, column.getNonNullBackgroundColor())
							.set(WORKFLOW_SUMMARY_COLUMN.LABEL, toJson(column.getLabel()))
							.set(WORKFLOW_SUMMARY_COLUMN.DESCRIPTION, toJson(column.getDescription()))
							.onDuplicateKeyUpdate()
							.set(WORKFLOW_SUMMARY_COLUMN.SORT_ORDER, colOrder)
							.set(WORKFLOW_SUMMARY_COLUMN.TOTAL, column.getTotal())
							.set(WORKFLOW_SUMMARY_COLUMN.PERCENT, column.getPercent())
							.set(WORKFLOW_SUMMARY_COLUMN.NON_NULL_COLOR, column.getNonNullColor())
							.set(WORKFLOW_SUMMARY_COLUMN.NON_NULL_BG_COLOR, column.getNonNullBackgroundColor())
							.set(WORKFLOW_SUMMARY_COLUMN.LABEL, toJson(column.getLabel()))
							.set(WORKFLOW_SUMMARY_COLUMN.DESCRIPTION, toJson(column.getDescription()))
							.execute();

						if(column.getStateIds() != null && !column.getStateIds().isEmpty()) {
							for(String workflowCode : summary.getWorkflowIds()) {
								for(String stateCode : column.getStateIds()) {
									final UUID workflowId = resolveWorkflowId(tx, projectId, workflowCode);
									final UUID stateId = resolveWorkflowStateId(tx, projectId, workflowId, stateCode);
									tx.insertInto(WORKFLOW_SUMMARY_COLUMN_STATE)
										.set(WORKFLOW_SUMMARY_COLUMN_STATE.PROJECT_ID, projectId)
										.set(WORKFLOW_SUMMARY_COLUMN_STATE.SUMMARY_COLUMN_ID, columnId)
										.set(WORKFLOW_SUMMARY_COLUMN_STATE.WORKFLOW_STATE_ID, stateId)
										.onDuplicateKeyIgnore()
										.execute();
								}
							}
						}
						colOrder++;
					}
				}
			}
		});
	}
}
