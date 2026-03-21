package ch.rodano.core.services.bll.configurator;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.EntityRightDTO;
import ch.rodano.core.services.dao.configurator.EventModelRightsDAOService;

@Service
@Transactional
public class EventModelRightsServiceImpl implements EventModelRightsService {

	private final EventModelRightsDAOService eventModelRightsDAOService;

	public EventModelRightsServiceImpl(final EventModelRightsDAOService eventModelRightsDAOService) {
		this.eventModelRightsDAOService = eventModelRightsDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, Map<UUID, EntityRightDTO>> getEventModelRights(final UUID projectId) {
		return eventModelRightsDAOService.getEventModelRights(projectId);
	}

	@Override
	public void saveEventModelRights(final UUID projectId, final Map<UUID, Map<UUID, EntityRightDTO>> rights) {
		eventModelRightsDAOService.saveEventModelRights(projectId, rights);
	}
}
