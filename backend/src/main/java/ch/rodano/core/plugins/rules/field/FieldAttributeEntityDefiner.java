package ch.rodano.core.plugins.rules.field;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ch.rodano.configuration.model.field.PartialDate;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.field.FieldRecord;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityAttribute;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.plugin.entity.EntityType;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FieldAttributeEntityDefiner extends AbstractFieldEntityDefiner {
	private final FieldDAOService fieldDAOService;
	private final WorkflowStatusService workflowStatusService;

	public FieldAttributeEntityDefiner(@Lazy final FieldDAOService fieldDAOService, @Lazy final WorkflowStatusService workflowStatusService) {
		this.fieldDAOService = fieldDAOService;
		this.workflowStatusService = workflowStatusService;
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
					return ((Field) evaluable).getFieldModelId();
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
					return ((Field) evaluable).getValue();
				}

				@Override
				public OperandType getType() {
					return OperandType.STRING;
				}

				@Override
				public String getId() {
					return "VALUE";
				}
			},
			new EntityAttribute() {
				@Override
				public PartialDate getValue(final Evaluable evaluable) {
					//TODO this must be merged with value date and named "VALUE_OBJECT"
					//not so sure. We need to know the returned type at compilation time
					return (PartialDate) ((Field) evaluable).getObjectValue();
				}

				@Override
				public OperandType getType() {
					return OperandType.DATE;
				}

				@Override
				public String getId() {
					return "VALUE_DATE";
				}
			},
			new EntityAttribute() {
				@Override
				public Number getValue(final Evaluable evaluable) {
					//TODO this must be merged with value date and named "VALUE_OBJECT"
					//not so sure. We need to know the returned type at compilation time
					return (Number) ((Field) evaluable).getObjectValue();
				}

				@Override
				public OperandType getType() {
					return OperandType.NUMBER;
				}

				@Override
				public String getId() {
					return "VALUE_NUMBER";
				}
			},
			new EntityAttribute() {
				@Override
				public PartialDate getValue(final Evaluable evaluable) {
					return PartialDate.of(((Field) evaluable).getLastUpdateTime());
				}

				@Override
				public OperandType getType() {
					return OperandType.DATE;
				}

				@Override
				public String getId() {
					return "MODIFICATION_DATE";
				}
			},
			new EntityAttribute() {
				@Override
				public PartialDate getValue(final Evaluable evaluable) {
					return PartialDate.of(fieldDAOService.getAuditTrailsForProperty((Field) evaluable, Optional.empty(), FieldRecord::getValue).last().getAuditDatetime());
				}

				@Override
				public OperandType getType() {
					return OperandType.DATE;
				}

				@Override
				public String getId() {
					return "NEWEST_AUDIT_TRAIL";
				}
			},
			new EntityAttribute() {
				@Override
				public PartialDate getValue(final Evaluable evaluable) {
					return PartialDate.of(fieldDAOService.getAuditTrailsForProperty((Field) evaluable, Optional.empty(), FieldRecord::getValue).last().getAuditDatetime());
				}

				@Override
				public OperandType getType() {
					return OperandType.DATE;
				}

				@Override
				public String getId() {
					return "OLDEST_AUDIT_TRAIL";
				}
			},
			new EntityAttribute() {
				@Override
				public Boolean getValue(final Evaluable evaluable) {
					return !workflowStatusService.getAll((Field) evaluable).isEmpty();
				}

				@Override
				public OperandType getType() {
					return OperandType.BOOLEAN;
				}

				@Override
				public String getId() {
					return "HAS_WORKFLOW";
				}
			},
			new EntityAttribute() {
				@Override
				public Boolean getValue(final Evaluable evaluable) {
					return ((Field) evaluable).getFieldModel().isPlugin();
				}

				@Override
				public OperandType getType() {
					return OperandType.BOOLEAN;
				}

				@Override
				public String getId() {
					return "IS_DYNAMIC";
				}
			}
		);
	}
}
