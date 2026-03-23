package ch.rodano.core.services.bll.configurator;

import java.util.UUID;

import ch.rodano.api.config.WidgetLayoutDTO;

public interface ScopeModelLayoutService {

	WidgetLayoutDTO getLayout(UUID projectId, UUID scopeModelId);

	WidgetLayoutDTO saveLayout(UUID projectId, UUID scopeModelId, WidgetLayoutDTO dto);
}
