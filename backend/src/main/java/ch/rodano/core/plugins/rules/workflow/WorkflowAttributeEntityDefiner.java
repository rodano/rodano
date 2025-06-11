package ch.rodano.core.plugins.rules.workflow;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ch.rodano.configuration.model.field.PartialDate;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityAttribute;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.dao.workflow.WorkflowStatusDAOService;
import ch.rodano.core.services.plugin.entity.EntityType;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WorkflowAttributeEntityDefiner extends AbstractWorkflowEntityDefiner {
	private final WorkflowStatusDAOService workflowStatusDAOService;

	public WorkflowAttributeEntityDefiner(@Lazy final WorkflowStatusDAOService workflowStatusDAOService) {
		this.workflowStatusDAOService = workflowStatusDAOService;
	}

	/**
	 * Get the related entity type
	 *
	 * @return An entity type
	 */
	@Override
	public EntityType getRelatedEntityType() {
		return EntityType.ATTRIBUTE;
	}

	/**
	 * Get all registered identifiable entity beans
	 *
	 * @return Registered identifiable entity beans
	 */
	@Override
	public List<IdentifiableEntity> getRegisteredBeans() {
		return List.of(
			new EntityAttribute() {
				@Override
				public String getValue(final Evaluable evaluable) {
					return ((WorkflowStatus) evaluable).getWorkflowId();
				}

				@Override
				public OperandType getType() {
					return OperandType.STRING;
				}

				@Override
				public String getId() {
					return "ID";
				}
			},
			new EntityAttribute() {
				@Override
				public String getValue(final Evaluable evaluable) {
					return ((WorkflowStatus) evaluable).getStateId();
				}

				@Override
				public OperandType getType() {
					return OperandType.STRING;
				}

				@Override
				public String getId() {
					return "STATUS";
				}
			},
			new EntityAttribute() {
				@Override
				public String getValue(final Evaluable evaluable) {
					return ((WorkflowStatus) evaluable).getActionId();
				}

				@Override
				public OperandType getType() {
					return OperandType.STRING;
				}

				@Override
				public String getId() {
					return "CREATION_ACTION";
				}
			},
			new EntityAttribute() {
				@Override
				public String getValue(final Evaluable evaluable) {
					return ((WorkflowStatus) evaluable).getValidatorId();
				}

				@Override
				public OperandType getType() {
					return OperandType.STRING;
				}

				@Override
				public String getId() {
					return "VALIDATOR_ID";
				}
			},
			new EntityAttribute() {
				@Override
				public PartialDate getValue(final Evaluable evaluable) {
					final var workflowTrails = workflowStatusDAOService.getAuditTrails((WorkflowStatus) evaluable, Optional.empty(), Optional.empty());
					if(workflowTrails.size() > 2) {
						return PartialDate.of(workflowTrails.last().getAuditDatetime());
					}
					return PartialDate.now();
				}

				@Override
				public OperandType getType() {
					return OperandType.DATE;
				}

				@Override
				public String getId() {
					return "DATE_OF_FIRST_STATUS_AFTER_INITIALIZATION";
				}
			},
			new EntityAttribute() {
				@Override
				public Boolean getValue(final Evaluable evaluable) {
					final var workflowTrails = workflowStatusDAOService.getAuditTrails((WorkflowStatus) evaluable, Optional.empty(), Optional.empty());
					if(!workflowTrails.isEmpty()) {
						return StringUtils.isNotBlank(workflowTrails.first().getAuditContext());
					}
					return false;
				}

				@Override
				public OperandType getType() {
					return OperandType.BOOLEAN;
				}

				@Override
				public String getId() {
					return "COMMENT_ON_LAST_AUDIT_TRAIL";
				}
			}
		);
	}
}
