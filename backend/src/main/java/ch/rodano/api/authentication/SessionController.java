package ch.rodano.api.authentication;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.core.configuration.core.Configurator;
import ch.rodano.core.configuration.core.Environment;
import ch.rodano.core.model.exception.security.WrongCredentialsException;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.session.SessionService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.core.services.bll.user.UserService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.core.utils.RightsService;

@Tag(name = "Session", description = "Used to manage API sessions")
@RestController
@RequestMapping("sessions")
@Validated
@Transactional(readOnly = true)
public class SessionController extends AbstractSecuredController {
	private final UserDAOService userDAOService;
	private final UserService userService;
	private final UserSecurityService userSecurityService;
	private final SessionService sessionService;
	private final Configurator configurator;
	private final String autologinEmail;

	public SessionController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final UserDAOService userDAOService,
		final UserService userService,
		final UserSecurityService userSecurityService,
		final SessionService sessionService,
		final Configurator configurator,
		@Value("${rodano.sso.autologin:}") final String autologinEmail
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.userDAOService = userDAOService;
		this.userService = userService;
		this.userSecurityService = userSecurityService;
		this.sessionService = sessionService;
		this.configurator = configurator;
		this.autologinEmail = autologinEmail;
	}

	/**
	 * Commit even though WrongCredentialsException is thrown.
	 * The user may have pending modification that must be saved in the database despite the thrown exception
	 */
	@SecurityRequirements
	@Operation(summary = "Login")
	// Warning : if you change this API endpoint, do not forget to change it in the security configuration !
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional(noRollbackFor = WrongCredentialsException.class)
	public AuthenticationDTO login(
		@Valid @RequestBody final CredentialsDTO credentials,
		@RequestHeader("User-Agent") final String agent,
		final HttpServletRequest servletRequest
	) {

		final var token = userSecurityService.login(
			credentials.getEmail(),
			credentials.getPassword(),
			credentials.getTsToken(),
			credentials.getTsCode(),
			credentials.getTsKey(),
			getServerURL(servletRequest),
			agent,
			currentContext()
		);

		return new AuthenticationDTO(token);

		// TODO 2FA
		// Add client to trusted clients
		/*
		if(credentials.isTsTrust()) {
			final var tsKey = userSecurityService.addTrustedClient(user, credentials.getTsKey(), request.getRemoteHost(), agent, currentContext());
			authenticationDTO.setTsKey(tsKey);
		}
		 */
	}

	//in an environment where rodano-ssoproxy has been setup, the real endpoint is provided by rodano-ssoproxy
	@SecurityRequirements
	@Operation(summary = "Get an SSO token", hidden = true)
	// Warning : if you change this API endpoint, do not forget to change it in the security configuration !
	@GetMapping("delegated")
	@Transactional
	public ResponseEntity<Object> ssotoken() {
		if(Environment.PROD.equals(configurator.getEnvironment()) || StringUtils.isBlank(autologinEmail)) {
			return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
		}
		//ONLY FOR TEST PURPOSE
		//the following code reproduces the code in rodano-ssoproxy
		//it is used to test the behavior of the SSO process

		final var user = userDAOService.getUserByEmail(autologinEmail);

		//create session for user
		final var session = sessionService.createSession(user);

		final var dto = new AuthenticationDTO(session.getToken());
		return ResponseEntity.ok(dto);
	}

	/**
	 * Get an authentication key for another user
	 */
	@Operation(summary = "Delegate a login", description = "Create a token that will be used to do requests on all protected resources, like a login")
	@PostMapping("delegated")
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public AuthenticationDTO delegateLogin(
		@Valid @RequestBody final DelegateLoginDTO delegateLogin,
		@RequestHeader("User-Agent") final String agent
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();

		// TODO For the time being, only admin can get a authentication token for another user, it has to be changed
		rightsService.checkRightAdmin(currentActor, currentRoles);

		final var token = userSecurityService.delegateLogin(
			currentActor,
			delegateLogin.email(),
			agent,
			currentContext()
		);

		return new AuthenticationDTO(token);
	}

	@Operation(summary = "Get all sessions", description = "Available for admins only")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<SessionDTO> getSessions() {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRightAdmin(currentActor, currentRoles);

		return sessionService.getSessions()
			.stream()
			.map(session -> new SessionDTO(session, userService.getUserByPk(session.getUserFk())))
			.toList();
	}

	@Operation(summary = "Logout")
	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void logout(
		@RequestHeader("Authorization") final String bearerToken
	) {
		if(bearerToken == null || !bearerToken.startsWith("Bearer ")) {
			throw new InvalidSessionException();
		}

		final var token = bearerToken.substring("Bearer".length()).trim();
		final var user = (User) currentActor();

		userSecurityService.logout(user, token, currentContext());
	}

	@Operation(summary = "Delete a user session", description = "Available for admins only", hidden = true)
	@DeleteMapping("{pk}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void deleteToken(
		@PathVariable final Long pk
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRightAdmin(currentActor, currentRoles);

		final var session = sessionService.getSessionByPk(pk);
		sessionService.deleteSession(session);
	}
}
