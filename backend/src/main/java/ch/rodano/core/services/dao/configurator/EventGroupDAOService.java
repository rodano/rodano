package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.EventGroupDTO;

public interface EventGroupDAOService {

	List<EventGroupDTO> getEventGroups(UUID projectId);

	EventGroupDTO getEventGroup(UUID projectId, UUID eventGroupId);

	EventGroupDTO createEventGroup(UUID projectId, EventGroupDTO eventGroup);

	EventGroupDTO updateEventGroup(UUID projectId, UUID eventGroupId, EventGroupDTO eventGroup);

	void deleteEventGroup(UUID projectId, UUID eventGroupId);
}
