package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.EventModelDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.EventModelDAOService;

@Service
@Transactional
public class EventModelServiceImpl implements EventModelService {

	private final EventModelDAOService eventModelDAOService;

	public EventModelServiceImpl(final EventModelDAOService eventModelDAOService) {
		this.eventModelDAOService = eventModelDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<EventModelDTO> getEventModels(final UUID projectId, final String view) {
		return eventModelDAOService.getEventModels(projectId, view);
	}

	@Override
	@Transactional(readOnly = true)
	public EventModelDTO getEventModel(final UUID projectId, final UUID eventModelId) {
		final var eventModel = eventModelDAOService.getEventModel(projectId, eventModelId);
		if(eventModel == null) {
			throw new NotFoundException("Event model not found:  " + eventModelId);
		}
		return eventModel;
	}

	@Override
	public EventModelDTO createEventModel(final UUID projectId, final EventModelDTO eventModel) {
		return eventModelDAOService.createEventModel(projectId, eventModel);
	}

	@Override
	public EventModelDTO updateEventModel(final UUID projectId, final UUID eventModelId, final EventModelDTO eventModel) {
		final var existing = eventModelDAOService.getEventModel(projectId, eventModelId);
		if(existing == null) {
			throw new NotFoundException("Event model not found:  " + eventModelId);
		}
		return eventModelDAOService.updateEventModel(projectId, eventModelId, eventModel);
	}

	@Override
	public void deleteEventModel(final UUID projectId, final UUID eventModelId) {
		final var existing = eventModelDAOService.getEventModel(projectId, eventModelId);
		if(existing == null) {
			throw new NotFoundException("Event model not found:  " + eventModelId);
		}
		eventModelDAOService.deleteEventModel(projectId, eventModelId);
	}
}
