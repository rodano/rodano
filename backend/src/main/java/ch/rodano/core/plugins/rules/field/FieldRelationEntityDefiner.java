package ch.rodano.core.plugins.rules.field;

import java.util.ArrayList;
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
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityRelation;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.plugin.entity.EntityType;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FieldRelationEntityDefiner extends AbstractFieldEntityDefiner {
	private final ScopeService scopeService;
	private final EventService eventService;
	private final DatasetService datasetService;
	private final WorkflowStatusService workflowStatusService;
	private final FormService formService;
	private final FieldService fieldService;

	public FieldRelationEntityDefiner(
		@Lazy final ScopeService scopeService,
		@Lazy final EventService eventService,
		@Lazy final DatasetService datasetService,
		@Lazy final WorkflowStatusService workflowStatusService,
		@Lazy final FormService formService,
		@Lazy final FieldService fieldService
	) {
		this.scopeService = scopeService;
		this.eventService = eventService;
		this.datasetService = datasetService;
		this.workflowStatusService = workflowStatusService;
		this.formService = formService;
		this.fieldService = fieldService;
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
					return RulableEntity.DATASET;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					return Collections.singleton(datasetService.get((Field) evaluable));
				}

				@Override
				public String getId() {
					return "DATASET";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.WORKFLOW;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					return new HashSet<>(workflowStatusService.getAll((Field) evaluable));
				}

				@Override
				public String getId() {
					return "WORKFLOW";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.FORM;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					final var field = (Field) evaluable;
					final var dataset = datasetService.get(field);
					final var event = eventService.get(dataset);
					final var forms = event.isPresent() ? formService.getAllIncludingRemoved(event.get()) : formService.getAllIncludingRemoved(scopeService.get(dataset));
					final var formModels = field.getFieldModel().getFormModels();
					return forms.stream().filter(p -> formModels.contains(p.getFormModel())).collect(Collectors.toCollection(HashSet::new));
				}

				@Override
				public String getId() {
					return "FORM";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.FIELD;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					final var field = (Field) evaluable;
					final var dataset = datasetService.get(field);
					final var scope = scopeService.get(dataset);
					final var event = eventService.get(dataset).get();

					//retrieve events
					final var events = new ArrayList<>(eventService.getAll(scope));
					Collections.reverse(events);

					//search only in events that happened before current event
					var searchEnabled = false;
					for(final var e : events) {
						if(searchEnabled) {
							if(e.getEventModel().getDatasetModels().contains(dataset.getDatasetModel())) {
								final var visitDataset = datasetService.get(e, dataset.getDatasetModel());
								final var visitField = fieldService.get(visitDataset, field.getFieldModel());
								if(!visitField.isBlank()) {
									return Collections.singleton(visitField);
								}
							}
						}

						if(e.equals(event)) {
							searchEnabled = true;
						}
					}

					//return empty set if value has not been found
					return Collections.emptySet();
				}

				@Override
				public String getId() {
					return "LAST_NON_EMPTY_VALUE";
				}
			}
		);
	}

}
