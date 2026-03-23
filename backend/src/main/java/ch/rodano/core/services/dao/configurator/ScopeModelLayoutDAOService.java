package ch.rodano.core.services.dao.configurator;

import java.util.UUID;

import ch.rodano.api.config.WidgetLayoutDTO;

public interface ScopeModelLayoutDAOService {

	WidgetLayoutDTO getLayout(UUID projectId, UUID scopeModelId);

	WidgetLayoutDTO saveLayout(UUID projectId, UUID scopeModelId, WidgetLayoutDTO dto);
}
