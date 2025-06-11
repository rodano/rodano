package ch.rodano.core.plugins.rules.dataset;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityRelation;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.plugin.entity.EntityType;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatasetRelationEntityDefiner extends AbstractDatasetEntityDefiner {

	private final ScopeService scopeService;
	private final EventService eventService;
	private final FieldService fieldService;

	public DatasetRelationEntityDefiner(@Lazy final ScopeService scopeService, @Lazy final EventService eventService, @Lazy final FieldService fieldService) {
		this.scopeService = scopeService;
		this.eventService = eventService;
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
					return RulableEntity.SCOPE;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					final var dataset = (Dataset) evaluable;
					return Collections.singleton(scopeService.get(dataset));
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
					final var dataset = (Dataset) evaluable;
					final var event = eventService.get(dataset);
					return event.<Set<Evaluable>> map(Collections::singleton).orElse(Collections.emptySet());
				}

				@Override
				public String getId() {
					return "EVENT";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.FIELD;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					return new HashSet<>(fieldService.getAll((Dataset) evaluable));
				}

				@Override
				public String getId() {
					return "FIELD";
				}
			}
		);
	}

}
