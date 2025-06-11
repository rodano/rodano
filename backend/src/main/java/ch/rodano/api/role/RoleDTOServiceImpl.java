package ch.rodano.api.role;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ch.rodano.api.config.ProfileDTO;
import ch.rodano.api.scope.ScopeMiniDTO;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.role.RoleStatus;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;

@Service
public class RoleDTOServiceImpl implements RoleDTOService {

	private final ScopeDAOService scopeDAOService;
	private final RoleService roleService;
	private final RightsService rightsService;

	public RoleDTOServiceImpl(
		final ScopeDAOService scopeService,
		final RoleService roleService,
		final RightsService rightsService
	) {
		this.scopeDAOService = scopeService;
		this.roleService = roleService;
		this.rightsService = rightsService;
	}

	@Override
	public Map<User, List<RoleDTO>> createDTOs(final Map<User, List<Role>> userRoles, final Actor currentActor, final List<Role> currentRoles) {
		if(userRoles.isEmpty()) {
			return Collections.emptyMap();
		}
		final List<Long> scopePks = userRoles.entrySet().stream().flatMap(e -> e.getValue().stream()).map(Role::getScopeFk).toList();
		//retrieve all scopes for the selected roles
		final var scopesByPk = scopeDAOService.getScopesByPks(scopePks)
			.stream()
			.collect(Collectors.toMap(Scope::getPk, Function.identity()));

		final var userRoleDTOs = new HashMap<User, List<RoleDTO>>();
		for(final var entry : userRoles.entrySet()) {
			final var user = entry.getKey();
			final List<RoleDTO> roleDTOs = entry.getValue().stream()
				.filter(r -> rightsService.hasRightOnActorRole(currentActor, currentRoles, user, r, Rights.READ))
				.map(r -> createDTO(user, r, scopesByPk.get(r.getScopeFk()), currentActor, currentRoles))
				.toList();
			userRoleDTOs.put(user, roleDTOs);
		}
		return userRoleDTOs;
	}

	@Override
	public RoleDTO createDTO(final Actor actor, final Role role, final Actor currentActor, final List<Role> currentRoles) {
		return createDTO(actor, role, scopeDAOService.getScopeByPk(role.getScopeFk()), currentActor, currentRoles);
	}

	private RoleDTO createDTO(
		final Actor actor,
		final Role role,
		final Scope scope,
		final Actor currentActor,
		final List<Role> currentRoles
	) {
		final var dto = new RoleDTO();

		dto.pk = role.getPk();

		dto.userPk = role.getUserFk();
		dto.robotPk = role.getRobotFk();

		dto.scopePk = role.getScopeFk();
		dto.scope = new ScopeMiniDTO(scope);

		dto.profileId = role.getProfileId();
		dto.profile = new ProfileDTO(role.getProfile());

		dto.status = role.getStatus();
		dto.enabled = role.isEnabled();

		final var isHimself = currentActor.getPk().equals(actor.getPk());
		final var hasRightOnRole = rightsService.hasRightOnActorRole(currentActor, currentRoles, actor, role, Rights.WRITE);
		final var hasPrivacyPolicies = !role.getProfile().getPrivacyPolicies().isEmpty();

		dto.canEnable = isHimself && role.getStatus().equals(RoleStatus.PENDING) || hasRightOnRole && !hasPrivacyPolicies && role.getStatus() != RoleStatus.ENABLED && actor.isActivated();
		dto.canDisable = !isHimself && hasRightOnRole && role.getStatus() == RoleStatus.ENABLED && roleService.getActiveRoles(actor).size() > 1;

		return dto;
	}
}
