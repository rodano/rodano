package ch.rodano.core.plugins.rules.visit;

import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ch.rodano.configuration.model.field.PartialDate;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityAttribute;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.plugin.entity.EntityType;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EventAttributeEntityDefiner extends AbstractEventEntityDefiner {
	protected final FieldDAOService fieldDAOService;
	protected final ScopeService scopeService;
	protected final DatasetService datasetService;
	protected final WorkflowStatusService workflowStatusService;
	protected final FieldService fieldService;
	protected final StudyService studyService;

	public EventAttributeEntityDefiner(
		final FieldDAOService fieldDAOService,
		@Lazy final ScopeService scopeService,
		@Lazy final DatasetService datasetService,
		@Lazy final WorkflowStatusService workflowStatusService,
		@Lazy final FieldService fieldService,
		final StudyService studyService
	) {
		this.fieldDAOService = fieldDAOService;
		this.scopeService = scopeService;
		this.datasetService = datasetService;
		this.workflowStatusService = workflowStatusService;
		this.fieldService = fieldService;
		this.studyService = studyService;
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
					return ((Event) evaluable).getEventModelId();
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
					return ((Event) evaluable).getEventModel().getEventGroupId();
				}

				@Override
				public OperandType getType() {
					return OperandType.STRING;
				}

				@Override
				public String getId() {
					return "EVENT_GROUP_ID";
				}
			},
			new EntityAttribute() {
				@Override
				public PartialDate getValue(final Evaluable evaluable) {
					return PartialDate.of(((Event) evaluable).getExpectedDate());
				}

				@Override
				public OperandType getType() {
					return OperandType.DATE;
				}

				@Override
				public String getId() {
					return "EXPECTED_DATE";
				}
			},
			new EntityAttribute() {
				@Override
				public PartialDate getValue(final Evaluable evaluable) {
					return PartialDate.ofNullable(((Event) evaluable).getDate());
				}

				@Override
				public OperandType getType() {
					return OperandType.DATE;
				}

				@Override
				public String getId() {
					return "DATE";
				}
			},
			new EntityAttribute() {
				@Override
				public PartialDate getValue(final Evaluable evaluable) {
					return PartialDate.ofNullable(((Event) evaluable).getDateOrExpectedDate());
				}

				@Override
				public OperandType getType() {
					return OperandType.DATE;
				}

				@Override
				public String getId() {
					return "DATE_OR_EXPECTED_DATE";
				}
			},
			new EntityAttribute() {
				// FIXME rename to creation time and modify it in the config file later on when several attributes will have been renamed
				@Override
				public PartialDate getValue(final Evaluable evaluable) {
					return PartialDate.of(((Event) evaluable).getCreationTime());
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
					return ((Event) evaluable).isExpected();
				}

				@Override
				public OperandType getType() {
					return OperandType.BOOLEAN;
				}

				@Override
				public String getId() {
					return "EXPECTED";
				}
			},
			new EntityAttribute() {
				@Override
				public Boolean getValue(final Evaluable evaluable) {
					return ((Event) evaluable).getBlocking();
				}

				@Override
				public OperandType getType() {
					return OperandType.BOOLEAN;
				}

				@Override
				public String getId() {
					return "BLOCKED";
				}
			},
			new EntityAttribute() {
				@Override
				public Boolean getValue(final Evaluable evaluable) {
					return ((Event) evaluable).getNotDone();
				}

				@Override
				public OperandType getType() {
					return OperandType.BOOLEAN;
				}

				@Override
				public String getId() {
					return "NOT_DONE";
				}
			},
			new EntityAttribute() {
				@Override
				public Boolean getValue(final Evaluable evaluable) {
					return ((Event) evaluable).getDeleted();
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
					final var event = (Event) evaluable;
					return fieldDAOService.doesEventHaveFieldsWithAValue(event.getPk());
				}

				@Override
				public OperandType getType() {
					return OperandType.BOOLEAN;
				}

				@Override
				public String getId() {
					return "CONTAINS_DATA";
				}
			},
			new EntityAttribute() {
				@Override
				public Boolean getValue(final Evaluable evaluable) {
					return ((Event) evaluable).isInSchedule();
				}

				@Override
				public OperandType getType() {
					return OperandType.BOOLEAN;
				}

				@Override
				public String getId() {
					return "EVENT_RESPECT_INTERVAL";
				}
			},
			new EntityAttribute() {
				@Override
				public Integer getValue(final Evaluable evaluable) {
					return ((Event) evaluable).getEventGroupNumber();
				}

				@Override
				public OperandType getType() {
					return OperandType.NUMBER;
				}

				@Override
				public String getId() {
					return "EVENT_GROUP_NUMBER";
				}
			}
		);
	}
}
