package ch.rodano.api.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.event.EventDTO;
import ch.rodano.api.event.EventDTOService;
import ch.rodano.api.exception.http.MissingArgumentException;
import ch.rodano.api.field.FieldDTO;
import ch.rodano.api.field.FieldDTOService;
import ch.rodano.api.form.FormDTO;
import ch.rodano.api.form.FormDTOService;
import ch.rodano.api.form.FormInfoDTO;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.api.scope.ScopeDTO;
import ch.rodano.api.scope.ScopeDTOService;
import ch.rodano.api.utils.URLConsistencyUtils;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.workflow.Action;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.core.model.actor.ActorType;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.exception.security.WrongCredentialsException;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.model.workflow.WorkflowStatusSortBy;
import ch.rodano.core.model.workflow.Workflowable;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.dataset.DatasetDAOService;
import ch.rodano.core.services.dao.event.EventDAOService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.dao.form.FormDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.dao.workflow.WorkflowStatusDAOService;
import ch.rodano.core.services.rule.RuleService;
import ch.rodano.core.utils.ACL;
import ch.rodano.core.utils.RightsService;

@Tag(name = "Workflow status")
@RestController
@Validated
@Transactional(readOnly = true)
public class WorkflowStatusController extends AbstractSecuredController {
	private final EventDAOService eventDAOService;
	private final ScopeDTOService scopeDTOService;
	private final FormDAOService formDAOService;
	private final FormService formService;
	private final FormDTOService formDTOService;
	private final EventService eventService;
	private final EventDTOService eventDTOService;
	private final WorkflowStatusService workflowStatusService;
	private final WorkflowStatusDAOService workflowStatusDAOService;
	private final ScopeDAOService scopeDAOService;
	private final FieldDTOService fieldDTOService;
	private final UserSecurityService userSecurityService;
	private final RuleService ruleService;
	private final WorkflowDTOService workflowDTOService;
	private final FieldDAOService fieldDAOService;
	private final DatasetDAOService datasetDAOService;

	private final Integer defaultPageSize;

