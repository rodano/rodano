package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.reports.WorkflowWidgetColumn;
import ch.rodano.configuration.model.reports.WorkflowWidgetColumnType;
import ch.rodano.core.model.jooq.tables.records.WorkflowWidgetColumnRecord;

import static ch.rodano.core.model.jooq.tables.WorkflowWidgetColumn.WORKFLOW_WIDGET_COLUMN;

@Repository
public class WorkflowWidgetColumnDAO {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;

	public WorkflowWidgetColumnDAO(final DSLContext dslContext, final MappingHelper mappingHelper) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
	}

	public List<WorkflowWidgetColumn> findByWorkflowWidget(final UUID workflowWidgetId) {
		return dslContext.selectFrom(WORKFLOW_WIDGET_COLUMN)
			.where(WORKFLOW_WIDGET_COLUMN.WORKFLOW_WIDGET_ID.eq(workflowWidgetId))
			.fetch(this::mapToModel);
	}

	private WorkflowWidgetColumn mapToModel(final WorkflowWidgetColumnRecord record) {
		if(record == null) {
			return null;
		}

		final WorkflowWidgetColumn model = new WorkflowWidgetColumn();

		model.setId(record.getCode());
		model.setWorkflowWidgetColumnId(record.getWorkflowWidgetColumnId());

		model.setWidth(record.getWidth() != null ? record.getWidth() : 0);

		model.setType(mappingHelper.parseEnum(WorkflowWidgetColumnType.class, record.getType(), "type"));

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

		return model;
	}
}
