package ch.rodano.api.config;

import java.util.List;

import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.menu.Menu;
import ch.rodano.core.utils.ACL;

@Service
public class MenuDTOServiceImpl implements MenuDTOService {

	@Override
	public MenuDTO createDTO(final Menu menu, final ACL acl) {

		// Get the submenus to which the user has rights
		final var submenus = menu.getSubmenus().stream()
			.filter(m -> acl.hasRight(m))
			.map(m -> createDTO(m, acl))
			.toList();

		// If submenus are present, automatically link first level action to the first second level menu action
		final var action = !submenus.isEmpty() ? submenus.get(0).action() : createActionDTO(menu);

		return new MenuDTO(
			menu.getId(),
			menu.getShortname(),
			menu.getLongname(),
			menu.getDescription(),
			action,
			menu.getOrderBy(),
			menu.isPublic(),
			menu.isHomePage(),
			submenus
		);
	}

	private MenuActionDTO createActionDTO(final Menu menu) {
		final var action = menu.getAction();

		// Automatically add context for menu with embedded layout
		final List<String> context;
		if((action.getContext() == null || action.getContext().isEmpty()) && menu.getLayout() != null) {
			context = List.of(menu.getId());
		}
		else {
			context = action.getContext();
		}

		return new MenuActionDTO(
			action.getId(),
			action.getLabels(),
			action.getPage(),
			context,
			action.getParameters(),
			action.getSection()
		);
	}

}
