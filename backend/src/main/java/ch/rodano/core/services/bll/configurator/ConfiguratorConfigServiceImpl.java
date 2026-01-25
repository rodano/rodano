package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.ScopeModelDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.ConfiguratorConfigDAOService;

@Service
@Transactional
public class ConfiguratorConfigServiceImpl implements ConfiguratorConfigService {

	private final ConfiguratorConfigDAOService configuratorConfigDAOService;

	public ConfiguratorConfigServiceImpl(final ConfiguratorConfigDAOService configuratorConfigDAOService) {
		this.configuratorConfigDAOService = configuratorConfigDAOService;
	}

	@Override
	public List<ScopeModelDTO> getScopeModels(final UUID projectId) {
		return configuratorConfigDAOService.getScopeModels(projectId);
	}

	@Override
	public ScopeModelDTO getScopeModel(final UUID projectId, final UUID scopeModelId) {
		final var scopeModel = configuratorConfigDAOService.getScopeModel(projectId, scopeModelId);
		if(scopeModel == null) {
			throw new NotFoundException("Scope model not found: " + scopeModelId);
		}
		return scopeModel;
	}

	@Override
	public ScopeModelDTO createScopeModel(final UUID projectId, final ScopeModelDTO scopeModel) {
		if(scopeModel.getId() == null || scopeModel.getId().isBlank()) {
			throw new IllegalArgumentException("Scope model code is required");
		}

		if(scopeModel.getShortname() == null || scopeModel.getShortname().isEmpty()) {
			throw new IllegalArgumentException("Scope model shortname is required");
		}

		return configuratorConfigDAOService.createScopeModel(projectId, scopeModel);
	}

	@Override
	public ScopeModelDTO updateScopeModel(final UUID projectId, final UUID scopeModelId, final ScopeModelDTO scopeModel) {
		final var existing = configuratorConfigDAOService.getScopeModel(projectId, scopeModelId);
		if(existing == null) {
			throw new NotFoundException("Scope model not found: " + scopeModelId);
		}

		if(scopeModel.getId() == null || scopeModel.getId().isBlank()) {
			throw new IllegalArgumentException("Scope model code is required");
		}

		if(scopeModel.getShortname() == null || scopeModel.getShortname().isEmpty()) {
			throw new IllegalArgumentException("Scope model shortname is required");
		}

		return configuratorConfigDAOService.updateScopeModel(projectId, scopeModelId, scopeModel);
	}

	@Override
	public void deleteScopeModel(final UUID projectId, final UUID scopeModelId) {
		final var existing = configuratorConfigDAOService.getScopeModel(projectId, scopeModelId);
		if(existing == null) {
			throw new NotFoundException("Scope model not found: " + scopeModelId);
		}

		configuratorConfigDAOService.deleteScopeModel(projectId, scopeModelId);
	}
}
