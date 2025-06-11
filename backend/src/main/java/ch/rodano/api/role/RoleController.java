package ch.rodano.api.role;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.api.utils.URLConsistencyUtils;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.exception.UnauthorizedException;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.role.RoleDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.core.utils.RightsService;
import ch.rodano.core.utils.UtilsService;

@Tag(name = "Role")
@RestController
@RequestMapping("/users/{userPk}/roles")
@Validated
@Transactional(readOnly = true)
public class RoleController extends AbstractSecuredController {

	private final UserDAOService userDAOService;
	private final RoleDAOService roleDAOService;
	private final RoleDTOService roleDTOService;
	private final UtilsService utilsService;
	private final ScopeDAOService scopeDAOService;

	public RoleController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final UserDAOService userDAOService,
		final RoleDAOService roleDAOService,
		final RoleDTOService roleDTOService,
		final UtilsService utilsService,
		final ScopeDAOService scopeDAOService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.userDAOService = userDAOService;
		this.roleDAOService = roleDAOService;
		this.roleDTOService = roleDTOService;
		this.utilsService = utilsService;
		this.scopeDAOService = scopeDAOService;
	}

	@Operation(summary = "Get all roles for a user")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<RoleDTO> getUserRoles(
		@PathVariable final Long userPk
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		final var user = userDAOService.getUserByPk(userPk);
		return roleService.getRoles(user).stream()
			.filter(role -> rightsService.hasRightOnActorRole(currentActor, currentRoles, user, role, Rights.READ))
			.map(role -> roleDTOService.createDTO(user, role, currentActor, currentRoles))
			.toList();
	}

	@Operation(summary = "Enable role")
	@PutMapping("{rolePk}/enable")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public RoleDTO enableRole(
		@PathVariable final Long userPk,
		@PathVariable final Long rolePk
	) {
		final var user = userDAOService.getUserByPk(userPk);
		final var role = roleDAOService.getRoleByPk(rolePk);

		utilsService.checkNotNull(User.class, user, userPk);
		utilsService.checkNotNull(Role.class, role, rolePk);

		URLConsistencyUtils.checkConsistency(user, role);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();

		if(!rightsService.hasRightOnActorRole(currentActor, currentRoles, user, role, Rights.WRITE)) {
			throw new UnauthorizedException("You don't have rights to modify this role");
		}

		// If the role has privacy policies and the current user is not the user then it can not be enabled.
		if(
			!currentActor.getPk().equals(user.getPk()) &&
			!role.getProfile().getPrivacyPolicies().isEmpty()
		) {
			throw new UnauthorizedException("The user's role cannot be enabled because the associated profile has a privacy policy");
		}

		roleService.enableRole(user, role, currentContext());
		return roleDTOService.createDTO(user, role, currentActor, currentRoles);
	}

	@Operation(summary = "Disable role")
	@PutMapping("{rolePk}/disable")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public RoleDTO disableRole(
		@PathVariable final Long userPk,
		@PathVariable final Long rolePk
	) {
		final var user = userDAOService.getUserByPk(userPk);
		final var role = roleDAOService.getRoleByPk(rolePk);

		utilsService.checkNotNull(User.class, user, userPk);
		utilsService.checkNotNull(Role.class, role, rolePk);

		URLConsistencyUtils.checkConsistency(user, role);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();

		if(!rightsService.hasRightOnActorRole(currentActor, currentRoles, user, role, Rights.WRITE)) {
			throw new UnauthorizedException("You don't have rights to modify this role");
		}

		roleService.disableRole(user, role, currentContext());
		return roleDTOService.createDTO(user, role, currentActor, currentRoles);
	}

	@Operation(summary = "Reject role")
	@PutMapping("{rolePk}/reject")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public RoleDTO rejectRole(
		@PathVariable final Long userPk,
		@PathVariable final Long rolePk
	) {
		final var user = userDAOService.getUserByPk(userPk);
		final var role = roleDAOService.getRoleByPk(rolePk);

		utilsService.checkNotNull(User.class, user, userPk);
		utilsService.checkNotNull(Role.class, role, rolePk);

		URLConsistencyUtils.checkConsistency(user, role);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();

		if(!currentActor.getPk().equals(user.getPk())) {
			throw new UnauthorizedException("You don't have rights to reject this role");
		}

		roleService.rejectRole(user, role, currentContext());
		return roleDTOService.createDTO(user, role, currentActor, currentRoles);
	}


	@Operation(summary = "Create role")
	@PostMapping()
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public RoleDTO createRole(
		@PathVariable final Long userPk,
		@Valid @RequestBody final RoleCreationDTO roleCreationDTO
	) {
		final var user = userDAOService.getUserByPk(userPk);
		final var scope = scopeDAOService.getScopeByPk(roleCreationDTO.getScopePk());
		final var profile = studyService.getStudy().getProfile(roleCreationDTO.getProfileId());

		utilsService.checkNotNull(Scope.class, scope, roleCreationDTO.getScopePk());
		utilsService.checkNotNull(User.class, user, userPk);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scope);

		rightsService.checkRight(currentActor, currentRoles, profile, Rights.WRITE);

		final var newRole = roleService.createRole(
			user,
			profile,
			scope,
			currentContext()
		);

		return roleDTOService.createDTO(user, newRole, currentActor, currentRoles);
	}
}
