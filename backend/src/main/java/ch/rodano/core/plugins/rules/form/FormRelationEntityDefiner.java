package ch.rodano.core.plugins.rules.form;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityRelation;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.form.FormContentService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.plugin.entity.EntityType;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FormRelationEntityDefiner extends AbstractFormEntityDefiner {

	private final FormContentService formContentService;
	private final ScopeService scopeService;
	private final EventService eventService;
	private final WorkflowStatusService workflowStatusService;

	public FormRelationEntityDefiner(
		@Lazy final FormContentService formContentService,
		@Lazy final ScopeService scopeService,
		@Lazy final EventService eventService,
		@Lazy final WorkflowStatusService workflowStatusService
	) {
		this.formContentService = formContentService;
		this.scopeService = scopeService;
		this.eventService = eventService;
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
					return RulableEntity.EVENT;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					final var event = eventService.get((Form) evaluable);
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
					return RulableEntity.WORKFLOW;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					return new HashSet<>(workflowStatusService.getAll((Form) evaluable));
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
					final Form form = (Form) evaluable;
					final var event = eventService.get(form);
					final var scope = scopeService.get(form);
					final var formContent = formContentService.generateFormContent(scope, event, form);
					return new HashSet<>(formContent.getAllFields());
				}

				@Override
				public String getId() {
					return "FIELD";
				}
			}
		);
	}
}
