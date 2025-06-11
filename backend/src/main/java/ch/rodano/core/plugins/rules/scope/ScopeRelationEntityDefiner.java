package ch.rodano.core.plugins.rules.scope;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
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
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.plugin.entity.EntityType;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ScopeRelationEntityDefiner extends AbstractScopeEntityDefiner {
	protected final StudyService studyService;
	protected final ScopeService scopeService;
	protected final ScopeRelationService scopeRelationService;
	protected final EventService eventService;
	protected final FormService formService;
	protected final DatasetService datasetService;
	protected final FieldDAOService fieldDAOService;
	protected final WorkflowStatusService workflowStatusService;

	public ScopeRelationEntityDefiner(
		final StudyService studyService,
		@Lazy final ScopeService scopeService,
		@Lazy final ScopeRelationService scopeRelationService,
		@Lazy final EventService eventService,
		@Lazy final FormService formService,
		@Lazy final DatasetService datasetService,
		final FieldDAOService fieldDAOService,
		@Lazy final WorkflowStatusService workflowStatusService
	) {
		this.studyService = studyService;
		this.scopeService = scopeService;
		this.scopeRelationService = scopeRelationService;
		this.eventService = eventService;
		this.formService = formService;
		this.datasetService = datasetService;
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
					return new HashSet<>(scopeRelationService.getEnabledAncestors((Scope) evaluable));
				}

				@Override
				public String getId() {
					return "ANCESTOR";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.SCOPE;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					final var scope = (Scope) evaluable;
					return new HashSet<>(scopeRelationService.getEnabledParents(scope, ZonedDateTime.now()));
				}

				@Override
				public String getId() {
					return "PARENT";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.SCOPE;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					final var scope = (Scope) evaluable;
					return Collections.singleton(scopeRelationService.getDefaultParent(scope));
				}

				@Override
				public String getId() {
					return "DEFAULT_PARENT";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.SCOPE;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					return new HashSet<>(scopeRelationService.getEnabledDescendants((Scope) evaluable));
				}

				@Override
				public String getId() {
					return "DESCENDANT";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.SCOPE;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					return new HashSet<>(scopeRelationService.getEnabledDescendants((Scope) evaluable, studyService.getStudy().getLeafScopeModel()));
				}

				@Override
				public String getId() {
					return "LEAF";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.WORKFLOW;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					return new HashSet<>(workflowStatusService.getAll((Scope) evaluable));
				}

				@Override
				public String getId() {
					return "WORKFLOW";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.EVENT;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					return new HashSet<>(eventService.getAllIncludingRemoved((Scope) evaluable));
				}

				@Override
				public String getId() {
					return "EVENT";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.EVENT;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					final Scope scope = (Scope) evaluable;
					//it's not required for scope models to have an inceptive event
					if(scope.getScopeModel().getInceptiveEventModels().isEmpty()) {
						return Collections.emptySet();
					}
					return Collections.singleton(eventService.getInceptive((Scope) evaluable));

				}

				@Override
				public String getId() {
					return "INCEPTIVE_VISIT";
				}
			},
			new EntityRelation() {
				@Override
				public RulableEntity getTargetEntity() {
					return RulableEntity.DATASET;
				}

				@Override
				public Set<Evaluable> getTargetEvaluables(final Evaluable evaluable) {
					return new HashSet<>(datasetService.getAllIncludingRemoved((Scope) evaluable));
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
					return new HashSet<>(formService.getAllIncludingRemoved((Scope) evaluable));
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
					final Scope scope = (Scope) evaluable;
					return new HashSet<>(fieldDAOService.getFieldsFromScopeWithAValue(scope.getPk()));
				}

				@Override
				public String getId() {
					return "FIELD_HAVING_VALUE";
				}
			}
		);
	}
}