	public WorkflowStatusController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final EventDAOService eventDAOService,
		final ScopeDTOService scopeDTOService,
		final FormDAOService formDAOService,
		final FormService formService,
		final FormDTOService formDTOService,
		final EventService eventService,
		final EventDTOService eventDTOService,
		final WorkflowStatusService workflowStatusService,
		final ScopeDAOService scopeDAOService,
		final FieldDTOService fieldDTOService,
		final UserSecurityService userSecurityService,
		final RuleService ruleService,
		final WorkflowDTOService workflowDTOService,
		final FieldDAOService fieldDAOService,
		final DatasetDAOService datasetDAOService,
		final WorkflowStatusDAOService workflowStatusDAOService,
		@Value("${rodano.pagination.maximum-page-size}") final Integer defaultPageSize
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.eventDAOService = eventDAOService;
		this.scopeDTOService = scopeDTOService;
		this.formDAOService = formDAOService;
		this.formService = formService;
		this.formDTOService = formDTOService;
		this.eventService = eventService;
		this.eventDTOService = eventDTOService;
		this.workflowStatusService = workflowStatusService;
		this.workflowStatusDAOService = workflowStatusDAOService;
		this.scopeDAOService = scopeDAOService;
		this.fieldDTOService = fieldDTOService;
		this.userSecurityService = userSecurityService;
		this.ruleService = ruleService;
		this.workflowDTOService = workflowDTOService;
		this.fieldDAOService = fieldDAOService;
		this.datasetDAOService = datasetDAOService;
		this.defaultPageSize = defaultPageSize;
	}

	@Operation(summary = "Search workflows")
	@GetMapping("workflows")
	public PagedResult<WorkflowStatusDTO> search(
		@Parameter(description = "Workflow IDs") @RequestParam final Optional<List<String>> workflowIds,
		@Parameter(description = "State IDs") @RequestParam final Optional<List<String>> stateIds,
		@Parameter(description = "Ancestor scope PKs") @RequestParam final Optional<List<Long>> ancestorScopePks,
		@Parameter(description = "Scope PKs") @RequestParam final Optional<List<Long>> scopePks,
		@Parameter(description = "Event PKs") @RequestParam final Optional<List<Long>> eventPks,
		@Parameter(description = "Full-text search on workflows") @RequestParam final Optional<String> fullText,
		@Parameter(description = "Include workflows on expected events ?") @RequestParam final Optional<Boolean> filterExpectedEvents,
		@Parameter(description = "Order the results by which property ?") @RequestParam final Optional<WorkflowStatusSortBy> sortBy,
		@Parameter(description = "Use the ascending order ?") @RequestParam final Optional<Boolean> orderAscending,
		@Parameter(description = "Page size") @RequestParam final Optional<Integer> pageSize,
		@Parameter(description = "Page index") @RequestParam final Optional<Integer> pageIndex
	) {
		final var currentActor = currentActor();
		final var acl = rightsService.getACL(currentActor);

		//use the ancestor scope pks provided in the URL or retrieve root scopes of the user
		final Collection<Scope> ancestorScopes = new ArrayList<>();
		if(ancestorScopePks.isPresent()) {
			ancestorScopes.addAll(scopeDAOService.getScopesByPks(ancestorScopePks.get()));
			for(final var scope : ancestorScopes) {
				final var scopeRoles = currentActiveRoles(scope);
				rightsService.checkRight(currentActor, scopeRoles, scope);
			}
		}
		else {
			ancestorScopes.addAll(actorService.getRootScopes(currentActor));
		}

		//use the workflow ids provided or retrieve root scopes of the user
		final Collection<Workflow> workflows = new ArrayList<>();
		if(workflowIds.isPresent()) {
			workflows.addAll(studyService.getStudy().getNodesFromIds(Entity.WORKFLOW, workflowIds.get()));
			for(final var workflow : workflows) {
				acl.checkRight(workflow);
			}
		}
		else {
			workflows.addAll(studyService.getStudy().getWorkflows().stream().filter(acl::hasRight).toList());
		}

		final var search = new WorkflowStatusSearch()
			.setAncestorScopePks(ancestorScopes.stream().map(Scope::getPk).collect(Collectors.toList()))
			.setWorkflowIds(workflows.stream().map(Workflow::getId).collect(Collectors.toList()))
			.setStateIds(stateIds)
			.setScopePks(scopePks)
			.setEventPks(eventPks)
			.setFullText(fullText.filter(StringUtils::isNotBlank))
			.setFilterExpectedEvents(filterExpectedEvents)
			.setPageSize(pageSize.isEmpty() ? Optional.of(defaultPageSize) : pageSize)
			.setPageIndex(pageIndex.isEmpty() ? Optional.of(0) : pageIndex);
		//set sort if provided
		sortBy.map(search::setSortBy);
		orderAscending.map(search::setSortAscending);

		final var workflowStatuses = workflowStatusService.search(search);
		final var dtoFunction = (Function<WorkflowStatus, WorkflowStatusDTO>) workflowStatus -> workflowDTOService.createWorkflowStatusDTO(workflowStatus, acl);
		return workflowStatuses.withObjectTransformation(dtoFunction);
	}

	@Operation(summary = "Initialise the given workflow on a field")
	@PostMapping({ "scopes/{scopePk}/datasets/{datasetPk}/fields/{fieldPk}/workflows", "scopes/{scopePk}/events/{eventPk}/datasets/{datasetPk}/fields/{fieldPk}/workflows" })
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public FieldDTO initWorkflowOnField(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@PathVariable final Long datasetPk,
		@PathVariable final Long fieldPk,
		@Valid @RequestBody final WorkflowUpdateDTO workflowAction
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var dataset = datasetDAOService.getDatasetByPk(datasetPk);
		final var field = fieldDAOService.getFieldByPk(fieldPk);
		URLConsistencyUtils.checkConsistency(scope, event, dataset, field);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);
		event.ifPresent(e -> acl.checkRight(e.getEventModel(), Rights.READ));
		acl.checkRight(dataset.getDatasetModel(), Rights.READ);

		final var family = new DataFamily(scope, event, dataset, field);
		final var fieldAfterAction = (Field) initializeWorkflowStatus(
			family,
			field,
			acl,
			workflowAction.actionId(),
			workflowAction.rationale(),
			workflowAction.email(),
			workflowAction.password(),
			workflowAction.workflowId()
		);

		return fieldDTOService.createDTO(scope, event, dataset, fieldAfterAction, acl);
	}

	@Operation(summary = "Execute the given workflow action on a field")
	@PutMapping({ "scopes/{scopePk}/datasets/{datasetPk}/fields/{fieldPk}/workflows/{workflowPk}", "scopes/{scopePk}/events/{eventPk}/datasets/{datasetPk}/fields/{fieldPk}/workflows/{workflowPk}" })
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public FieldDTO doActionOnFieldWorkflow(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@PathVariable final Long datasetPk,
		@PathVariable final Long fieldPk,
		@PathVariable final Long workflowPk,
		@Valid @RequestBody final WorkflowUpdateDTO workflowAction
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var dataset = datasetDAOService.getDatasetByPk(datasetPk);
		final var field = fieldDAOService.getFieldByPk(fieldPk);
		URLConsistencyUtils.checkConsistency(scope, event, dataset, field);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);
		event.ifPresent(e -> acl.checkRight(e.getEventModel(), Rights.READ));
		acl.checkRight(dataset.getDatasetModel(), Rights.READ);

		final var family = new DataFamily(scope, event, dataset, field);
		final var fieldAfterAction = (Field) executeWorkflowAction(
			acl,
			family,
			field,
			workflowPk,
			workflowAction.actionId(),
			workflowAction.rationale(),
			workflowAction.email(),
			workflowAction.password()
		);

		return fieldDTOService.createDTO(scope, event, dataset, fieldAfterAction, acl);
	}

	@Operation(summary = "Initialise the given workflow on an event")
	@PostMapping("scopes/{scopePk}/events/{eventPk}/workflows")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public EventDTO initWorkflowOnEvent(
		@PathVariable final Long scopePk,
		@PathVariable final Long eventPk,
		@Valid @RequestBody final WorkflowUpdateDTO workflowAction
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventDAOService.getEventByPk(eventPk);
		URLConsistencyUtils.checkConsistency(scope, event);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);
		acl.checkRight(event.getEventModel(), Rights.READ);

		final var family = new DataFamily(scope, event);
		final var eventAfterAction = (Event) initializeWorkflowStatus(
			family,
			event,
			acl,
			workflowAction.actionId(),
			workflowAction.rationale(),
			workflowAction.email(),
			workflowAction.password(),
			workflowAction.workflowId()
		);

		return eventDTOService.createDTO(scope, eventAfterAction, acl);
	}

	@Operation(summary = "Execute the given workflow action on a event")
	@PutMapping("scopes/{scopePk}/events/{eventPk}/workflows/{workflowPk}")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public EventDTO doActionOnEventWorkflow(
		@PathVariable final Long scopePk,
		@PathVariable final Long eventPk,
		@PathVariable final Long workflowPk,
		@Valid @RequestBody final WorkflowUpdateDTO workflowAction
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventDAOService.getEventByPk(eventPk);
		URLConsistencyUtils.checkConsistency(scope, event);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);
		acl.checkRight(event.getEventModel(), Rights.READ);

		final var family = new DataFamily(scope, event);
		final var eventAfterAction = (Event) executeWorkflowAction(
			acl,
			family,
			event,
			workflowPk,
			workflowAction.actionId(),
			workflowAction.rationale(),
			workflowAction.email(),
			workflowAction.password()
		);

		return eventDTOService.createDTO(scope, eventAfterAction, acl);
	}

	@Operation(summary = "Execute the given aggregate workflow action on a event")
	@PutMapping("scopes/{scopePk}/events/{eventPk}/workflows/{workflowId}/{actionId}")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public EventDTO doActionOnEventAggregateWorkflow(
		@PathVariable final Long scopePk,
		@PathVariable final Long eventPk,
		@PathVariable final String workflowId,
		@PathVariable final String actionId,
		@Valid @RequestBody final WorkflowUpdateDTO workflowAction
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventDAOService.getEventByPk(eventPk);
		URLConsistencyUtils.checkConsistency(scope, event);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);
		acl.checkRight(event.getEventModel(), Rights.READ);

		final var family = new DataFamily(scope, event);
		final var eventAfterAction = (Event) executeAggregateWorkflowAction(
			acl,
			family,
			event,
			workflowId,
			actionId,
			workflowAction.rationale(),
			workflowAction.email(),
			workflowAction.password()
		);

		return eventDTOService.createDTO(scope, eventAfterAction, acl);
	}

	@Operation(summary = "Initialise the given workflow on a form")
	@PostMapping({ "scopes/{scopePk}/events/{eventPk}/forms/{formPk}/workflows", "scopes/{scopePk}/forms/{formPk}/workflows" })
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public FormDTO initWorkflowOnForm(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@PathVariable final Long formPk,
		@Valid @RequestBody final WorkflowUpdateDTO workflowAction
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var form = formDAOService.getFormByPk(formPk);
		URLConsistencyUtils.checkConsistency(scope, event, form);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);
		event.ifPresent(e -> acl.checkRight(e.getEventModel(), Rights.READ));
		acl.checkRight(form.getFormModel(), Rights.READ);

		final var family = new DataFamily(scope, event, form);
		final var formAfterAction = (Form) initializeWorkflowStatus(
			family,
			form,
			acl,
			workflowAction.actionId(),
			workflowAction.rationale(),
			workflowAction.email(),
			workflowAction.password(),
			workflowAction.workflowId()
		);

		return formDTOService.createDTO(scope, event, formAfterAction, acl);
	}

	@Operation(summary = "Execute the given workflow action on a form")
	@PutMapping({ "scopes/{scopePk}/events/{eventPk}/forms/{formPk}/workflows/{workflowPk}", "scopes/{scopePk}/forms/{formPk}/workflows/{workflowPk}" })
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public FormDTO doActionOnFormWorkflow(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@PathVariable final Long formPk,
		@PathVariable final Long workflowPk,
		@Valid @RequestBody final WorkflowUpdateDTO workflowAction
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var form = formDAOService.getFormByPk(formPk);
		URLConsistencyUtils.checkConsistency(scope, event, form);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);
		event.ifPresent(e -> acl.checkRight(e.getEventModel(), Rights.READ));
		acl.checkRight(form.getFormModel(), Rights.READ);

		final var family = new DataFamily(scope, event, form);
		final var formAfterAction = (Form) executeWorkflowAction(
			acl,
			family,
			form,
			workflowPk,
			workflowAction.actionId(),
			workflowAction.rationale(),
			workflowAction.email(),
			workflowAction.password()
		);

		return formDTOService.createDTO(scope, event, formAfterAction, acl);
	}

	@Operation(summary = "Execute the given aggregate workflow action on a form")
	@PutMapping({ "scopes/{scopePk}/events/{eventPk}/forms/{formPk}/workflows/{workflowId}/{actionId}", "scopes/{scopePk}/forms/{formPk}/workflows/{workflowId}/{actionId}" })
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public FormDTO doActionOnFormAggregateWorkflow(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@PathVariable final Long formPk,
		@PathVariable final String workflowId,
		@PathVariable final String actionId,
		@Valid @RequestBody final WorkflowUpdateDTO workflowAction
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var form = formDAOService.getFormByPk(formPk);

		URLConsistencyUtils.checkConsistency(scope, event, form);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);
		event.ifPresent(e -> acl.checkRight(e.getEventModel(), Rights.READ));
		acl.checkRight(form.getFormModel(), Rights.READ);

		final var family = new DataFamily(scope, event, form);
		final var formAfterAction = (Form) executeAggregateWorkflowAction(
			acl,
			family,
			form,
			workflowId,
			actionId,
			workflowAction.rationale(),
			workflowAction.email(),
			workflowAction.password()
		);

		return formDTOService.createDTO(scope, event, formAfterAction, acl);
	}

	@Operation(summary = "Initialise the given workflow on a scope")
	@PostMapping("scopes/{scopePk}/workflows")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public ScopeDTO initWorkflowOnScope(
		@PathVariable final Long scopePk,
		@Valid @RequestBody final WorkflowUpdateDTO workflowAction
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);

		final var family = new DataFamily(scope);
		final var scopeAfterAction = (Scope) initializeWorkflowStatus(
			family,
			scope,
			acl,
			workflowAction.actionId(),
			workflowAction.rationale(),
			workflowAction.email(),
			workflowAction.password(),
			workflowAction.workflowId()
		);

		return scopeDTOService.createDTO(scopeAfterAction, acl);
	}

	@Operation(summary = "Execute the selected workflow action on a scope")
	@PutMapping("scopes/{scopePk}/workflows/{workflowPk}")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public ScopeDTO doActionOnScopeWorkflow(
		@PathVariable final Long scopePk,
		@PathVariable final Long workflowPk,
		@Valid @RequestBody final WorkflowUpdateDTO workflowAction
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);

		final var family = new DataFamily(scope);
		final var scopeAfterAction = (Scope) executeWorkflowAction(
			acl,
			family,
			scope,
			workflowPk,
			workflowAction.actionId(),
			workflowAction.rationale(),
			workflowAction.email(),
			workflowAction.password()
		);

		return scopeDTOService.createDTO(scopeAfterAction, acl);
	}

	@Operation(summary = "Execute the selected aggregate workflow action on a scope")
	@PutMapping("scopes/{scopePk}/workflows/{workflowId}/{actionId}")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public ScopeDTO doActionOnScopeAggregateWorkflow(
		@PathVariable final Long scopePk,
		@PathVariable final String workflowId,
		@PathVariable final String actionId,
		@Valid @RequestBody final WorkflowUpdateDTO workflowAction
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);

		final var family = new DataFamily(scope);
		final var scopeAfterAction = (Scope) executeAggregateWorkflowAction(
			acl,
			family,
			scope,
			workflowId,
			actionId,
			workflowAction.rationale(),
			workflowAction.email(),
			workflowAction.password()
		);

		return scopeDTOService.createDTO(scopeAfterAction, acl);
	}

	@Deprecated
	@Operation(summary = "Get form for the workflow status")
	@GetMapping("workflows/{workflowPk}/form")
	@ResponseStatus(HttpStatus.OK)
	public FormInfoDTO getFormFromWorkflowStatus(
		@PathVariable final Long workflowPk
	) {
		final var ws = workflowStatusDAOService.getWorkflowStatusByPk(workflowPk);
		final var scope = scopeDAOService.getScopeByPk(ws.getScopeFk());
		final var event = Optional.ofNullable(ws.getEventFk()).map(eventDAOService::getEventByPk);
		final var field = fieldDAOService.getFieldByPk(ws.getFieldFk());
		final var dataset = datasetDAOService.getDatasetByPk(field.getDatasetFk());
		final var fieldModel = field.getFieldModel();

		final var acl = rightsService.getACL(currentActor(), scope);

		final var fieldDTO = fieldDTOService.createDTO(scope, event, dataset, field, acl);

		//if the workflow is attached to a event, find form in this event
		if(event.isPresent()) {
			final var formModel = event.get().getEventModel().getFormModels().stream()
				.filter(f -> f.containsFieldModel(fieldModel))
				.findFirst()
				.orElseThrow();
			final var form = formService.get(event.get(), formModel.getId());
			return new FormInfoDTO(fieldDTO, scope.getPk(), event.get().getPk(), form.getPk());
		}
		//if the workflow is attached to the scope (not attached to a event), search form in the scope forms
		else if(!scope.getScopeModel().getFormModels().isEmpty()) {
			final var formModel = scope.getScopeModel().getFormModels().stream()
				.filter(f -> f.containsFieldModel(fieldModel))
				.findFirst()
				.orElseThrow();
			final var form = formService.get(scope, formModel.getId());
			return new FormInfoDTO(fieldDTO, scope.getPk(), null, form.getPk());
		}
		else {
			//if the workflow is attached to the scope (not attached to a event) and has not been found in the scope forms, search in all events starting from the inceptive event
			final var events = new ArrayList<Event>();
			events.add(eventService.getInceptive(scope));
			events.addAll(eventService.getAll(scope));
			for(final var e : events) {
				final var formModel = e.getEventModel().getFormModels().stream()
					.filter(f -> f.containsFieldModel(fieldModel))
					.findFirst();

				if(formModel.isPresent()) {
					final var form = formService.get(e, formModel.get().getId());
					return new FormInfoDTO(fieldDTO, scope.getPk(), e.getPk(), form.getPk());
				}
			}
		}
		throw new UnsupportedOperationException(String.format("Unable to find the form containing the workflow %d", workflowPk));
	}

	private Workflowable executeWorkflowAction(
		final ACL acl,
		final DataFamily family,
		final Workflowable workflowable,
		final Long workflowStatusPk,
		final String actionId,
		final String rationale,
		final String email,
		final String password
	) {
		family.checkNotLocked();
		family.checkNotDeleted();

		// Retrieve current status to retrieve creator role
		final var workflowStatus = workflowStatusDAOService.getWorkflowStatusByPk(workflowStatusPk);

		// Retrieve workflow and check rights
		final var workflow = studyService.getStudy().getWorkflow(workflowStatus.getWorkflowId());
		acl.checkRight(workflow);

		// Retrieve action
		final var action = workflow.getAction(actionId);

		// Check rationale or login and password if needed
		checkRequiredParameters(acl, email, password, rationale, action);

		// Check right on action
		acl.checkRight(action, workflowStatusService.getCreatorProfile(workflowStatus));

		ruleService.execute(new DataState(family, workflowStatus), action.getRules(), currentContext(), rationale, Collections.emptyMap());

		return workflowable;
	}

	private Workflowable executeAggregateWorkflowAction(
		final ACL acl,
		final DataFamily family,
		final Workflowable workflowable,
		final String workflowId,
		final String actionId,
		final String rationale,
		final String email,
		final String password
	) {
		family.checkNotLocked();
		family.checkNotDeleted();

		// Retrieve workflow and check rights
		final var workflow = studyService.getStudy().getWorkflow(workflowId);
		acl.checkRight(workflow);

		// Retrieve action
		final var action = workflow.getAction(actionId);

		// Check rationale or login and password if needed
		checkRequiredParameters(acl, email, password, rationale, action);

		//Check right on action
		acl.checkRight(action, Optional.empty());

		ruleService.execute(new DataState(family), action.getRules(), currentContext(), rationale, Collections.emptyMap());

		return workflowable;
	}

	private Workflowable initializeWorkflowStatus(
		final DataFamily family,
		final Workflowable workflowable,
		final ACL acl,
		final String actionId,
		final String rationale,
		final String email,
		final String password,
		final String workflowId
	) {

		// Retrieve workflow and check rights
		final var workflow = studyService.getStudy().getWorkflow(workflowId);
		acl.checkRight(workflow);

		// Retrieve action
		final var action = workflow.getAction(actionId);

		// Check rationale or login and password if needed
		checkRequiredParameters(acl, email, password, rationale, action);

		// Check right on action
		acl.checkRight(action, Optional.empty());

		//find profile
		final var profile = actorService.getActiveProfiles(acl.actor()).get(0);

		// Initialize workflow
		workflowStatusService.create(
			family,
			workflowable,
			workflow,
			action,
			profile,
			currentContext(),
			//rational will be empty if the workflow is not documentable
			//however, a rationale is still required for the context
			StringUtils.defaultIfBlank(rationale, String.format("Created by %s", acl.actor().getName()))
		);

		return workflowable;
	}

	// TODO this should be in the service layer
	private void checkRequiredParameters(
		final ACL acl,
		final String email,
		final String password,
		final String rationale,
		final Action action
	) {
		//check rationale for documentable workflows
		if(action.isDocumentable() && StringUtils.isBlank(rationale)) {
			throw new MissingArgumentException("rationale");
		}

		//check signature for workflows that need to be signed
		if(action.isRequireSignature()) {
			if(!ActorType.USER.equals(acl.actor().getType())) {
				throw new IllegalArgumentException("Only users can update a workflow that requires a signature");
			}

			if(StringUtils.isAnyBlank(email, password)) {
				throw new MissingArgumentException("e-mail and password");
			}

			final User user = (User) acl.actor();
			if(!email.equals(user.getEmail())) {
				throw new WrongCredentialsException();
			}

			if(!userSecurityService.isPasswordValid(user, password)) {
				throw new WrongCredentialsException();
			}
		}
	}
}
