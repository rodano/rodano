package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.MenuConfigDTO;

public interface MenuConfigService {

	List<MenuConfigDTO> getMenus(UUID projectId);

	MenuConfigDTO getMenu(UUID projectId, UUID menuId);

	MenuConfigDTO createMenu(UUID projectId, MenuConfigDTO dto);

	MenuConfigDTO updateMenu(UUID projectId, UUID menuId, MenuConfigDTO dto);

	void deleteMenu(UUID projectId, UUID menuId);
}
