package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.ScopeModelDTO;

public interface ScopeModelDAOService {

	List<ScopeModelDTO> getScopeModels(UUID projectId);

	ScopeModelDTO getScopeModel(UUID projectId, UUID scopeModelId);

	ScopeModelDTO createScopeModel(UUID projectId, ScopeModelDTO scopeModel);

	ScopeModelDTO updateScopeModel(UUID projectId, UUID scopeModelId, ScopeModelDTO scopeModel);

	void deleteScopeModel(UUID projectId, UUID scopeModelId);
}
