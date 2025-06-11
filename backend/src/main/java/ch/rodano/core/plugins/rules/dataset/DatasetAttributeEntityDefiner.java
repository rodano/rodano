package ch.rodano.core.plugins.rules.dataset;

import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ch.rodano.configuration.model.field.PartialDate;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityAttribute;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.services.plugin.entity.EntityType;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatasetAttributeEntityDefiner extends AbstractDatasetEntityDefiner {
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
				public Long getValue(final Evaluable evaluable) {
					return ((Dataset) evaluable).getPk();
				}

				@Override
				public OperandType getType() {
					return OperandType.NUMBER;
				}

				@Override
				public String getId() {
					return "PK";
				}
			},
			new EntityAttribute() {
				@Override
				public String getValue(final Evaluable evaluable) {
					return ((Dataset) evaluable).getDatasetModelId();
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
			// FIXME rename to creation time and modify it in the config file later on when several attributes will have been renamed
			new EntityAttribute() {
				@Override
				public PartialDate getValue(final Evaluable evaluable) {
					return PartialDate.of(((Dataset) evaluable).getCreationTime());
				}

				@Override
				public OperandType getType() {
					return OperandType.DATE;
				}

				@Override
				public String getId() {
					return "CREATION_DATE";
				}
			},
			new EntityAttribute() {
				@Override
				public Boolean getValue(final Evaluable evaluable) {
					return ((Dataset) evaluable).getDeleted();
				}

				@Override
				public OperandType getType() {
					return OperandType.BOOLEAN;
				}

				@Override
				public String getId() {
					return "REMOVED";
				}
			},
			new EntityAttribute() {
				@Override
				public Boolean getValue(final Evaluable evaluable) {
					return ((Dataset) evaluable).getEventFk() == null;
				}

				@Override
				public OperandType getType() {
					return OperandType.BOOLEAN;
				}

				@Override
				public String getId() {
					return "IS_ATTACHED_TO_SCOPE";
				}
			}
		);
	}
}
