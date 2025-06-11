package ch.rodano.core.plugins.rules.form;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityAction;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.plugin.entity.EntityType;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FormActionEntityDefiner extends AbstractFormEntityDefiner {
	private final StudyService studyService;
	private final WorkflowStatusService workflowStatusService;
	private final ScopeService scopeService;
	private final EventService eventService;
	private final FormService formService;

	public FormActionEntityDefiner(
		@Lazy final StudyService studyService,
		@Lazy final WorkflowStatusService workflowStatusService,
		@Lazy final ScopeService scopeService,
		@Lazy final EventService eventService,
		@Lazy final FormService formService
	) {
		this.studyService = studyService;
		this.workflowStatusService = workflowStatusService;
		this.scopeService = scopeService;
		this.eventService = eventService;
		this.formService = formService;
	}

	@Override
	public EntityType getRelatedEntityType() {
		return EntityType.ACTION;
	}

	@Override
	public List<IdentifiableEntity> getRegisteredBeans() {
		return List.of(
			new EntityAction() {
				@Override
				public void action(
					final Evaluable evaluable,
					final Map<String, Object> parameters,
					final DatabaseActionContext context,
					final String message,
					final Map<String, Object> data
				) {
					final var form = (Form) evaluable;
					final var workflow = studyService.getStudy().getWorkflow((String) parameters.get("WORKFLOW"));

					if(form.getFormModel().getWorkflowIds().contains(workflow.getId())) {
						final var scope = scopeService.get(form);
						final var event = eventService.get(form);
						final var family = new DataFamily(scope, event, form);
						workflowStatusService.create(family, form, workflow, data, context, "Initialize from rule");
					}
				}

				@Override
				public String getId() {
					return "INITIALIZE_WORKFLOW";
				}
			},
			new EntityAction() {
				@Override
				public void action(
					final Evaluable evaluable,
					final Map<String, Object> parameters,
					final DatabaseActionContext context,
					final String message,
					final Map<String, Object> data
				) {
					final var form = (Form) evaluable;
					final var workflow = studyService.getStudy().getWorkflow((String) parameters.get("WORKFLOW"));

					final var workflowStatuses = workflowStatusService.getAll(form, workflow);
					if(!workflowStatuses.isEmpty()) {
						final var scope = scopeService.get(form);
						final var event = eventService.get(form);
						final var family = new DataFamily(scope, event, form);
						for(final var workflowStatus : workflowStatuses) {
							workflowStatusService.delete(family, workflowStatus, context, "Delete workflow");
						}
					}
				}

				@Override
				public String getId() {
					return "DELETE_WORKFLOW";
				}
			},
			new EntityAction() {
				@Override
				public void action(
					final Evaluable evaluable,
					final Map<String, Object> parameters,
					final DatabaseActionContext context,
					final String message,
					final Map<String, Object> data
				) {
					final var form = (Form) evaluable;
					final var event = eventService.get(form);
					final var scope = scopeService.get(form);
					final var rationale = (String) parameters.get("RATIONALE");
					formService.delete(scope, event, form, context, StringUtils.defaultIfBlank(rationale, "Delete form"));
				}

				@Override
				public String getId() {
					return "REMOVE";
				}
			},
			new EntityAction() {
				@Override
				public void action(
					final Evaluable evaluable,
					final Map<String, Object> parameters,
					final DatabaseActionContext context,
					final String message,
					final Map<String, Object> data
				) {
					final var form = (Form) evaluable;
					final var event = eventService.get(form);
					final var scope = scopeService.get(form);
					final var rationale = (String) parameters.get("RATIONALE");
					formService.restore(scope, event, form, context, StringUtils.defaultIfBlank(rationale, "Restore form"));
				}

				@Override
				public String getId() {
					return "RESTORE";
				}
			}
		);
	}
}
