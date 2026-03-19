package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.MenuConfigDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.MenuConfigDAOService;

@Service
@Transactional
public class MenuConfigServiceImpl implements MenuConfigService {

	private final MenuConfigDAOService menuConfigDAOService;

	public MenuConfigServiceImpl(final MenuConfigDAOService menuConfigDAOService) {
		this.menuConfigDAOService = menuConfigDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<MenuConfigDTO> getMenus(final UUID projectId) {
		return menuConfigDAOService.getMenus(projectId);
	}

	@Override
	@Transactional(readOnly = true)
	public MenuConfigDTO getMenu(final UUID projectId, final UUID menuId) {
		final var menu = menuConfigDAOService.getMenu(projectId, menuId);
		if(menu == null) {
			throw new NotFoundException("Menu not found: " + menuId);
		}
		return menu;
	}

	@Override
	public MenuConfigDTO createMenu(final UUID projectId, final MenuConfigDTO dto) {
		return menuConfigDAOService.createMenu(projectId, dto);
	}

	@Override
	public MenuConfigDTO updateMenu(final UUID projectId, final UUID menuId, final MenuConfigDTO dto) {
		final var existing = menuConfigDAOService.getMenu(projectId, menuId);
		if(existing == null) {
			throw new NotFoundException("Menu not found: " + menuId);
		}
		return menuConfigDAOService.updateMenu(projectId, menuId, dto);
	}

	@Override
	public void deleteMenu(final UUID projectId, final UUID menuId) {
		final var existing = menuConfigDAOService.getMenu(projectId, menuId);
		if(existing == null) {
			throw new NotFoundException("Menu not found: " + menuId);
		}
		menuConfigDAOService.deleteMenu(projectId, menuId);
	}
}
