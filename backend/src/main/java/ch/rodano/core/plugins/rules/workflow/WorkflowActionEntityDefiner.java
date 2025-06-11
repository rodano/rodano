package ch.rodano.core.plugins.rules.workflow;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityAction;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.plugin.entity.EntityType;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WorkflowActionEntityDefiner extends AbstractWorkflowEntityDefiner {
	private final WorkflowStatusService workflowStatusService;

	public WorkflowActionEntityDefiner(@Lazy final WorkflowStatusService workflowStatusService) {
		this.workflowStatusService = workflowStatusService;
	}

	@Override
	public EntityType getRelatedEntityType() {
		return EntityType.ACTION;
	}

	@Override
	public List<IdentifiableEntity> getRegisteredBeans() {
		return List.of(
			new EntityAction() {
				@Override
				public void action(
					final Evaluable evaluable,
					final Map<String, Object> parameters,
					final DatabaseActionContext context,
					final String message,
					final Map<String, Object> data
				) {
					final var workflowStatus = (WorkflowStatus) evaluable;
					final var state = workflowStatus.getWorkflow().getState((String) parameters.get("STATUS"));
					final var workflowMessage = (String) parameters.getOrDefault("MESSAGE", message);

					final var family = workflowStatusService.createDataFamily(workflowStatus);
					workflowStatusService.updateState(family, workflowStatus, state, data, context, StringUtils.defaultIfBlank(workflowMessage, "Change workflow status"));
				}

				@Override
				public String getId() {
					return "CHANGE_STATUS";
				}
			}
		);
	}
}
