package ch.rodano.core.helpers;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.user.UserService;

@Service
public class UserCreatorService {

	public record UserCreation(
		User user,
		List<Pair<Profile, Scope>> roles
	) {}

	private final UserService userService;
	private final RoleService roleService;

	public UserCreatorService(
		final UserService userService,
		final RoleService roleService
	) {
		this.userService = userService;
		this.roleService = roleService;
	}

	public User createAndEnable(final UserCreation userAndRoles, final DatabaseActionContext context) {
		final var user = userAndRoles.user;
		userService.saveUser(user, context, "Create user");

		for(final var role : userAndRoles.roles) {
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

	public void batchCreateAndEnable(final List<UserCreation> usersAndRoles, final DatabaseActionContext context) {
		for(final var userRoles : usersAndRoles) {
			createAndEnable(userRoles, context);
		}
	}
}
