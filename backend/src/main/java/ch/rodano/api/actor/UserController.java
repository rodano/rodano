package ch.rodano.api.actor;

import java.util.Optional;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.export.ExportFormat;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.exception.WrongDataConditionException;
import ch.rodano.core.model.exception.security.WrongCredentialsException;
import ch.rodano.core.model.role.RoleStatus;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;
import ch.rodano.core.model.user.UserSearch;
import ch.rodano.core.model.user.UserSortBy;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.mail.MailService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.core.services.bll.user.UserService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;
import ch.rodano.core.utils.UtilsService;

@Tag(name = "User")
@RestController
@RequestMapping("/users")
@Validated
@Transactional(readOnly = true)
public class UserController extends AbstractSecuredController {

	private final MailService mailService;
	private final UserService userService;
	private final UserSecurityService userSecurityService;
	private final ActorDTOService actorDTOService;
	private final UtilsService utilsService;
	private final ScopeDAOService scopeDAOService;

	private final Integer defaultPageSize;

	public UserController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final MailService mailService,
		final UserService userService,
		final UserSecurityService userSecurityService,
		final ActorDTOService actorDTOService,
		final UtilsService utilsService,
		final ScopeDAOService scopeDAOService,
		@Value("${rodano.pagination.maximum-page-size}") final Integer defaultPageSize
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.mailService = mailService;
		this.userService = userService;
		this.userSecurityService = userSecurityService;
		this.actorDTOService = actorDTOService;
		this.utilsService = utilsService;
		this.scopeDAOService = scopeDAOService;
		this.defaultPageSize = defaultPageSize;
	}

	/**
	 * Search for users
	 * Be careful when changing this method signature, it is used by SSO Reverse Proxy.
	 *
	 * @return Users
	 */
	@Operation(summary = "Search for users")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public PagedResult<UserDTO> search(
		@Parameter(description = "Scope PKs") @RequestParam final Optional<Set<Long>> scopePks,
		@Parameter(description = "Profile IDs") @RequestParam final Optional<Set<String>> profileIds,
		@Parameter(description = "Role statuses") @RequestParam final Optional<Set<RoleStatus>> states,
		@Parameter(description = "User e-mail") @RequestParam final Optional<String> email,
		@Parameter(description = "Full text search on name and e-mail") @RequestParam final Optional<String> fullText,
		@Parameter(description = "Is the user enabled?") @RequestParam final Optional<Boolean> enabled,
		@Parameter(description = "Is the user externally managed?") @RequestParam final Optional<Boolean> externallyManaged,
		@Parameter(description = "Order the results by which property?") @RequestParam final Optional<UserSortBy> sortBy,
		@Parameter(description = "Use the ascending order?") @RequestParam final Optional<Boolean> orderAscending,
		@Parameter(description = "Page size") @RequestParam final Optional<Integer> pageSize,
		@Parameter(description = "Page index") @RequestParam final Optional<Integer> pageIndex
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();

		final var search = new UserSearch()
			.setScopePks(scopePks)
			.setProfileIds(profileIds)
			.setStates(states)
			.setFullText(fullText.filter(StringUtils::isNotBlank))
			.setEmail(email.filter(StringUtils::isNotBlank))
			.setIncludeDeleted(rightsService.hasRight(currentRoles, FeatureStatic.MANAGE_DELETED_DATA))
			.setEnabled(enabled)
			.setExternallyManaged(externallyManaged)
			.setPageSize(pageSize.isEmpty() ? Optional.of(defaultPageSize) : pageSize)
			.setPageIndex(pageIndex.isEmpty() ? Optional.of(0) : pageIndex);
		//set sort if provided
		sortBy.map(search::setSortBy);
		orderAscending.map(search::setSortAscending);

		return userService.search(search, currentActor, currentRoles)
			.withObjectsTransformation(u -> actorDTOService.createUserDTOs(u, currentActor, currentRoles));
	}

	@Operation(summary = "Export users in excel format")
	@GetMapping("export")
	public ResponseEntity<StreamingResponseBody> exportUsers(
		@Parameter(description = "Scope PKs") @RequestParam final Optional<Set<Long>> scopePks,
		@Parameter(description = "Profile IDs") @RequestParam final Optional<Set<String>> profileIds,
		@Parameter(description = "Role statuses") @RequestParam final Optional<Set<RoleStatus>> states,
		@Parameter(description = "User e-mail") @RequestParam final Optional<String> email,
		@Parameter(description = "Full text search on name and e-mail") @RequestParam final Optional<String> fullText,
		@Parameter(description = "Is the user enabled?") @RequestParam final Optional<Boolean> enabled,
		@Parameter(description = "Is the user externally managed?") @RequestParam final Optional<Boolean> externallyManaged
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();

		final var predicate = new UserSearch()
			.setScopePks(scopePks)
			.setProfileIds(profileIds)
			.setStates(states)
			.setEmail(email.filter(StringUtils::isNotBlank))
			.setFullText(fullText.filter(StringUtils::isNotBlank))
			.setIncludeDeleted(rightsService.hasRight(currentRoles, FeatureStatic.MANAGE_DELETED_DATA))
			.setEnabled(enabled)
			.setExternallyManaged(externallyManaged)
			.setSortBy(UserSearch.DEFAULT_SORT_BY)
			.setSortAscending(UserSearch.DEFAULT_SORT_ASCENDING);

		final var languages = actorService.getLanguages(currentActor);

		//send response
		final StreamingResponseBody stream = os -> userService.exportUsers(os, predicate, currentActor, currentRoles, languages);
		final var filename = studyService.getStudy().generateFilename("users", ExportFormat.CSV);
		return exportResponse(ExportFormat.CSV, stream, filename);
	}

	@Operation(summary = "Get user")
	@GetMapping("{userPk}")
	@ResponseStatus(HttpStatus.OK)
	public UserDTO getUser(
		@PathVariable final Long userPk
	) {
		//retrieve user
		final var user = userService.getUserByPk(userPk);
		utilsService.checkNotNull(User.class, user, userPk);

		//check rights
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRight(currentActor, currentRoles, user, Rights.READ);

		return actorDTOService.createUserDTO(user, currentActor, currentRoles);
	}

	/**
	 * Create a user
	 * Be careful when changing this method signature, it is used by SSO Reverse Proxy.
	 */
	@Operation(summary = "Create new user")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public UserDTO createUser(
		@Valid @RequestBody final UserCreationDTO userCreationDTO,
		final HttpServletRequest servletRequest
	) {
		// retrieve the future role scope
		final var selectedScope = scopeDAOService.getScopeByPk(userCreationDTO.role().getScopePk());
		utilsService.checkNotNull(Scope.class, selectedScope, userCreationDTO.role().getScopePk());

		// Check right on profile
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(selectedScope);
		final var selectedProfile = studyService.getStudy().getProfile(userCreationDTO.role().getProfileId());
		rightsService.checkRight(currentActor, currentRoles, selectedProfile, Rights.WRITE);

		// generate the needed objects from the DTO
		final var newUser = actorDTOService.generateUser(userCreationDTO);
		final var profile = studyService.getStudy().getProfile(userCreationDTO.role().getProfileId());
		final var roleScope = scopeDAOService.getScopeByPk(userCreationDTO.role().getScopePk());
		final var createdUser = userService.createUser(
			newUser,
			profile,
			roleScope,
			getServerURL(servletRequest),
			currentContext()
		);

		return actorDTOService.createUserDTO(createdUser, currentActor, currentRoles);
	}

	@Operation(summary = "Update user")
	@PutMapping("{userPk}")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public UserDTO updateUser(
		@PathVariable final Long userPk,
		@Valid @RequestBody final UserUpdateDTO userUpdateDTO
	) {
		//retrieve user
		final var user = userService.getUserByPk(userPk);
		utilsService.checkNotNull(User.class, user, userPk);

		//check rights
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRight(currentActor, currentRoles, user, Rights.WRITE);

		//update the existing user with the data from the DTO
		actorDTOService.updateUser(user, userUpdateDTO);
		userService.saveUser(user, currentContext(), "Update user");

		return actorDTOService.createUserDTO(user, currentActor, currentRoles);
	}

	@Operation(summary = "Remove user")
	@PutMapping("{userPk}/remove")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public UserDTO removeUser(
		@PathVariable final Long userPk
	) {
		//retrieve user
		final var user = userService.getUserByPk(userPk);
		utilsService.checkNotNull(User.class, user, userPk);

		//check rights
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRight(currentActor, currentRoles, user, Rights.WRITE);

		//delete user
		userService.deleteUser(user, currentContext(), "Remove user");

		return actorDTOService.createUserDTO(user, currentActor, currentRoles);
	}

	@Operation(summary = "Restore user")
	@PutMapping("{userPk}/restore")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public UserDTO restoreUser(
		@PathVariable final Long userPk
	) {
		//retrieve user
		final var user = userService.getUserByPk(userPk);
		utilsService.checkNotNull(User.class, user, userPk);

		//check rights
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRight(currentActor, currentRoles, user, Rights.WRITE);
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.MANAGE_DELETED_DATA);

		//restore user
		userService.restoreUser(user, currentContext(), "Restore user");

		return actorDTOService.createUserDTO(user, currentActor, currentRoles);
	}

	@Operation(summary = "Change the user's email")
	@PostMapping("{userPk}/email")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public UserDTO changeEmail(
		@PathVariable final Long userPk,
		@Valid @RequestBody final ChangeEmailDTO emailDTO,
		final HttpServletRequest servletRequest
	) {
		//retrieve user
		final var user = userService.getUserByPk(userPk);
		utilsService.checkNotNull(User.class, user, userPk);

		final var currentUser = (User) currentActor();
		final var currentRoles = currentActiveRoles();

		// Check the user's password
		if(!userSecurityService.isPasswordValid(currentUser, emailDTO.password())) {
			throw new WrongCredentialsException();
		}

		userSecurityService.changeEmail(
			user,
			emailDTO.email(),
			currentUser,
			getServerURL(servletRequest),
			currentContext()
		);

		return actorDTOService.createUserDTO(user, currentUser, currentRoles);
	}

	@Operation(summary = "Convert to a local user")
	@PutMapping("{userPk}/convert-to-local")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public UserDTO convertToLocalUser(
		@PathVariable final Long userPk
	) {
		//retrieve user
		final var user = userService.getUserByPk(userPk);
		utilsService.checkNotNull(User.class, user, userPk);

		//check rights
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRightAdmin(currentActor, currentRoles);

		user.setExternallyManaged(false);
		userService.saveUser(user, currentContext(), "Convert to a local user");

		return actorDTOService.createUserDTO(user, currentActor, currentRoles);
	}

	@Operation(summary = "Convert to an external user")
	@PutMapping("{userPk}/convert-to-external")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public UserDTO convertToExternalUser(
		@PathVariable final Long userPk
	) {
		//retrieve user
		final var user = userService.getUserByPk(userPk);
		utilsService.checkNotNull(User.class, user, userPk);

		//check rights
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRightAdmin(currentActor, currentRoles);

		user.setExternallyManaged(true);
		userService.saveUser(user, currentContext(), "Convert to an external user");

		return actorDTOService.createUserDTO(user, currentActor, currentRoles);
	}

	@Operation(summary = "Unblock user")
	@PutMapping("{userPk}/unblock")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public UserDTO unblockUser(
		@PathVariable final Long userPk
	) {
		//retrieve user
		final var user = userService.getUserByPk(userPk);
		utilsService.checkNotNull(User.class, user, userPk);

		//check rights
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRight(currentActor, currentRoles, user, Rights.WRITE);

		user.setPasswordAttempts(0);
		user.setRecoveryCode(null);
		userService.saveUser(user, currentContext(), "Unblock user");

		return actorDTOService.createUserDTO(user, currentActor, currentRoles);
	}

	@SecurityRequirements
	@Operation(summary = "Verify user e-mail", description = "Verify user's e-mail with the provided verification code")
	//warning: if you change this API endpoint, do not forget to change it in the WebConfigurer/SecurityConfiguration configuration classes!
	@PostMapping("email-verification/{verificationCode}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void verifyUserEmail(
		@PathVariable final String verificationCode
	) {
		userService.verifyUserEmail(verificationCode, currentContext());
	}

	@SecurityRequirements
	@Operation(summary = "Recover user account", description = "Recover user's account with the provided recovery code")
	//warning: if you change this API endpoint, do not forget to change it in the WebConfigurer/SecurityConfiguration configuration classes!
	@PostMapping("account-recovery/{recoveryCode}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void recoverUserAccount(
		@PathVariable final String recoveryCode
	) {
		userSecurityService.recoverUserAccount(recoveryCode, currentContext());
	}

	@Operation(summary = "Resend email verification email")
	@PostMapping("{userPk}/resend-email-verification")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void resendEmailVerificationEmail(
		@PathVariable final Long userPk,
		final HttpServletRequest servletRequest
	) {
		final var user = userService.getUserByPk(userPk);
		utilsService.checkNotNull(User.class, user, userPk);

		if(StringUtils.isBlank(user.getPendingEmail())) {
			throw new WrongDataConditionException("User has no pending email");
		}

		mailService.sendUserEmailVerificationEmail(
			user,
			UserService.PENDING_EMAIL_EXPIRY_LIMIT_IN_DAYS,
			getServerURL(servletRequest),
			currentContext()
		);
	}

	@Operation(summary = "Resend account activation email")
	@PostMapping("{userPk}/resend-account-activation")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void resendAccountActivationEmail(
		@PathVariable final Long userPk,
		final HttpServletRequest servletRequest
	) {
		final var user = userService.getUserByPk(userPk);
		utilsService.checkNotNull(User.class, user, userPk);

		if(user.isActivated()) {
			throw new WrongDataConditionException("User is already activated");
		}

		mailService.sendUserAccountActivationInvitation(
			user,
			roleService.getRoles(user).getFirst(),
			getServerURL(servletRequest),
			currentContext()
		);
	}
}
