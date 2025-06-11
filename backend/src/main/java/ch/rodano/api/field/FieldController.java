package ch.rodano.api.field;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.api.utils.URLConsistencyUtils;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.dataset.DatasetDAOService;
import ch.rodano.core.services.dao.event.EventDAOService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;
import ch.rodano.core.utils.UtilsService;

@Tag(name = "Field")
@RestController
@RequestMapping("/scopes/{scopePk}")
@Validated
@Transactional(readOnly = true)
public class FieldController extends AbstractSecuredController {
	private final FieldDTOService fieldDTOService;
	private final ScopeDAOService scopeDAOService;
	private final EventDAOService eventDAOService;
	private final FieldDAOService fieldDAOService;
	private final DatasetDAOService datasetDAOService;
	private final UtilsService utilsService;

	public FieldController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final FieldDTOService fieldDTOService,
		final ScopeDAOService scopeDAOService,
		final EventDAOService eventDAOService,
		final FieldDAOService fieldDAOService,
		final DatasetDAOService datasetDAOService,
		final UtilsService utilsService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.fieldDTOService = fieldDTOService;
		this.scopeDAOService = scopeDAOService;
		this.eventDAOService = eventDAOService;
		this.fieldDAOService = fieldDAOService;
		this.datasetDAOService = datasetDAOService;
		this.utilsService = utilsService;
	}

	@Operation(summary = "Get a field")
	@GetMapping("datasets/{datasetPk}/fields/{fieldPk}")
	@ResponseStatus(HttpStatus.OK)
	public FieldDTO getField(
		@PathVariable final Long scopePk,
		@PathVariable final Long datasetPk,
		@PathVariable final Long fieldPk
	) {
		return getField(scopePk, Optional.empty(), datasetPk, fieldPk);
	}

	@Operation(summary = "Get a field")
	@GetMapping("events/{eventPk}/datasets/{datasetPk}/fields/{fieldPk}")
	@ResponseStatus(HttpStatus.OK)
	public FieldDTO getFieldOnEvent(
		@PathVariable final Long scopePk,
		@PathVariable final Long eventPk,
		@PathVariable final Long datasetPk,
		@PathVariable final Long fieldPk
	) {
		return getField(scopePk, Optional.of(eventPk), datasetPk, fieldPk);
	}

	private FieldDTO getField(final Long scopePk, final Optional<Long> eventPk, final Long datasetPk, final Long fieldPk) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var dataset = datasetDAOService.getDatasetByPk(datasetPk);
		final var field = fieldDAOService.getFieldByPk(fieldPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		utilsService.checkNotNull(Dataset.class, dataset, datasetPk);
		utilsService.checkNotNull(Field.class, field, fieldPk);
		URLConsistencyUtils.checkConsistency(scope, event, dataset, field);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(dataset.getDatasetModel(), Rights.READ);

		return fieldDTOService.createDTO(scope, event, dataset, field, acl);
	}
}
