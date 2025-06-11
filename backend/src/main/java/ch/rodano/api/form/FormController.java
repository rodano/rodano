package ch.rodano.api.form;

import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.config.ConfigDTOService;
import ch.rodano.api.config.LayoutDTO;
import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.controller.form.exception.DatasetSubmissionException;
import ch.rodano.api.dataset.DatasetDTO;
import ch.rodano.api.dataset.DatasetDTOService;
import ch.rodano.api.dataset.DatasetSubmissionDTO;
import ch.rodano.api.exception.http.BadArgumentException;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.api.utils.URLConsistencyUtils;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.workflow.WorkflowAction;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.dataset.DatasetSubmissionService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.event.EventDAOService;
import ch.rodano.core.services.dao.form.FormDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.rule.RuleService;
import ch.rodano.core.utils.RightsService;
import ch.rodano.core.utils.UtilsService;

@Profile("!migration")
@Tag(name = "Form")
@RestController
@RequestMapping("/scopes/{scopePk}")
@Validated
@Transactional(readOnly = true)
public class FormController extends AbstractSecuredController {
	private final ScopeDAOService scopeDAOService;
	private final EventDAOService eventDAOService;
	private final FormDAOService formDAOService;
	private final FormService formService;
	private final FormDTOService formDTOService;
	private final ConfigDTOService configDTOService;
	private final UtilsService utilsService;
	private final DatasetDTOService datasetDTOService;
	private final DatasetSubmissionService datasetSubmissionService;
	private final RuleService ruleService;

	public FormController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final ScopeDAOService scopeDAOService,
		final EventDAOService eventDAOService,
		final FormService formService,
		final FormDTOService formDTOService,
		final ConfigDTOService configDTOService,
		final UtilsService utilsService,
		final FormDAOService formDAOService,
		final DatasetDTOService datasetDTOService,
		final DatasetSubmissionService datasetSubmissionService,
		final RuleService ruleService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.scopeDAOService = scopeDAOService;
		this.eventDAOService = eventDAOService;
		this.formDAOService = formDAOService;
		this.formService = formService;
		this.formDTOService = formDTOService;
		this.configDTOService = configDTOService;
		this.utilsService = utilsService;
		this.datasetDTOService = datasetDTOService;
		this.datasetSubmissionService = datasetSubmissionService;
		this.ruleService = ruleService;
	}

	@Operation(summary = "Get forms")
	@GetMapping({ "forms", "events/{eventPk}/forms" })
	@ResponseStatus(HttpStatus.OK)
	public List<FormDTO> getForms(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		URLConsistencyUtils.checkConsistency(scope, event);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);
		event.ifPresent(e -> acl.checkRight(e.getEventModel(), Rights.READ));

		final var forms = formService.search(scope, event, acl);
		return formDTOService.createDTOs(scope, event, forms, acl);
	}

	@Operation(summary = "Get form")
	@GetMapping({ "forms/{formPk}", "events/{eventPk}/forms/{formPk}" })
	@ResponseStatus(HttpStatus.OK)
	public FormDTO getForm(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@PathVariable final Long formPk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var form = formDAOService.getFormByPk(formPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		utilsService.checkNotNull(Form.class, form, formPk);
		URLConsistencyUtils.checkConsistency(scope, event, form);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);
		event.ifPresent(e -> acl.checkRight(e.getEventModel(), Rights.READ));
		acl.checkRight(form.getFormModel(), Rights.READ);

		return formDTOService.createDTO(scope, event, form, acl);
	}

	@Operation(summary = "Get form layouts", description = "Get layouts that correspond to a specific form")
	@GetMapping({ "forms/{formPk}/layouts", "events/{eventPk}/forms/{formPk}/layouts" })
	@ResponseStatus(HttpStatus.OK)
	public List<LayoutDTO> getFormLayouts(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@PathVariable final Long formPk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var form = formDAOService.getFormByPk(formPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		utilsService.checkNotNull(Form.class, form, formPk);
		URLConsistencyUtils.checkConsistency(scope, event, form);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);
		event.ifPresent(e -> acl.checkRight(e.getEventModel(), Rights.READ));
		acl.checkRight(form.getFormModel(), Rights.READ);

		return configDTOService.createLayoutDTOs(scope, event, form, acl, currentLanguages());
	}

	//save a form and its datasets in one request
	@Operation(summary = "Save form")
	@PutMapping({ "forms/{formPk}", "events/{eventPk}/forms/{formPk}" })
	@ResponseStatus(HttpStatus.OK)
	@Transactional(rollbackFor = DatasetSubmissionException.class)
	public List<DatasetDTO> saveForm(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@PathVariable final Long formPk,
		@RequestBody final DatasetSubmissionDTO datasetSubmissionDTO
	) throws DatasetSubmissionException {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var form = formDAOService.getFormByPk(formPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		utilsService.checkNotNull(Form.class, form, formPk);
		URLConsistencyUtils.checkConsistency(scope, event, form);

		//cannot save a form if it's deleted
		if(form.getDeleted()) {
			throw new BadArgumentException("Form has been removed");
		}

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(form.getFormModel(), Rights.WRITE);

		final var datasets = datasetSubmissionService.submit(acl, scope, event, datasetSubmissionDTO, currentContext(), Optional.empty());

		final var state = new DataState(scope, event, form);

		//execute form save rules
		var rules = form.getFormModel().getRules();
		if(CollectionUtils.isNotEmpty(rules)) {
			ruleService.execute(state, rules, currentContext());
		}

		//execute global form save rules
		rules = studyService.getStudy().getEventActions().get(WorkflowAction.SAVE_FORM);
		if(CollectionUtils.isNotEmpty(rules)) {
			ruleService.execute(state, rules, currentContext());
		}

		return datasetDTOService.createDTOs(scope, event, form, datasets, acl);
	}
}
