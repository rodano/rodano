package ch.rodano.api.actor;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ch.rodano.api.role.RoleDTO;
import ch.rodano.api.role.RoleDTOService;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.core.services.bll.user.UserService;
import ch.rodano.core.services.dao.role.RoleDAOService;
import ch.rodano.core.utils.RightsService;

@Service
public class ActorDTOServiceImpl implements ActorDTOService {

	private final StudyService studyService;
	private final RoleService roleService;
	private final RoleDAOService roleDAOService;
	private final RoleDTOService roleDTOService;
	private final RightsService rightsService;

	public ActorDTOServiceImpl(
		final StudyService studyService,
		final RoleService roleService,
		final RoleDAOService roleDAOService,
		final RoleDTOService roleDTOService,
		final RightsService rightsService
	) {
		this.studyService = studyService;
		this.roleService = roleService;
		this.roleDAOService = roleDAOService;
		this.roleDTOService = roleDTOService;
		this.rightsService = rightsService;
	}

	@Override
	public Robot generateRobot(final RobotCreationDTO robotCreationDTO) {
		final var robot = new Robot();
		robot.setName(robotCreationDTO.name());

		if(StringUtils.isNotBlank(robotCreationDTO.key())) {
			robot.setKey(robotCreationDTO.key());
		}
		else {
			// If no key is set, generate a new one
			robot.setKey(generateKey());
		}

		return robot;
	}

	@Override
	public void updateRobot(final Robot robot, final RobotUpdateDTO robotDTO) {
		robot.setName(robotDTO.name());
		robot.setKey(robotDTO.key());
	}

	@Override
	public List<RobotDTO> createRobotDTOs(final Collection<Robot> robots, final Actor actor, final List<Role> roles) {
		if(robots.isEmpty()) {
			return Collections.emptyList();
		}
		final var robotPks = robots.stream().map(Robot::getPk).toList();
		//retrieve all roles for the selected robots
		final var rolesByRobotPk = roleDAOService.getRolesByRobotPks(robotPks)
			.stream()
			.collect(Collectors.groupingBy(Role::getRobotFk));

		return robots.stream()
			.map(
				r -> createRobotDTO(
					r,
					rolesByRobotPk.getOrDefault(r.getPk(), Collections.emptyList()),
					actor,
					roles
				)
			)
			.toList();
	}

	@Override
	public RobotDTO createRobotDTO(final Robot robot, final Actor actor, final List<Role> roles) {
		final var robotRoles = roleService.getRoles(robot);
		return createRobotDTO(robot, robotRoles, actor, roles);
	}

	private RobotDTO createRobotDTO(final Robot robot, final List<Role> robotRoles, final Actor actor, final List<Role> roles) {
		final var dto = new RobotDTO();
		dto.pk = robot.getPk();
		dto.creationTime = robot.getCreationTime();
		dto.lastUpdateTime = robot.getLastUpdateTime();
		dto.removed = robot.getDeleted();
		dto.name = robot.getName();

		dto.key = robot.getKey();

		dto.roles = robotRoles.stream()
			.filter(r -> rightsService.hasRightOnActorRole(actor, roles, robot, r, Rights.READ))
			.map(r -> roleDTOService.createDTO(robot, r, actor, roles))
			.toList();

		return dto;
	}

	@Override
	public List<UserDTO> createUserDTOs(final Collection<User> users, final Actor actor, final List<Role> roles) {
		if(users.isEmpty()) {
			return Collections.emptyList();
		}
		final var userPks = users.stream().map(User::getPk).toList();
		//retrieve all roles for the selected users
		final var rolesByUserPk = roleDAOService.getRolesByUserPks(userPks)
			.stream()
			.collect(Collectors.groupingBy(Role::getUserFk));
		//build all roles DTOs for the selected users
		final var rolesByUser = new HashMap<User, List<Role>>();
		for(final var user : users) {
			rolesByUser.put(user, rolesByUserPk.getOrDefault(user.getPk(), Collections.emptyList()));
		}
		final var roleDTOsByUser = roleDTOService.createDTOs(rolesByUser, actor, roles);

		return users.stream()
			.map(
				u -> createUserDTO(
					u,
					rolesByUserPk.getOrDefault(u.getPk(), Collections.emptyList()),
					roleDTOsByUser.getOrDefault(u, Collections.emptyList()),
					actor,
					roles
				)
			)
			.toList();
	}

