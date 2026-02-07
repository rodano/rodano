package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.EventModelDTO;

public interface EventModelService {

	List<EventModelDTO> getEventModels(UUID projectId);

	EventModelDTO getEventModel(UUID projectId, UUID eventModelId);

	EventModelDTO createEventModel(UUID projectId, EventModelDTO eventModelDTO);

	EventModelDTO updateEventModel(UUID projectId, UUID eventModelId, EventModelDTO eventModelDTO);

	void deleteEventModel(UUID projectId, UUID eventModelId);
}
