package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.ScopeModelDTO;

public interface ConfiguratorConfigService {

	List<ScopeModelDTO> getScopeModels(UUID projectId);

	ScopeModelDTO getScopeModel(UUID projectId, UUID scopeModelId);

	ScopeModelDTO createScopeModel(UUID projectId, ScopeModelDTO scopeModel);

	ScopeModelDTO updateScopeModel(UUID projectId, UUID scopeModelId, ScopeModelDTO scopeModel);

	void deleteScopeModel(UUID projectId, UUID scopeModelId);
}
