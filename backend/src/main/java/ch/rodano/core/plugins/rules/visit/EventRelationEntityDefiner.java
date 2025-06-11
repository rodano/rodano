package ch.rodano.core.plugins.rules.visit;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityRelation;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.plugin.entity.EntityType;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EventRelationEntityDefiner extends AbstractEventEntityDefiner {
	private final FieldDAOService fieldDAOService;
	private final ScopeService scopeService;
	private final EventService eventService;
	private final DatasetService datasetService;
	private final FormService formService;
	private final WorkflowStatusService workflowStatusService;

	public EventRelationEntityDefiner(
		final FieldDAOService fieldDAOService,
		@Lazy final ScopeService scopeService,
		@Lazy final EventService eventService,
		@Lazy final DatasetService datasetService,
		@Lazy final FormService formService,
		@Lazy final WorkflowStatusService workflowStatusService
	) {
		this.fieldDAOService = fieldDAOService;
		this.scopeService = scopeService;
		this.eventService = eventService;
		this.datasetService = datasetService;
		this.formService = formService;
		this.workflowStatusService = workflowStatusService;
	}

	/**
	 * Get the related entity type
	 *
	 * @return An entity type
	 */
	@Override
	public EntityType getRelatedEntityType() {
		return EntityType.RELATION;
	}

	/**
	 * Get all registered identifiable entity beans
	 *
	 * @return Registered identifiable entity beans
	 */
	@Override
	public List<IdentifiableEntity> getRegisteredBeans() {
		return List.of(
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.SCOPE;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					return Collections.singleton(scopeService.get((Event) evaluable));
				}

				@Override
				public String getId() {
					return "SCOPE";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.EVENT;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					return eventService.getPrevious((Event) evaluable).stream().collect(Collectors.toSet());
				}

				@Override
				public String getId() {
					return "PREVIOUS";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.EVENT;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					return eventService.getNext((Event) evaluable).stream().collect(Collectors.toSet());
				}

				@Override
				public String getId() {
					return "NEXT";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.EVENT;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					final var event = (Event) evaluable;
					final var scope = scopeService.get(event);
					final var events = eventService.getAll(scope);

					return events.stream().filter(e -> !e.getDateOrExpectedDate().isAfter(event.getDateOrExpectedDate())).collect(Collectors.toSet());
				}

				@Override
				public String getId() {
					return "ALL_PREVIOUS";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.EVENT;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					final var event = (Event) evaluable;
					final var scope = scopeService.get(event);
					final var events = eventService.getAll(scope);

					return events.stream().filter(e -> e.getDateOrExpectedDate().isAfter(event.getDateOrExpectedDate())).collect(Collectors.toSet());
				}

				@Override
				public String getId() {
					return "ALL_NEXT";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.EVENT;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					final var event = (Event) evaluable;
					final var scope = scopeService.get(event);
					final var events = eventService.getAllIncludingRemoved(scope);

					return events.stream().filter(e -> e.getDateOrExpectedDate().isAfter(event.getDateOrExpectedDate())).collect(Collectors.toSet());
				}

				@Override
				public String getId() {
					return "ALL_NEXT_INCLUDING_REMOVED";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.DATASET;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					return new HashSet<>(datasetService.getAllIncludingRemoved((Event) evaluable));
				}

				@Override
				public String getId() {
					return "DATASET";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.FORM;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					return new HashSet<>(formService.getAllIncludingRemoved((Event) evaluable));
				}

				@Override
				public String getId() {
					return "FORM";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.WORKFLOW;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					return new HashSet<>(workflowStatusService.getAll((Event) evaluable));
				}

				@Override
				public String getId() {
					return "WORKFLOW";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.FIELD;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					final var event = (Event) evaluable;
					return new HashSet<>(fieldDAOService.getFieldsFromEventWithAValue(event.getPk()));
				}

				@Override
				public String getId() {
					return "FIELD_HAVING_VALUE";
				}
			}
		);
	}
}
