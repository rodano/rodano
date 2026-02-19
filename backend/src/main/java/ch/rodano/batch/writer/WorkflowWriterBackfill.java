package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Workflow;

import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowActionId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowStateId;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;

public class WorkflowWriterBackfill extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowWriterBackfill.class);

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<Workflow> wrapped = (ProjectScoped<Workflow>) raw;

				final UUID projectId = wrapped.getProjectId();
				final Workflow workflow = wrapped.getPayload();

				if(workflow == null || workflow.getId() == null || workflow.getId().isBlank()) {
					LOGGER.warn("Skipping workflow with blank id");
					continue;
				}

				final UUID workflowId = resolveWorkflowId(tx, projectId, workflow.getId());

				final UUID aggregatedWorkflowId = resolveWorkflowId(tx, projectId, workflow.getAggregateWorkflowId());
				final UUID resolvedStateId = resolveWorkflowStateId(tx, projectId, workflowId, workflow.getInitialStateId());
				final UUID resolvedActionId = resolveWorkflowActionId(tx, projectId, workflowId, workflow.getActionId());

				tx.update(WORKFLOW)
					.set(WORKFLOW.AGGREGATE_WORKFLOW_ID, aggregatedWorkflowId)
					.set(WORKFLOW.INITIAL_STATE_ID, resolvedStateId)
					.set(WORKFLOW.CREATION_ACTION_ID, resolvedActionId)
					.where(WORKFLOW.PROJECT_ID.eq(projectId))
					.and(WORKFLOW.WORKFLOW_ID.eq(workflowId))
					.execute();
			}
		});
	}
}
