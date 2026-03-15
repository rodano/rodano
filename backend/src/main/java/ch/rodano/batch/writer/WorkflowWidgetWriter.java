package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.WorkflowStatesSelector;
import ch.rodano.batch.pojo.WorkflowWidget;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowStateId;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.WorkflowWidget.WORKFLOW_WIDGET;
import static ch.rodano.core.model.jooq.tables.WorkflowWidgetColumn.WORKFLOW_WIDGET_COLUMN;
import static ch.rodano.core.model.jooq.tables.WorkflowWidgetStateSelector.WORKFLOW_WIDGET_STATE_SELECTOR;

public class WorkflowWidgetWriter extends BaseWriter {

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<WorkflowWidget> wrapped = (ProjectScoped<WorkflowWidget>) raw;
				final UUID projectId = wrapped.getProjectId();
				final WorkflowWidget widget = wrapped.getPayload();

				final String widgetCode = widget.getId();
				final UUID widgetId = deterministic(projectId, "WORKFLOW_WIDGET", widgetCode);
				tx.insertInto(WORKFLOW_WIDGET)
					.set(WORKFLOW_WIDGET.PROJECT_ID, projectId)
					.set(WORKFLOW_WIDGET.WORKFLOW_WIDGET_ID, widgetId)
					.set(WORKFLOW_WIDGET.CODE, widgetCode)
					.set(WORKFLOW_WIDGET.WORKFLOW_ENTITY, widget.getWorkflowEntity())
					.set(WORKFLOW_WIDGET.FILTER_EXPECTED_EVENTS, widget.getFilterExpectedEvents())
					.set(WORKFLOW_WIDGET.SHORTNAME, toJson(widget.getShortname()))
					.set(WORKFLOW_WIDGET.LONGNAME, toJson(widget.getLongname()))
					.set(WORKFLOW_WIDGET.DESCRIPTION, toJson(widget.getDescription()))
					.onDuplicateKeyUpdate()
					.set(WORKFLOW_WIDGET.WORKFLOW_ENTITY, widget.getWorkflowEntity())
					.set(WORKFLOW_WIDGET.FILTER_EXPECTED_EVENTS, widget.getFilterExpectedEvents())
					.set(WORKFLOW_WIDGET.SHORTNAME, toJson(widget.getShortname()))
					.set(WORKFLOW_WIDGET.LONGNAME, toJson(widget.getLongname()))
					.set(WORKFLOW_WIDGET.DESCRIPTION, toJson(widget.getDescription()))
					.execute();

				if(widget.getColumns() != null && !widget.getColumns().isEmpty()) {
					for(int i = 0; i < widget.getColumns().size(); i++) {
						final var col = widget.getColumns().get(i);
						final String columnCode = col.getId();
						final UUID columnId = deterministic(projectId, "WORKFLOW_WIDGET_COLUMN", widgetCode + "|" + columnCode);

						tx.insertInto(WORKFLOW_WIDGET_COLUMN)
							.set(WORKFLOW_WIDGET_COLUMN.PROJECT_ID, projectId)
							.set(WORKFLOW_WIDGET_COLUMN.WORKFLOW_WIDGET_ID, widgetId)
							.set(WORKFLOW_WIDGET_COLUMN.WORKFLOW_WIDGET_COLUMN_ID, columnId)
							.set(WORKFLOW_WIDGET_COLUMN.CODE, columnCode)
							.set(WORKFLOW_WIDGET_COLUMN.TYPE, col.getType())
							.set(WORKFLOW_WIDGET_COLUMN.WIDTH, col.getWidth())
							.set(WORKFLOW_WIDGET_COLUMN.SHORTNAME, toJson(col.getShortname()))
							.set(WORKFLOW_WIDGET_COLUMN.LONGNAME, toJson(col.getLongname()))
							.set(WORKFLOW_WIDGET_COLUMN.DESCRIPTION, toJson(col.getDescription()))
							.set(WORKFLOW_WIDGET_COLUMN.SORT_ORDER, i)
							.onDuplicateKeyUpdate()
							.set(WORKFLOW_WIDGET_COLUMN.TYPE, col.getType())
							.set(WORKFLOW_WIDGET_COLUMN.WIDTH, col.getWidth())
							.set(WORKFLOW_WIDGET_COLUMN.SHORTNAME, toJson(col.getShortname()))
							.set(WORKFLOW_WIDGET_COLUMN.LONGNAME, toJson(col.getLongname()))
							.set(WORKFLOW_WIDGET_COLUMN.DESCRIPTION, toJson(col.getDescription()))
							.set(WORKFLOW_WIDGET_COLUMN.SORT_ORDER, i)
							.execute();
					}
				}

				if(widget.getWorkflowStatesSelectors() != null && !widget.getWorkflowStatesSelectors().isEmpty()) {
					for(WorkflowStatesSelector selector : widget.getWorkflowStatesSelectors()) {
						final UUID workflowId = resolveWorkflowId(tx, projectId, selector.getWorkflowId());

						final List<String> stateCodes = selector.getStateIds();
						for(String stateCode : stateCodes) {
							final UUID stateId = resolveWorkflowStateId(tx, projectId, workflowId, stateCode);
							tx.insertInto(WORKFLOW_WIDGET_STATE_SELECTOR)
								.set(WORKFLOW_WIDGET_STATE_SELECTOR.PROJECT_ID, projectId)
								.set(WORKFLOW_WIDGET_STATE_SELECTOR.WORKFLOW_WIDGET_ID, widgetId)
								.set(WORKFLOW_WIDGET_STATE_SELECTOR.WORKFLOW_ID, workflowId)
								.set(WORKFLOW_WIDGET_STATE_SELECTOR.WORKFLOW_STATE_ID, stateId)
								.onDuplicateKeyIgnore()
								.execute();
						}
					}
				}
			}
		});
	}
}
