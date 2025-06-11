package ch.rodano.core.services.bll.export.views;

import java.util.Arrays;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import ch.rodano.core.services.bll.workflowStatus.AggregateWorkflowDAOService;

import static ch.rodano.core.model.jooq.Tables.WORKFLOW_STATUS;

/**
 * Generate an SQL view containing all aggregated workflow statuses. It is not used in the code but it is very useful for debugging purposes
 */
@Profile({ "!migration & !database" })
@Service
public class AggregateWorkflowViewService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final DSLContext create;
	private final AggregateWorkflowDAOService aggregateWorkflowDAOService;

	public AggregateWorkflowViewService(
		final DSLContext create,
		final AggregateWorkflowDAOService aggregateWorkflowDAOService
	) {
		this.create = create;
		this.aggregateWorkflowDAOService = aggregateWorkflowDAOService;
	}

	public void updateView() {
		logger.info("Updating workflow status aggregate view");
		final var scopeQuery = aggregateWorkflowDAOService.generateScopeQuery(Optional.empty(), Optional.empty());
		final var eventQuery = aggregateWorkflowDAOService.generateEventQuery(Optional.empty(), Optional.empty());
		//the fields of the view are the same as the fields of the workflow status table
		final var fields = Arrays.asList(WORKFLOW_STATUS.fields()).stream().map(Field<?>::getName).toList().toArray(new String[0]);
		create.createOrReplaceView("workflow_status_aggregate", fields).as(scopeQuery.union(eventQuery)).execute();
	}
}
