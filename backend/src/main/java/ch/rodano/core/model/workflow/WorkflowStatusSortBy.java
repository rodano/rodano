package ch.rodano.core.model.workflow;

import org.jooq.TableField;

import ch.rodano.core.model.jooq.tables.records.WorkflowStatusRecord;

import static ch.rodano.core.model.jooq.Tables.WORKFLOW_STATUS;

public enum WorkflowStatusSortBy {
	creationTime(WORKFLOW_STATUS.CREATION_TIME),
	lastUpdateTime(WORKFLOW_STATUS.LAST_UPDATE_TIME),
	workflowId(WORKFLOW_STATUS.WORKFLOW_ID),
	stateId(WORKFLOW_STATUS.STATE_ID);

	private final TableField<WorkflowStatusRecord, ?> field;

	WorkflowStatusSortBy(final TableField<WorkflowStatusRecord, ?> field) {
		this.field = field;
	}

	public TableField<WorkflowStatusRecord, ?> getField() {
		return field;
	}
}
