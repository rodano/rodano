package ch.rodano.core.services.bll.workflowStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.workflow.WorkflowStatusSearch;
import ch.rodano.configuration.exceptions.NoRespectForConfigurationException;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.validator.Validator;
import ch.rodano.configuration.model.workflow.Action;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowAction;
import ch.rodano.configuration.model.workflow.WorkflowState;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.model.workflow.Workflowable;
import ch.rodano.core.services.bll.form.FormContentService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.dataset.DatasetDAOService;
import ch.rodano.core.services.dao.event.EventDAOService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.dao.form.FormDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.dao.workflow.WorkflowStatusDAOService;
import ch.rodano.core.services.rule.RuleService;

@Service
public class WorkflowStatusServiceImpl implements WorkflowStatusService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final StudyService studyService;
	private final WorkflowStatusDAOService workflowStatusDAOService;
	private final RuleService ruleService;
	private final FormContentService formContentService;
	private final ScopeDAOService scopeDAOService;
	private final EventDAOService eventDAOService;
	private final DatasetDAOService datasetDAOService;
	private final FormDAOService formDAOService;
	private final FieldDAOService fieldDAOService;

	public WorkflowStatusServiceImpl(
		final StudyService studyService,
		final WorkflowStatusDAOService workflowStatusDAOService,
		final RuleService ruleService,
		final FormContentService formContentService,
		final ScopeDAOService scopeDAOService,
		final EventDAOService eventDAOService,
		final DatasetDAOService datasetDAOService,
		final FormDAOService formDAOService,
		final FieldDAOService fieldDAOService
	) {
		this.studyService = studyService;
		this.workflowStatusDAOService = workflowStatusDAOService;
		this.ruleService = ruleService;
		this.formContentService = formContentService;
		this.scopeDAOService = scopeDAOService;
		this.eventDAOService = eventDAOService;
		this.datasetDAOService = datasetDAOService;
		this.formDAOService = formDAOService;
		this.fieldDAOService = fieldDAOService;
	}

	@Override
	public void save(
		final DataFamily family,
		final WorkflowStatus workflowStatus,
		final DatabaseActionContext context,
		final String rationale
	) {
		family.checkNotLocked();
		family.checkNotDeleted();

		workflowStatusDAOService.saveWorkflowStatus(workflowStatus, context, rationale);
	}

	@Override
	public void updateState(
		final DataFamily family,
		final WorkflowStatus workflowStatus,
		final WorkflowState state,
		final Map<String, Object> data,
		final DatabaseActionContext context,
		final String rationale
	) {
		//update modification date, add trail and execute rules only if workflow status changes
		if(!state.getId().equals(workflowStatus.getStateId())) {
			family.checkNotLocked();
			family.checkNotDeleted();

			workflowStatus.setState(state);
			workflowStatusDAOService.saveWorkflowStatus(workflowStatus, context, rationale);

			//execute modifications rules
			final var dataState = new DataState(family, workflowStatus);
			ruleService.execute(dataState, workflowStatus.getWorkflow().getRules(), context, rationale, data);
		}
	}

	@Override
	public void delete(
		final DataFamily family,
		final WorkflowStatus workflowStatus,
		final DatabaseActionContext context,
		final String rationale
	) {
		family.checkNotLocked();
		family.checkNotDeleted();

		workflowStatusDAOService.deleteWorkflowStatus(workflowStatus, context, rationale);
	}

	@Override
	public PagedResult<WorkflowStatus> search(final WorkflowStatusSearch search) {
		return workflowStatusDAOService.search(search);
	}

	@Override
	public Optional<Profile> getCreatorProfile(final WorkflowStatus workflowStatus) {
		if(workflowStatus.getUserFk() == null && workflowStatus.getRobotFk() == null) {
			return Optional.empty();
		}

		final var profile = studyService.getStudy().getProfile(workflowStatus.getProfileId());
		return Optional.of(profile);
	}

	@Override
	public WorkflowStatus create(
		final DataFamily family,
		final Workflowable workflowable,
		final Workflow workflow,
		final Optional<WorkflowState> state,
		final Optional<Action> action,
		final Optional<Validator> validator,
		final Optional<Profile> profile,
		final Map<String, Object> data,
		final DatabaseActionContext context,
		final String rationale
	) {
		family.checkNotLocked();
		family.checkNotDeleted();

		if(!workflowable.getWorkflowableModel().getWorkflowIds().contains(workflow.getId())) {
			throw new NoRespectForConfigurationException(String.format("Workflow %s is not allowed for %s", workflow.getId(), workflowable.getWorkflowableModel().getId()));
		}

		final var dataState = new DataState(family);

		//aggregator workflows can not be created in database
		//however the rules associated to their creation action can be executed
		//for example this is very useful to initialize workflows on fields from a scope (even if the workflow on the scope is an aggregated workflow)
		if(workflow.isAggregator()) {
			//execute rules only if workflow is initialized by a click on a button
			if(action.isPresent()) {
				//execute action rules
				ruleService.execute(dataState, action.get().getRules(), context, rationale, data, Collections.emptySet());
			}
			return null;
		}

		//if workflow is unique and an instance already exists, do not throw an exception but return the existing workflow status
		//uniqueness is more a safeguard than a constraint
		if(workflow.isUnique()) {
			final var existingWorkflowStatus = getMostRecent(workflowable, workflow);
			if(existingWorkflowStatus.isPresent()) {
				logger.info(
					"Initializing the unique workflow [{}] on [{}] but one instance of that workflow already exists: return existing instance", workflow.getId(), workflowable.getWorkflowableModel()
						.getId()
				);
				return existingWorkflowStatus.get();
			}
		}

		final var ws = new WorkflowStatus();
		ws.setScopeFk(family.scope().getPk());
		family.event().map(Event::getPk).ifPresent(ws::setEventFk);
		family.form().map(Form::getPk).ifPresent(ws::setFormFk);
		family.field().map(Field::getPk).ifPresent(ws::setFieldFk);

		ws.setWorkflow(workflow);
		ws.setTriggerMessage(rationale);
		//use orElseGet to retrieve the workflow initial state only if the state is not provided
		//retrieving the initial state will fail if the workflow has none
		ws.setState(state.orElseGet(() -> workflow.getInitialState()));
		action.ifPresent(ws::setAction);
		validator.ifPresent(ws::setValidator);
		profile.ifPresent(ws::setProfile);

		workflowStatusDAOService.saveWorkflowStatus(ws, context, rationale);

		final var statusDataStates = dataState.withWorkflows(Collections.singleton(ws));

		//execute rules for all created visits
		final var triggerRules = studyService.getStudy().getEventActions().get(WorkflowAction.CREATE_WORKFLOW_STATUS);

		//execute trigger rules
		if(CollectionUtils.isNotEmpty(triggerRules)) {
			ruleService.execute(statusDataStates, triggerRules, context);
		}

		//execute rules only if workflow is initialized by a click on a button
		if(action.isPresent()) {
			//execute action rules
			ruleService.execute(statusDataStates, action.get().getRules(), context, rationale, data, Collections.emptySet());
		}

		return ws;
	}

	@Override
	public WorkflowStatus create(
		final DataFamily family,
		final Workflowable workflowable,
		final Workflow workflow,
		final Action action,
		final Profile profile,
		final DatabaseActionContext context,
		final String rationale
	) {
		return create(
			family,
			workflowable,
			workflow,
			Optional.empty(),
			Optional.of(action),
			Optional.empty(),
			Optional.of(profile),
			Collections.emptyMap(),
			context,
			rationale
		);
	}

	@Override
	public WorkflowStatus create(
		final DataFamily family,
		final Workflowable workflowable,
		final Workflow workflow,
		final Map<String, Object> data,
		final DatabaseActionContext context,
		final String rationale
	) {
		return create(
			family,
			workflowable,
			workflow,
			Optional.empty(),
			Optional.empty(),
			Optional.empty(),
			Optional.empty(),
			data,
			context,
			rationale
		);
	}

	@Override
	public WorkflowStatus create(
		final DataFamily family,
		final Workflowable workflowable,
		final Workflow workflow,
		final WorkflowState state,
		final Map<String, Object> data,
		final DatabaseActionContext context,
		final String rationale
	) {
		return create(
			family,
			workflowable,
			workflow,
			Optional.of(state),
			Optional.empty(),
			Optional.empty(),
			Optional.empty(),
			data,
			context,
			rationale
		);
	}

	@Override
	public List<WorkflowStatus> createAll(
		final DataFamily family,
		final Workflowable workflowable,
		final Map<String, Object> data,
		final DatabaseActionContext context,
		final String rationale
	) {
		return workflowable.getWorkflowableModel().getWorkflows().stream()
			.filter(w -> !w.isAggregator() && w.isMandatory())
			.map(w -> create(family, workflowable, w, data, context, rationale))
			.toList();
	}

	@Override
	public void resetMandatoryAndDeleteTheRest(
		final DataFamily family,
		final Workflowable workflowable,
		final DatabaseActionContext context,
		final String rationale
	) {
		for(final var ws : getAll(workflowable)) {
			if(ws.getWorkflow().isMandatory()) {
				updateState(family, ws, ws.getWorkflow().getInitialState(), Collections.emptyMap(), context, rationale);
			}
			else {
				delete(family, ws, context, rationale);
			}
		}
	}

	@Override
	public Workflowable getWorkflowable(final WorkflowStatus workflowStatus) {
		return switch(workflowStatus.getWorkflowableType()) {
			case EVENT -> eventDAOService.getEventByPk(workflowStatus.getEventFk());
			case FORM -> formDAOService.getFormByPk(workflowStatus.getFormFk());
			case FIELD -> fieldDAOService.getFieldByPk(workflowStatus.getFieldFk());
			default -> scopeDAOService.getScopeByPk(workflowStatus.getScopeFk());
		};
	}

	@Override
	public List<WorkflowStatus> getAll(final Workflowable workflowable) {
		return switch(workflowable.getWorkflowableEntity()) {
			case SCOPE -> getAll((Scope) workflowable);
			case EVENT -> getAll((Event) workflowable);
			case FORM -> getAll((Form) workflowable);
			case FIELD -> getAll((Field) workflowable);
		};
	}

	@Override
	public List<WorkflowStatus> getAll(final Workflowable workflowable, final Workflow workflow) {
		return switch(workflowable.getWorkflowableEntity()) {
			case SCOPE -> getAll((Scope) workflowable, workflow);
			case EVENT -> getAll((Event) workflowable, workflow);
			case FORM -> getAll((Form) workflowable, workflow);
			case FIELD -> getAll((Field) workflowable, workflow);
		};
	}

	@Override
	public List<WorkflowStatus> getAll(final Workflowable workflowable, final WorkflowState workflowState) {
		return getAll(workflowable, workflowState.getWorkflow()).stream()
			.filter(w -> w.getStateId().equals(workflowState.getId()))
			.toList();
	}

	@Override
	public Optional<WorkflowStatus> getMostRecent(final Workflowable workflowable, final Workflow workflow) {
		return getAll(workflowable, workflow).stream()
			.max(WorkflowStatus.MOST_RECENT_COMPARATOR);
	}

	@Override
	public Optional<WorkflowStatus> getMostRecent(final Workflowable workflowable, final WorkflowState workflowState) {
		return getAll(workflowable, workflowState).stream()
			.max(WorkflowStatus.MOST_RECENT_COMPARATOR);
	}

	@Override
	public List<WorkflowStatus> getContained(
		final Scope scope,
		final Optional<Event> event,
		final Form form
	) {
		final var formContent = formContentService.generateFormContent(scope, event, form);
		final var fieldPks = formContent.getAllNonDeletedFields().stream()
			.filter(f -> f.getValue() != null)
			.map(Field::getPk)
			.toList();

		if(fieldPks.isEmpty()) {
			return Collections.emptyList();
		}
		return workflowStatusDAOService.getWorkflowStatusesByFieldPks(fieldPks);
	}

	@Override
	public Map<Form, List<WorkflowStatus>> getContained(
		final Scope scope,
		final Optional<Event> event,
		final List<Form> forms
	) {
		final var scopeDatasets = datasetDAOService.getAllDatasetsByScopePk(scope.getPk());
		final var eventDatasets = event.map(Event::getPk).map(datasetDAOService::getAllDatasetsByEventPk).orElse(Collections.emptyList());
		final var fields = fieldDAOService.getFieldsRelatedToEvent(scope.getPk(), event.map(Event::getPk));

		final var wsByForm = new HashMap<Form, List<WorkflowStatus>>();
		for(final var form : forms) {
			final var formContent = formContentService.generateFormContent(scope, event, form, scopeDatasets, eventDatasets, fields);
			final var fieldPks = formContent.getAllNonDeletedFields().stream()
				.filter(f -> f.getValue() != null)
				.map(Field::getPk)
				.toList();
			final List<WorkflowStatus> ws = fieldPks.isEmpty() ? Collections.emptyList() : workflowStatusDAOService.getWorkflowStatusesByFieldPks(fieldPks);
			wsByForm.put(form, ws);
		}
		return wsByForm;
	}

	@Override
	public DataFamily createDataFamily(final WorkflowStatus workflowStatus) {
		final var scope = scopeDAOService.getScopeByPk(workflowStatus.getScopeFk());
		if(workflowStatus.getFieldFk() != null) {
			final var field = fieldDAOService.getFieldByPk(workflowStatus.getFieldFk());
			final var dataset = datasetDAOService.getDatasetByPk(field.getDatasetFk());
			final var event = Optional.ofNullable(dataset.getEventFk()).map(eventDAOService::getEventByPk);
			return new DataFamily(scope, event, dataset, field);
		}

		if(workflowStatus.getFormFk() != null) {
			final var form = formDAOService.getFormByPk(workflowStatus.getFormFk());
			final var event = Optional.ofNullable(form.getEventFk()).map(eventDAOService::getEventByPk);
			return new DataFamily(scope, event, form);
		}

		if(workflowStatus.getEventFk() != null) {
			final var event = eventDAOService.getEventByPk(workflowStatus.getEventFk());
			return new DataFamily(scope, event);
		}

		return new DataFamily(scope);
	}

	private List<WorkflowStatus> getAll(final Scope scope) {
		return workflowStatusDAOService.getWorkflowStatusesByScopePk(scope.getPk());
	}

	private List<WorkflowStatus> getAll(final Event event) {
		return workflowStatusDAOService.getWorkflowStatusesByEventPk(event.getPk());
	}

	private List<WorkflowStatus> getAll(final Form form) {
		return workflowStatusDAOService.getWorkflowStatusesByFormPk(form.getPk());
	}

	private List<WorkflowStatus> getAll(final Field field) {
		return workflowStatusDAOService.getWorkflowStatusesByFieldPk(field.getPk());
	}

	private List<WorkflowStatus> getAll(final Scope scope, final Workflow workflow) {
		return workflowStatusDAOService.getWorkflowStatusesByScopePk(scope.getPk(), workflow.getId());
	}

	private List<WorkflowStatus> getAll(final Event event, final Workflow workflow) {
		return workflowStatusDAOService.getWorkflowStatusesByEventPk(event.getPk(), workflow.getId());
	}

	private List<WorkflowStatus> getAll(final Form form, final Workflow workflow) {
		return workflowStatusDAOService.getWorkflowStatusesByFormPk(form.getPk(), workflow.getId());
	}

	private List<WorkflowStatus> getAll(final Field field, final Workflow workflow) {
		return workflowStatusDAOService.getWorkflowStatusesByFieldPk(field.getPk(), workflow.getId());
	}
}
