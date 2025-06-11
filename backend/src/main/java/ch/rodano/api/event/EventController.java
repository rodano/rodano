package ch.rodano.api.event;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.dto.RationaleDTO;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.api.utils.URLConsistencyUtils;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.event.EventDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;
import ch.rodano.core.utils.UtilsService;

@Tag(name = "Event")
@RestController
@RequestMapping("/scopes/{scopePk}/events")
@Validated
@Transactional(readOnly = true)
public class EventController extends AbstractSecuredController {

	private final ScopeDAOService scopeDAOService;
	private final EventDAOService eventDAOService;
	private final EventService eventService;
	private final EventDTOService eventDTOService;
	private final UtilsService utilsService;

	public EventController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final ScopeDAOService scopeDAOService,
		final EventDAOService eventDAOService,
		final EventService eventService,
		final EventDTOService eventDTOService,
		final UtilsService utilsService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.scopeDAOService = scopeDAOService;
		this.eventDAOService = eventDAOService;
		this.eventService = eventService;
		this.eventDTOService = eventDTOService;
		this.utilsService = utilsService;
	}

	@Operation(summary = "Get events")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<EventDTO> getScopeEvents(
		@PathVariable final Long scopePk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);

		final var events = eventService.search(scope, acl);
		return eventDTOService.createDTOs(scope, events, acl);
	}

	@Operation(summary = "Get event")
	@GetMapping("{eventPk}")
	@ResponseStatus(HttpStatus.OK)
	public EventDTO getEvent(
		@PathVariable final Long scopePk,
		@PathVariable final Long eventPk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventDAOService.getEventByPk(eventPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		URLConsistencyUtils.checkConsistency(scope, event);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);
		acl.checkRight(event.getEventModel(), Rights.READ);

		return eventDTOService.createDTO(scope, event, acl);
	}

	@Operation(summary = "Remove event")
	@PutMapping("{eventPk}/remove")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public EventDTO removeEvent(
		@PathVariable final Long scopePk,
		@PathVariable final Long eventPk,
		@Valid @RequestBody final RationaleDTO rationale
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventDAOService.getEventByPk(eventPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		URLConsistencyUtils.checkConsistency(scope, event);

		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(event.getEventModel(), Rights.WRITE);

		// Check if the event is mandatory
		if(event.getEventModel().isMandatory()) {
			throw new MandatoryEventRemovalException();
		}

		eventService.delete(scope, event, currentContext(), rationale.message());

		return eventDTOService.createDTO(scope, event, acl);
	}

	@Operation(summary = "Restore event")
	@PutMapping("{eventPk}/restore")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public EventDTO restoreEvent(
		@PathVariable final Long scopePk,
		@PathVariable final Long eventPk,
		@Valid @RequestBody final RationaleDTO rationale
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventDAOService.getEventByPk(eventPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		URLConsistencyUtils.checkConsistency(scope, event);

		final var acl = rightsService.getACL(currentActor(), scope);

		acl.checkRight(FeatureStatic.MANAGE_DELETED_DATA);
		//no need to check if actor has the right to write the scope and the event group (MANAGE_DELETED_DATA is sufficient and surpass WRITE scope model right and WRITE event group)

		eventService.restore(scope, event, currentContext(), rationale.message());

		return eventDTOService.createDTO(scope, event, acl);
	}

	@Operation(summary = "Create event", description = "Create a new event group for the selected scope")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public EventDTO createEvent(
		@PathVariable final Long scopePk,
		@RequestParam final String eventModelId
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		final var eventModel = scope.getScopeModel().getEventModel(eventModelId);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(eventModel, Rights.WRITE);

		// Create event group
		final var event = eventService.create(scope, eventModel, currentContext(), null);

		return eventDTOService.createDTO(scope, event, acl);
	}

	@Operation(summary = "Lock event")
	@PutMapping("{eventPk}/lock")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public EventDTO lockEvent(
		@PathVariable final Long scopePk,
		@PathVariable final Long eventPk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventDAOService.getEventByPk(eventPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);

		URLConsistencyUtils.checkConsistency(scope, event);

		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(FeatureStatic.LOCK);

		eventService.lock(scope, event, currentContext(), "Lock event");

		return eventDTOService.createDTO(scope, event, acl);
	}

	@Operation(summary = "Unlock event")
	@PutMapping("{eventPk}/unlock")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public EventDTO unlockEvent(
		@PathVariable final Long scopePk,
		@PathVariable final Long eventPk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventDAOService.getEventByPk(eventPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		URLConsistencyUtils.checkConsistency(scope, event);

		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(FeatureStatic.LOCK);

		eventService.unlock(scope, event, currentContext(), "Unlock event");

		return eventDTOService.createDTO(scope, event, acl);
	}
}
