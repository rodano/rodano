package ch.rodano.core.services.bll.configurator;


import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.EventGroupDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.EventGroupDAOService;

@Service
@Transactional
public class EventGroupServiceImpl implements EventGroupService {

	private final EventGroupDAOService eventGroupDAOService;

	public EventGroupServiceImpl(final EventGroupDAOService eventGroupDAOService) {
		this.eventGroupDAOService = eventGroupDAOService;
	}

	@Override
	public List<EventGroupDTO> getEventGroups(final UUID projectId) {
		return eventGroupDAOService.getEventGroups(projectId);
	}

	@Override
	public EventGroupDTO getEventGroup(final UUID projectId, final UUID eventGroupId) {
		final var eventGroup = eventGroupDAOService.getEventGroup(projectId, eventGroupId);
		if(eventGroup == null) {
			throw new NotFoundException("Event group not found: " + eventGroupId);
		}
		return eventGroup;
	}

	@Override
	public EventGroupDTO createEventGroup(final UUID projectId, final EventGroupDTO eventGroup) {
		if(eventGroup.getId() == null || eventGroup.getId().isBlank()) {
			throw new IllegalArgumentException("Event group code is required");
		}
		if(eventGroup.getShortname() == null || eventGroup.getShortname().isEmpty()) {
			throw new IllegalArgumentException("Event group shortname is required");
		}

		return eventGroupDAOService.createEventGroup(projectId, eventGroup);
	}

	@Override
	public EventGroupDTO updateEventGroup(final UUID projectId, final UUID eventGroupId, final EventGroupDTO eventGroup) {
		final var existing = eventGroupDAOService.getEventGroup(projectId, eventGroupId);
		if(existing == null) {
			throw new NotFoundException("Event group not found: " + eventGroupId);
		}

		if(eventGroup.getId() == null || eventGroup.getId().isBlank()) {
			throw new IllegalArgumentException("Event group code is required");
		}
		if(eventGroup.getShortname() == null || eventGroup.getShortname().isEmpty()) {
			throw new IllegalArgumentException("Event group shortname is required");
		}

		return eventGroupDAOService.updateEventGroup(projectId, eventGroupId, eventGroup);
	}

	@Override
	public void deleteEventGroup(final UUID projectId, final UUID eventGroupId) {
		final var existing = eventGroupDAOService.getEventGroup(projectId, eventGroupId);
		if(existing == null) {
			throw new NotFoundException("Event group not found: " + eventGroupId);
		}

		eventGroupDAOService.deleteEventGroup(projectId, eventGroupId);
	}
}