	@Override
	public UserDTO createUserDTO(final User user, final Actor actor, final List<Role> roles) {
		final var userRoles = roleService.getRoles(user);
		final var userRoleDTOs = userRoles.stream()
			.filter(r -> rightsService.hasRightOnActorRole(actor, roles, user, r, Rights.READ))
			.map(r -> roleDTOService.createDTO(user, r, actor, roles))
			.toList();
		return createUserDTO(user, userRoles, userRoleDTOs, actor, roles);
	}

	private UserDTO createUserDTO(final User user, final List<Role> userRoles, final List<RoleDTO> userRoleDTOs, final Actor actor, final List<Role> roles) {
		final var dto = new UserDTO();
		dto.pk = user.getPk();
		dto.creationTime = user.getCreationTime();
		dto.lastUpdateTime = user.getLastUpdateTime();
		dto.removed = user.getDeleted();
		dto.name = user.getName();

		dto.email = user.getEmail();
		dto.externallyManaged = user.isExternallyManaged();
		dto.activated = user.isActivated();
		dto.phone = user.getPhone();

		dto.roles = userRoleDTOs;

		final var userActiveRoles = userRoles.stream().filter(Role::isEnabled).toList();

		dto.canWrite = rightsService.hasRight(actor, roles, user, userActiveRoles, Rights.WRITE);

		dto.hasPassword = StringUtils.isNotEmpty(user.getPassword());
		dto.passwordChangedDate = user.getPasswordChangedDate();
		dto.countryId = user.getCountryId();

		dto.languageId = StringUtils.defaultIfBlank(user.getLanguageId(), studyService.getStudy().getDefaultLanguageId());

		dto.loginDate = user.getLoginDate();
		dto.userAgent = user.getUserAgent();
		dto.isAdmin = rightsService.hasRightAdmin(userActiveRoles);
		dto.rights = createUserRightDTO(userActiveRoles);

		dto.blocked = user.getPasswordAttempts() >= UserSecurityService.PASSWORD_MAX_ATTEMPTS;

		if(user.getEmailModificationDate() != null) {
			final var emailExpirationDate = ChronoUnit.DAYS.addTo(user.getEmailModificationDate(), UserService.PENDING_EMAIL_EXPIRY_LIMIT_IN_DAYS);
			if(emailExpirationDate.isAfter(ZonedDateTime.now())) {
				dto.pendingEmail = user.getPendingEmail();
				dto.newEmailExpirationDate = emailExpirationDate;
			}
		}

		return dto;
	}

	@Override
	public User generateUser(final UserCreationDTO userCreationDTO) {
		final var generatedUser = new User();
		generatedUser.setEmail(userCreationDTO.email());
		generatedUser.setName(userCreationDTO.name());
		generatedUser.setPhone(userCreationDTO.phone());
		generatedUser.setExternallyManaged(userCreationDTO.externallyManaged());
		generatedUser.setCountryId(userCreationDTO.countryId());
		generatedUser.setLanguageId(userCreationDTO.languageId());

		return generatedUser;
	}

	@Override
	public void updateUser(final User user, final UserUpdateDTO userUpdateDTO) {
		user.setName(userUpdateDTO.name());
		user.setPhone(userUpdateDTO.phone());
		user.setCountryId(userUpdateDTO.countryId());
		user.setLanguageId(userUpdateDTO.languageId());
	}

	private UserRightsDTO createUserRightDTO(final Collection<Role> roles) {
		final var dto = new UserRightsDTO();

		dto.canCreateUser = rightsService.hasRight(roles, Entity.PROFILE, Rights.WRITE);
		final var profiles = roles.stream().map(Role::getProfile).toList();
		dto.readProfilesIds = profiles.stream().flatMap(p -> p.getProfiles(Rights.READ).stream()).map(Profile::getId).collect(Collectors.toList());
		dto.writeProfilesIds = profiles.stream().flatMap(p -> p.getProfiles(Rights.WRITE).stream()).map(Profile::getId).collect(Collectors.toList());
		dto.readScopeModelIds = profiles.stream().flatMap(p -> p.getScopeModels(Rights.READ).stream()).map(ScopeModel::getId).collect(Collectors.toList());
		dto.writeScopeModelIds = profiles.stream().flatMap(p -> p.getScopeModels(Rights.WRITE).stream()).map(ScopeModel::getId).collect(Collectors.toList());

		return dto;
	}

	private String generateKey() {
		//255 is the max authorized by the Basic authentication protocol
		return RandomStringUtils.randomAlphanumeric(32);
	}

}
