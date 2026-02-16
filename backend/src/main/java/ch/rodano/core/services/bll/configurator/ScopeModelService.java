package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.ScopeModelDTO;

public interface ScopeModelService {

	List<ScopeModelDTO> getScopeModels(UUID projectId, String view);

	default List<ScopeModelDTO> getScopeModels(final UUID projectId) {
		return getScopeModels(projectId, "summary");
	}

	ScopeModelDTO getScopeModel(UUID projectId, UUID scopeModelId);

	ScopeModelDTO createScopeModel(UUID projectId, ScopeModelDTO scopeModel);

	ScopeModelDTO updateScopeModel(UUID projectId, UUID scopeModelId, ScopeModelDTO scopeModel);

	void deleteScopeModel(UUID projectId, UUID scopeModelId);
}
