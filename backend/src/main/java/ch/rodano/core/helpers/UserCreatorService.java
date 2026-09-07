package ch.rodano.core.helpers;

import java.util.List;

import org.springframework.stereotype.Service;

import ch.rodano.core.helpers.builder.UserBuilder;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.user.UserService;

@Service
public class UserCreatorService {

	private final UserService userService;
	private final RoleService roleService;

	public UserCreatorService(
		final UserService userService,
		final RoleService roleService
	) {
		this.userService = userService;
		this.roleService = roleService;
	}

	public User createAndEnable(final UserBuilder builder, final DatabaseActionContext context) {
		final var user = builder.getUser();
		userService.saveUser(user, context, "Create user");

		for(final var role : builder.getRoles()) {
			final var newRole = roleService.createRole(
				user,
				role.getLeft(),
				role.getRight(),
				context
			);
			roleService.enableRole(
				user,
				newRole,
				context
			);
		}

		return user;
	}

	public List<User> batchCreateAndEnable(final List<UserBuilder> builders, final DatabaseActionContext context) {
		return builders.stream().map(builder -> createAndEnable(builder, context)).toList();
	}
}
