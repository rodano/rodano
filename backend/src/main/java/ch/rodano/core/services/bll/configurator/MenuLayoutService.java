package ch.rodano.core.services.bll.configurator;

import java.util.UUID;

import ch.rodano.api.config.WidgetLayoutDTO;

public interface MenuLayoutService {

	WidgetLayoutDTO getLayout(UUID projectId, UUID menuId);

	WidgetLayoutDTO saveLayout(UUID projectId, UUID menuId, WidgetLayoutDTO dto);
}
