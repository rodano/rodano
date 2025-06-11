package ch.rodano.core.plugins.rules.field;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityAction;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.plugin.entity.EntityType;
import ch.rodano.core.services.plugin.validator.exception.BadlyFormattedValue;
import ch.rodano.core.services.plugin.validator.exception.InvalidValueException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FieldActionEntityDefiner extends AbstractFieldEntityDefiner {
	private final StudyService studyService;
	private final ScopeService scopeService;
	private final EventService eventService;
	private final DatasetService datasetService;
	private final WorkflowStatusService workflowStatusService;
	private final FieldService fieldService;

	public FieldActionEntityDefiner(
		@Lazy final StudyService studyService,
		@Lazy final ScopeService scopeService,
		@Lazy final EventService eventService,
		@Lazy final DatasetService datasetService,
		@Lazy final WorkflowStatusService workflowStatusService,
		@Lazy final FieldService fieldService
	) {
		this.studyService = studyService;
		this.scopeService = scopeService;
		this.eventService = eventService;
		this.datasetService = datasetService;
		this.workflowStatusService = workflowStatusService;
		this.fieldService = fieldService;
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
				) throws InvalidValueException, BadlyFormattedValue {
					final var field = (Field) evaluable;
					final var dataset = datasetService.get(field);
					final var scope = scopeService.get(dataset);
					final var event = eventService.get(dataset);

					final var value = parameters.get("VALUE");
					final var fallbackMessage = StringUtils.defaultIfBlank(message, "Value set by system");

					//if value is null, FieldService::updateValue cannot be used
					if(value == null) {
						fieldService.reset(scope, event, dataset, field, context, fallbackMessage);
					}
					else {
						//the parameter must be a string
						final var stringValue = ((String) value).trim();

						//update field: this method has no effect on unchanged values
						fieldService.updateValue(scope, event, dataset, field, stringValue, context, fallbackMessage);
					}
				}

				@Override
				public String getId() {
					return "SET_STRING_VALUE";
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
				) throws InvalidValueException, BadlyFormattedValue {
					final var field = (Field) evaluable;
					final var dataset = datasetService.get(field);
					final var scope = scopeService.get(dataset);
					final var event = eventService.get(dataset);

					final var value = parameters.get("VALUE");
					final var fallbackMessage = StringUtils.defaultIfBlank(message, "Value set by system");

					//if value is null, FieldService::updateValue cannot be used
					if(value == null) {
						fieldService.reset(scope, event, dataset, field, context, fallbackMessage);
					}
					else {
						final var stringValue = field.getFieldModel().objectToString(value);

						//update field: this method has no effect on unchanged values
						fieldService.updateValue(scope, event, dataset, field, stringValue, context, fallbackMessage);
					}
				}

				@Override
				public String getId() {
					return "SET_OBJECT_VALUE";
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
					final var field = (Field) evaluable;
					final var dataset = datasetService.get(field);
					final var scope = scopeService.get(dataset);
					final var event = eventService.get(dataset);

					fieldService.reset(scope, event, dataset, field, context, "Reset field");
				}

				@Override
				public String getId() {
					return "RESET";
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
				) throws InvalidValueException,
					BadlyFormattedValue {
					final var field = (Field) evaluable;
					final var dataset = datasetService.get(field);
					final var scope = scopeService.get(dataset);
					final var event = eventService.get(dataset);

					//update field: this method has no effect on unchanged values
					final var value = fieldService.getPluginValue(scope, event, dataset, field);
					fieldService.updateValue(scope, event, dataset, field, value, context, "Calculate value");
				}

				@Override
				public String getId() {
					return "CALCULATE";
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
					final var field = (Field) evaluable;
					final var workflow = studyService.getStudy().getWorkflow((String) parameters.get("WORKFLOW"));

					if(field.getFieldModel().getWorkflowIds().contains(workflow.getId())) {
						final var dataset = datasetService.get(field);
						final var scope = scopeService.get(dataset);
						final var event = eventService.get(dataset);
						final var family = new DataFamily(scope, event, dataset, field);
						workflowStatusService.create(family, field, workflow, data, context, "Initialize from rule");
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
					final var field = (Field) evaluable;
					final var workflow = studyService.getStudy().getWorkflow((String) parameters.get("WORKFLOW"));

					final var workflowStatuses = workflowStatusService.getAll(field, workflow);
					if(!workflowStatuses.isEmpty()) {
						final var dataset = datasetService.get(field);
						final var scope = scopeService.get(dataset);
						final var event = eventService.get(dataset);
						final var family = new DataFamily(scope, event, dataset, field);
						for(final var workflowStatus : workflowStatuses) {
							workflowStatusService.delete(family, workflowStatus, context, "Delete workflow");
						}
					}
				}

				@Override
				public String getId() {
					return "DELETE_WORKFLOW";
				}
			}
		);
	}

}
