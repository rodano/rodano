package ch.rodano.core.services.dao.configurator;

import java.util.UUID;

import ch.rodano.api.config.WidgetLayoutDTO;

public interface MenuLayoutDAOService {

	WidgetLayoutDTO getLayout(UUID projectId, UUID menuId);

	WidgetLayoutDTO saveLayout(UUID projectId, UUID menuId, WidgetLayoutDTO dto);
}
