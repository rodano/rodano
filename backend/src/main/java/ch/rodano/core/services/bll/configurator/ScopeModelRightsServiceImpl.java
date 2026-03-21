package ch.rodano.core.services.bll.configurator;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.EntityRightDTO;
import ch.rodano.core.services.dao.configurator.ScopeModelRightsDAOService;

@Service
@Transactional
public class ScopeModelRightsServiceImpl implements ScopeModelRightsService {

	private final ScopeModelRightsDAOService scopeModelRightsDAOService;

	public ScopeModelRightsServiceImpl(final ScopeModelRightsDAOService scopeModelRightsDAOService) {
		this.scopeModelRightsDAOService = scopeModelRightsDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, Map<UUID, EntityRightDTO>> getScopeModelRights(final UUID projectId) {
		return scopeModelRightsDAOService.getScopeModelRights(projectId);
	}

	@Override
	public void saveScopeModelRights(final UUID projectId, final Map<UUID, Map<UUID, EntityRightDTO>> rights) {
		scopeModelRightsDAOService.saveScopeModelRights(projectId, rights);
	}
}
