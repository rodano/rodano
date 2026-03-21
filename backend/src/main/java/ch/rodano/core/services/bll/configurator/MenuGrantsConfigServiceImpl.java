package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.services.dao.configurator.MenuGrantsConfigDAOService;

@Service
@Transactional
public class MenuGrantsConfigServiceImpl implements MenuGrantsConfigService {

	private final MenuGrantsConfigDAOService menuGrantsConfigDAOService;

	public MenuGrantsConfigServiceImpl(final MenuGrantsConfigDAOService menuGrantsConfigDAOService) {
		this.menuGrantsConfigDAOService = menuGrantsConfigDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, List<UUID>> getMenuGrants(final UUID projectId) {
		return menuGrantsConfigDAOService.getMenuGrants(projectId);
	}

	@Override
	public void saveMenuGrants(final UUID projectId, final Map<UUID, List<UUID>> profileMenuMap) {
		menuGrantsConfigDAOService.saveMenuGrants(projectId, profileMenuMap);
	}
}
