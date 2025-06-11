package ch.rodano.api.role;

import java.util.List;
import java.util.Map;

import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.user.User;

public interface RoleDTOService {

	Map<User, List<RoleDTO>> createDTOs(Map<User, List<Role>> userRoles, Actor currentActor, List<Role> currentRoles);

	RoleDTO createDTO(Actor actor, Role role, Actor currentActor, List<Role> currentRoles);
}
