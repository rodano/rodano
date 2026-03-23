package ch.rodano.core.services.bll.configurator;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.WidgetLayoutDTO;
import ch.rodano.core.services.dao.configurator.ScopeModelLayoutDAOService;

@Service
@Transactional
public class ScopeModelLayoutServiceImpl implements ScopeModelLayoutService {

	private final ScopeModelLayoutDAOService scopeModelLayoutDAOService;

	public ScopeModelLayoutServiceImpl(final ScopeModelLayoutDAOService scopeModelLayoutDAOService) {
		this.scopeModelLayoutDAOService = scopeModelLayoutDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public WidgetLayoutDTO getLayout(final UUID projectId, final UUID scopeModelId) {
		return scopeModelLayoutDAOService.getLayout(projectId, scopeModelId);
	}

	@Override
	public WidgetLayoutDTO saveLayout(final UUID projectId, final UUID scopeModelId, final WidgetLayoutDTO dto) {
		return scopeModelLayoutDAOService.saveLayout(projectId, scopeModelId, dto);
	}
}
