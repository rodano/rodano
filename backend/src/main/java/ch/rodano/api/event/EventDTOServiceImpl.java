package ch.rodano.api.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ch.rodano.api.config.EventModelDTO;
import ch.rodano.api.workflow.WorkflowDTOService;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.workflowStatus.AggregateWorkflowDAOService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.dao.workflow.WorkflowStatusDAOService;
import ch.rodano.core.utils.ACL;

@Service
public class EventDTOServiceImpl implements EventDTOService {

	private final ActorService actorService;
	private final EventService eventService;
	private final WorkflowStatusDAOService workflowStatusDAOService;
	private final AggregateWorkflowDAOService aggregateWorkflowDAOService;
	private final WorkflowDTOService workflowDTOService;

	public EventDTOServiceImpl(
		final ActorService actorService,
		final EventService eventService,
		final WorkflowStatusDAOService workflowStatusDAOService,
		final AggregateWorkflowDAOService aggregateWorkflowDAOService,
		final WorkflowDTOService workflowDTOService
	) {
		this.actorService = actorService;
		this.eventService = eventService;
		this.workflowStatusDAOService = workflowStatusDAOService;
		this.aggregateWorkflowDAOService = aggregateWorkflowDAOService;
		this.workflowDTOService = workflowDTOService;
	}

	@Override
	public List<EventDTO> createDTOs(final Scope scope, final Collection<Event> events, final ACL acl) {
		if(events.isEmpty()) {
			return Collections.emptyList();
		}
		final var eventPks = events.stream().map(Event::getPk).toList();
		final var workflowStatuses = new ArrayList<WorkflowStatus>();
		//retrieve all workflow statuses for the selected events
		workflowStatuses.addAll(workflowStatusDAOService.getWorkflowStatusesByEventPks(eventPks));
		//retrieve all aggregated workflow statuses for the selected events
		workflowStatuses.addAll(aggregateWorkflowDAOService.getAggregateWorkflowStatusByEvents(events));
		//group all workflow statuses by event pks
		final var workflowStatusesByEventPk = workflowStatuses.stream().collect(Collectors.groupingBy(WorkflowStatus::getEventFk));

		return events.stream()
			.map(
				event -> createDTO(
					scope,
					event,
					acl,
					workflowStatusesByEventPk.getOrDefault(event.getPk(), Collections.emptyList())
				)
			)
			.toList();
	}

	@Override
	public EventDTO createDTO(final Scope scope, final Event event, final ACL acl) {
		final var workflowStatuses = new ArrayList<WorkflowStatus>();
		workflowStatuses.addAll(workflowStatusDAOService.getWorkflowStatusesByEventPk(event.getPk()));
		workflowStatuses.addAll(aggregateWorkflowDAOService.getAggregateWorkflowStatusByEvent(event));
		return createDTO(scope, event, acl, workflowStatuses);
	}

	private EventDTO createDTO(
		final Scope scope,
		final Event event,
		final ACL acl,
		final List<WorkflowStatus> workflowStatuses
	) {
		final var languages = actorService.getLanguages(acl.actor());
		final var model = event.getEventModel();

		final var dto = new EventDTO();

		dto.scopePk = scope.getPk();
		dto.scopeId = scope.getId();
		dto.scopeCodeAndShortname = scope.getCodeAndShortname();

		dto.pk = event.getPk();
		dto.id = event.getId();
		dto.eventGroupNumber = event.getEventGroupNumber();

		dto.creationTime = event.getCreationTime();
		dto.lastUpdateTime = event.getLastUpdateTime();

		dto.model = new EventModelDTO(model);
		dto.modelId = event.getEventModelId();

		dto.removed = event.getDeleted();
		dto.inRemoved = scope.getDeleted();

		dto.locked = event.getLocked();
		dto.inLocked = scope.getLocked();

		dto.canWrite = !dto.inRemoved && !scope.getLocked() && !event.getLocked() && acl.hasRight(model, Rights.WRITE);
		dto.canBeRemoved = dto.canWrite && !model.isPreventAdd();

		dto.shortname = event.getEventModel().getLocalizedShortname(languages);
		dto.longname = eventService.getLabel(scope, event, languages);

		dto.expectedDate = event.getExpectedDate();
		dto.date = event.getDate();
		dto.endDate = event.getEndDate();

		dto.mandatory = model.isMandatory();
		dto.expected = event.isExpected();
		dto.notDone = event.getNotDone();
		dto.blocking = event.getBlocking();

		//workflow statuses
		dto.workflowStatuses = new ArrayList<>();
		dto.possibleWorkflows = new ArrayList<>();

		final var family = new DataFamily(scope, event);
		final var workflowComparator = Workflow.getWorkflowableComparator(model);
		dto.workflowStatuses = workflowStatuses
			.stream()
			.filter(w -> acl.hasRight(w.getWorkflow()))
			.sorted(WorkflowStatus.proxyComparator(workflowComparator))
			.map(ws -> workflowDTOService.createWorkflowStatusDTO(family, ws, acl))
			.toList();

		//available workflow creation actions
		dto.possibleWorkflows = model.getWorkflows()
			.stream()
			.filter(w -> !w.isMandatory() && w.getActionId() != null)
			.filter(w -> !w.isUnique() || dto.workflowStatuses.stream().noneMatch(ws -> ws.getWorkflowId().equals(w.getId())))
			.filter(w -> acl.hasRight(w.getAction()))
			.sorted(workflowComparator)
			.map(w -> workflowDTOService.createWorkflowDTO(w, acl))
			.toList();

		return dto;
	}

}
