package ch.rodano.api.config;

import ch.rodano.configuration.model.menu.Menu;
import ch.rodano.core.utils.ACL;

public interface MenuDTOService {

	MenuDTO createDTO(Menu menu, ACL acl);
}
