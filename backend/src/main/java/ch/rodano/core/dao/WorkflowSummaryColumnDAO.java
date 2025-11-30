package ch.rodano.core.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.reports.WorkflowSummaryColumn;
import ch.rodano.core.model.jooq.tables.records.WorkflowSummaryColumnRecord;

import static ch.rodano.core.model.jooq.tables.WorkflowState.WORKFLOW_STATE;
import static ch.rodano.core.model.jooq.tables.WorkflowSummaryColumn.WORKFLOW_SUMMARY_COLUMN;
import static ch.rodano.core.model.jooq.tables.WorkflowSummaryColumnState.WORKFLOW_SUMMARY_COLUMN_STATE;

@Repository
public class WorkflowSummaryColumnDAO {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;

	public WorkflowSummaryColumnDAO(final DSLContext dslContext, final MappingHelper mappingHelper) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
	}

	public List<WorkflowSummaryColumn> findByWorkflowSummary(final UUID summaryId) {
		return dslContext.selectFrom(WORKFLOW_SUMMARY_COLUMN)
			.where(WORKFLOW_SUMMARY_COLUMN.WORKFLOW_SUMMARY_ID.eq(summaryId))
			.orderBy(WORKFLOW_SUMMARY_COLUMN.SORT_ORDER)
			.fetch(this::mapToModel);
	}

	private WorkflowSummaryColumn mapToModel(final WorkflowSummaryColumnRecord record) {
		if(record == null) {
			return null;
		}

		final WorkflowSummaryColumn model = new WorkflowSummaryColumn();

		model.setSummaryColumnId(record.getSummaryColumnId());

		model.setTotal(record.getTotal() != null ? record.getTotal() : false);
		model.setPercent(record.getPercent() != null ? record.getPercent() : false);
		model.setNonNullColor(record.getNonNullColor());
		model.setNonNullBackgroundColor(record.getNonNullBgColor());

		model.setLabel(mappingHelper.parseJsonToMap(record.getLabel()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

		model.setStateIds(loadStateIds(record.getSummaryColumnId()));

		return model;
	}

	private Set<String> loadStateIds(final UUID columnId) {
		final var codes = dslContext.select(WORKFLOW_STATE.CODE)
			.from(WORKFLOW_SUMMARY_COLUMN_STATE)
			.join(WORKFLOW_STATE).on(WORKFLOW_STATE.WORKFLOW_STATE_ID.eq(WORKFLOW_SUMMARY_COLUMN_STATE.WORKFLOW_STATE_ID))
			.where(WORKFLOW_SUMMARY_COLUMN_STATE.SUMMARY_COLUMN_ID.eq(columnId))
			.fetch(WORKFLOW_STATE.CODE);

		return new HashSet<>(codes);
	}
}
