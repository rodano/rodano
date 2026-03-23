package ch.rodano.core.services.bll.configurator;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.WidgetLayoutDTO;
import ch.rodano.core.services.dao.configurator.MenuLayoutDAOService;

@Service
@Transactional
public class MenuLayoutServiceImpl implements MenuLayoutService {

	private final MenuLayoutDAOService menuLayoutDAOService;

	public MenuLayoutServiceImpl(final MenuLayoutDAOService menuLayoutDAOService) {
		this.menuLayoutDAOService = menuLayoutDAOService;
	}

	@Override
	public WidgetLayoutDTO getLayout(final UUID projectId, final UUID menuId) {
		return menuLayoutDAOService.getLayout(projectId, menuId);
	}

	@Override
	public WidgetLayoutDTO saveLayout(final UUID projectId, final UUID menuId, final WidgetLayoutDTO dto) {
		return menuLayoutDAOService.saveLayout(projectId, menuId, dto);
	}
}
