package ch.rodano.core.plugins.rules.workflow;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityRelation;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.plugin.entity.EntityType;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WorkflowRelationEntityDefiner extends AbstractWorkflowEntityDefiner {
	private final ScopeService scopeService;
	private final EventService eventService;
	private final FieldService fieldService;
	private final FormService formService;

	public WorkflowRelationEntityDefiner(@Lazy final ScopeService scopeService, @Lazy final EventService eventService, @Lazy final FieldService fieldService, @Lazy final FormService formService) {
		this.scopeService = scopeService;
		this.eventService = eventService;
		this.fieldService = fieldService;
		this.formService = formService;
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
					return Collections.singleton(scopeService.get((WorkflowStatus) evaluable));
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
					return Collections.singleton(eventService.get((WorkflowStatus) evaluable).get());
				}

				@Override
				public String getId() {
					return "EVENT";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.FORM;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					return Collections.singleton(formService.get((WorkflowStatus) evaluable).get());
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
					return Collections.singleton(fieldService.get((WorkflowStatus) evaluable).get());
				}

				@Override
				public String getId() {
					return "FIELD";
				}
			}
		);
	}
}
