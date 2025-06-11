package ch.rodano.core.plugins.rules.visit;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ch.rodano.configuration.exceptions.NoRespectForConfigurationException;
import ch.rodano.configuration.model.field.PartialDate;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityAction;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.user.UserService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.dataset.DatasetDAOService;
import ch.rodano.core.services.dao.event.EventDAOService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.dao.form.FormDAOService;
import ch.rodano.core.services.dao.role.RoleDAOService;
import ch.rodano.core.services.plugin.entity.EntityType;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EventActionEntityDefiner extends AbstractEventEntityDefiner {
	protected final StudyService studyService;
	protected final RoleDAOService roleDAOService;
	protected final DatasetDAOService datasetDAOService;
	protected final WorkflowStatusService workflowStatusService;
	protected final FormDAOService formDAOService;
	protected final EventDAOService eventDAOService;
	protected final FormService formService;
	protected final EventService eventService;
	protected final DatasetService datasetService;
	protected final FieldDAOService fieldDAOService;
	protected final FieldService fieldService;
	protected final ScopeService scopeService;
	protected final UserService userService;
	protected final ActorService actorService;

	public EventActionEntityDefiner(
		final StudyService studyService,
		final RoleDAOService roleDAOService,
		final DatasetDAOService datasetDAOService,
		@Lazy final WorkflowStatusService workflowStatusService,
		final FormDAOService formDAOService,
		final EventDAOService eventDAOService,
		@Lazy final FormService formService,
		@Lazy final EventService eventService,
		@Lazy final DatasetService datasetService,
		final FieldDAOService fieldDAOService,
		@Lazy final FieldService fieldService,
		@Lazy final ScopeService scopeService,
		@Lazy final UserService userService,
		@Lazy final ActorService actorService
	) {
		this.studyService = studyService;
		this.roleDAOService = roleDAOService;
		this.datasetDAOService = datasetDAOService;
		this.workflowStatusService = workflowStatusService;
		this.formDAOService = formDAOService;
		this.eventDAOService = eventDAOService;
		this.formService = formService;
		this.eventService = eventService;
		this.datasetService = datasetService;
		this.fieldDAOService = fieldDAOService;
		this.fieldService = fieldService;
		this.scopeService = scopeService;
		this.userService = userService;
		this.actorService = actorService;
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
					final var event = (Event) evaluable;
					final var blocking = Boolean.parseBoolean((String) parameters.get("BLOCKING"));
					event.setBlocking(blocking);
					eventService.save(event, context, "Set blocking");
				}

				@Override
				public String getId() {
					return "SET_BLOCKING";
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
					final var event = (Event) evaluable;
					final var locked = Boolean.parseBoolean((String) parameters.get("LOCKED"));
					event.setLocked(locked);
					eventService.save(event, context, "Set locked");
				}

				@Override
				public String getId() {
					return "SET_LOCKED";
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
					final var eventDate = (PartialDate) parameters.get("DATE");
					if(eventDate.isAnchoredInTime()) {
						final Event event = (Event) evaluable;
						if(event.getEventModel().isPlanned()) {
							throw new NoRespectForConfigurationException("Can not set expected date for a event that is planned");
						}
						event.setExpectedDate(eventDate.toZonedDateTime().get());
						eventService.save(event, context, "Set expected date");
					}
				}

				@Override
				public String getId() {
					return "SET_EXPECTED_DATE";
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
					final var event = (Event) evaluable;
					final var scope = scopeService.get(event);
					final var eventDate = (PartialDate) parameters.get("DATE");
					if(eventDate.isAnchoredInTime()) {
						eventService.updateDate(scope, event, eventDate.toZonedDateTime().get(), context, "Set date");
					}
				}

				@Override
				public String getId() {
					return "SET_DATE";
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
					final var eventDate = (PartialDate) parameters.get("DATE");
					if(eventDate.isAnchoredInTime()) {
						final Event event = (Event) evaluable;
						event.setEndDate(eventDate.toZonedDateTime().get());
						eventService.save(event, context, "Set end date");
					}
				}

				@Override
				public String getId() {
					return "SET_END_DATE";
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
					final var event = (Event) evaluable;
					final var currentEventDate = event.getDate();
					final var eventTime = (PartialDate) parameters.get("TIME");

					final var eventHour = eventTime.getHour().get();
					final var eventMinute = eventTime.getMinute().get();

					final var newEventDateBase = currentEventDate.truncatedTo(ChronoUnit.DAYS);
					final var newEventDate = newEventDateBase.plusHours(eventHour).plusMinutes(eventMinute);

					event.setDate(newEventDate);

					eventDAOService.saveEvent(event, context, "Set time");
				}

				@Override
				public String getId() {
					return "SET_TIME";
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
					final var event = (Event) evaluable;
					final var currentEventEndDate = event.getEndDate();
					final var eventEndTime = (PartialDate) parameters.get("TIME");

					final var eventEndHour = eventEndTime.getHour().get();
					final var eventEndMinute = eventEndTime.getMinute().get();

					final var newEventEndDateBase = currentEventEndDate.truncatedTo(ChronoUnit.DAYS);
					final var newEventEndDate = newEventEndDateBase.plusHours(eventEndHour).plusMinutes(eventEndMinute);

					event.setEndDate(newEventEndDate);

					eventDAOService.saveEvent(event, context, "Set end time");
				}

				@Override
				public String getId() {
					return "SET_END_TIME";
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
					final var event = (Event) evaluable;
					event.setNotDone(true);
					eventService.save(event, context, "Set not done");
				}

				@Override
				public String getId() {
					return "SET_NOT_DONE";
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
					final var event = (Event) evaluable;
					event.setNotDone(false);
					eventService.save(event, context, "Set done");
				}

				@Override
				public String getId() {
					return "SET_DONE";
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
					final var formModel = studyService.getStudy().getFormModel((String) parameters.get("FORM_MODEL_ID"));
					final var rationale = (String) parameters.get("RATIONALE");
					final var event = (Event) evaluable;
					final var scope = scopeService.get(event);
					//rule can execute only if the form model is allowed for event model
					if(event.getEventModel().getFormModelIds().contains(formModel.getId())) {
						final var forms = formService.getAllIncludingRemoved(event);
						final var form = forms.stream().filter(f -> f.getFormModelId().equals(formModel.getId())).findFirst();
						//create form if it does not exist
						if(form.isEmpty()) {
							formService.create(scope, event, formModel, context, StringUtils.defaultIfBlank(rationale, "Create form"));
						}
						//restore form otherwise
						else if(form.get().getDeleted()) {
							formService.restore(scope, Optional.of(event), form.get(), context, StringUtils.defaultIfBlank(rationale, "Restore form"));
						}
						//otherwise, nothing to do
					}
				}

				@Override
				public String getId() {
					return "ADD_FORM";
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
					final var event = (Event) evaluable;
					final var scope = scopeService.get(event);
					eventService.resetDates(scope, context, "Reset next events' dates");
				}

				@Override
				public String getId() {
					return "RESET_NEXT_EVENT_DATES";
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
					final var event = (Event) evaluable;
					final var scope = scopeService.get(event);
					final var rationale = (String) parameters.get("RATIONALE");
					eventService.delete(scope, event, context, StringUtils.defaultIfBlank(rationale, "Delete event"));
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
					final var event = (Event) evaluable;
					final var scope = scopeService.get(event);
					final var rationale = (String) parameters.get("RATIONALE");
					eventService.restore(scope, event, context, StringUtils.defaultIfBlank(rationale, "Restore event"));
				}

				@Override
				public String getId() {
					return "RESTORE";
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
					final var event = (Event) evaluable;
					final var workflow = studyService.getStudy().getWorkflow((String) parameters.get("WORKFLOW"));

					if(event.getEventModel().getWorkflowIds().contains(workflow.getId())) {
						final var scope = scopeService.get(event);
						final var family = new DataFamily(scope, event);
						workflowStatusService.create(family, event, workflow, data, context, "Initialize workflow");
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
					final var event = (Event) evaluable;
					final var workflow = studyService.getStudy().getWorkflow((String) parameters.get("WORKFLOW"));

					final var workflowStatuses = workflowStatusService.getAll(event, workflow);
					if(!workflowStatuses.isEmpty()) {
						final var scope = scopeService.get(event);
						final var family = new DataFamily(scope, event);
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
					final var event = (Event) evaluable;
					final var scope = scopeService.get(event);
					eventService.validate(scope, event, context, "Validate event");
				}

				@Override
				public String getId() {
					return "VALIDATE";
				}
			}
		);
	}
}
