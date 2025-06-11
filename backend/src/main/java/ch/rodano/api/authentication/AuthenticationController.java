package ch.rodano.api.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.core.services.bll.user.UserService;
import ch.rodano.core.utils.RightsService;

@Tag(name = "Authentication")
@RestController
@Validated
public class AuthenticationController extends AbstractSecuredController {

	private final UserService userService;
	private final UserSecurityService userSecurityService;

	public AuthenticationController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final UserService userService,
		final UserSecurityService userSecurityService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.userService = userService;
		this.userSecurityService = userSecurityService;
	}

	@SecurityRequirements
	@Operation(summary = "Recover password")
	//warning: if you change this API endpoint, do not forget to change it in the WebConfigurer/SecurityConfiguration configuration classes!
	@PostMapping("/auth/password/recover")
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public void recover(
		@RequestParam final String email,
		final HttpServletRequest servletRequest
	) {
		final var user = userService.getUserByEmail(email);

		//user may be null if an unknown address has been filled
		//in this case, do not crash to prevent users from guessing e-mails
		if(user == null) {
			return;
		}

		userSecurityService.triggerPasswordReset(user, getServerURL(servletRequest), currentContext());
	}

	@Operation(summary = "Change user password")
	//warning: if you change this API endpoint, do not forget to change it in the WebConfigurer/SecurityConfiguration configuration classes!
	@PostMapping("/auth/password/change")
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public void change(
		@Valid @RequestBody final ChangePasswordDTO changePassword
	) {
		final var user = (User) currentActor();
		userSecurityService.changePassword(
			user,
			changePassword.currentPassword(),
			changePassword.newPassword(),
			currentContext()
		);
	}

	@SecurityRequirements
	@Operation(summary = "Reset user password", description = "Reset user's password with the provided password reset code")
	//warning: if you change this API endpoint, do not forget to change it in the WebConfigurer/SecurityConfiguration configuration classes!
	@PostMapping("/auth/password/reset")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void resetUserPassword(
		@Valid @RequestBody final ResetPasswordDTO resetPasswordDTO
	) {
		userSecurityService.resetPassword(resetPasswordDTO.resetCode(), resetPasswordDTO.newPassword(), currentContext());
	}

	/*
	@ApiOperation("generateKey")
	@PostMapping("/twostep/key/generate")
	@ResponseStatus(HttpStatus.CREATED)
	public String generateKey(@RequestParam("password") final String password) throws WrongPasswordException {
		final User user = currentUser();
	
		// Check user password
		if(!user.isValidPassword(password)) {
			throw new WrongPasswordException();
		}
	
		if(user.getUserData().getEncryptedPrivateKey() == null) {
			try {
				user.generateRSAKeys(password);
			}
			catch(final WrongPasswordException ignored) {
				// No way to come here as password is valid
			}
		}
	
		final byte[] tsKey = user.generateTSMaterials();
	
		userDAOService.createOrUpdateUser(user, user, "Generate two step key");
	
		final Base32 codec32 = new Base32();
		return codec32.encodeToString(tsKey);
	}
	
	@ApiOperation("getKey")
	@PostMapping("/twostep/key/get")
	@ResponseStatus(HttpStatus.CREATED)
	public String getKey(@RequestParam("password") final String password) throws WrongPasswordException, FeatureException {
		final User user = currentUser();
	
		// Check two step has been enabled for user
		if(!user.getUserData().isTwoStep()) {
			throw new FeatureException();
		}
	
		// Check user password
		if(!user.isValidPassword(password)) {
			throw new WrongPasswordException();
		}
	
		final byte[] tsKey = user.decrypt(user.getUserData().getEncryptedTwoStepKeyAsBytes(), password);
	
		final Base32 codec32 = new Base32();
		return codec32.encodeToString(tsKey);
	}
	
	@ApiOperation("enable")
	@PostMapping("/twostep/enable")
	@ResponseStatus(HttpStatus.CREATED)
	public List<String> enable(@RequestParam("login") final String login) throws RightException, NoNodeException, WrongDataConditionException, NoUserException {
		return toggleTwoStep(currentUser(), login, true);
	}
	
	@ApiOperation("disable")
	@PostMapping("/twostep/disable")
	@ResponseStatus(HttpStatus.CREATED)
	public List<String> disable(@RequestParam("login") final String login) throws RightException, NoNodeException, WrongDataConditionException, NoUserException {
		return toggleTwoStep(currentUser(), login, false);
	}
	
	@ApiOperation("status")
	@GetMapping("/twostep/status")
	@ResponseStatus(HttpStatus.OK)
	public Boolean status(@RequestParam("login") final String login) throws RightException, NoNodeException, NoUserException {
		final User user = currentUser();
	
		final User operationUser = getOperationUser(user, login);
	
		return operationUser.getUserData().isTwoStep();
	}
	
	@ApiOperation("checkToken")
	@PostMapping("/twostep/token/check")
	@ResponseStatus(HttpStatus.CREATED)
	public void checkToken(@RequestParam("password") final String password, @RequestParam("token") final Integer token) throws FeatureException, WrongPasswordException {
		final User user = currentUser();
	
		// Check two step has been enabled for user
		if(user.getUserData().getEncryptedTwoStepKey() == null) {
			throw new FeatureException();
		}
	
		// Check user password and token
		user.isValidToken(password, token);
	}
	
	@ApiOperation("all")
	@GetMapping("/twostep/clients/all")
	@ResponseStatus(HttpStatus.OK)
	public List<UserClient> add(@RequestParam("login") final String login) throws RightException, NoNodeException, NoUserException {
		final User user = currentUser();
	
		final User operationUser;
		operationUser = getOperationUser(user, login);
	
		//TODO do not send entire trusted clients, the key must remain private
		return operationUser.getUserData().getTwoStepTrustedClients();
	}
	
	@ApiOperation("remove")
	@PostMapping("/twostep/clients/remove")
	@ResponseStatus(HttpStatus.CREATED)
	public void add(@RequestParam("login") final String login, @RequestParam("key") final String key) throws Exception {
		final User user = currentUser();
	
		final User operationUser = getOperationUser(user, login);
	
		operationUser.getUserData().getTwoStepTrustedClients().remove(operationUser.getUserData().getUserClient(key));
	
		userDAOService.createOrUpdateUser(operationUser, operationUser, "Remove twostep authentication");
	}
	
	@ApiOperation("codes/get")
	@PostMapping("/twostep/codes/get")
	@ResponseStatus(HttpStatus.CREATED)
	public List<String> codes(@RequestParam("password") final String password) throws WrongPasswordException, FeatureException {
		final User user = currentUser();
	
		// Check user password
		if(!user.isValidPassword(password)) {
			throw new WrongPasswordException();
		}
	
		if(!user.getUserData().isTwoStep()) {
			throw new FeatureException();
		}
	
		return retrieveBackupCodes(user);
	}
	
	@ApiOperation("remove")
	@PostMapping("/twostep/codes/generate")
	@ResponseStatus(HttpStatus.CREATED)
	public List<String> generateCodes(@RequestParam("password") final String password) throws WrongPasswordException, FeatureException {
		final User user = currentUser();
	
		// Check user password
		if(!user.isValidPassword(password)) {
			throw new WrongPasswordException();
		}
	
		if(!user.getUserData().isTwoStep()) {
			throw new FeatureException();
		}
	
		user.regenerateTSOneUseCodes();
	
		userDAOService.createOrUpdateUser(user, user, "Generate two step codes");
	
		return retrieveBackupCodes(user);
	}
	*/

	/*
	 * Toggle the two step
	 *
	 * @param user   The user
	 * @param login  The login
	 * @param toggle Whether to activate or deactivate
	 * @return A response
	 * @throws RightException Thrown if the user does not have the right to do this action
	 */
	/*
	private List<String> toggleTwoStep(final User user, final String login, final boolean toggle) throws RightException, NoNodeException, NoUserException, WrongDataConditionException {
		final User operationUser;
	
		operationUser = getOperationUser(user, login);
	
		final List<String> backupCodes;
	
		if(toggle) {
			operationUser.enableTwoStep();
			backupCodes = retrieveBackupCodes(operationUser);
		}
		else {
			operationUser.disableTwoStep();
			backupCodes = null;
		}
	
		// Save modifications
		userDAOService.createOrUpdateUser(operationUser, operationUser, "Toggle two step");
	
		return backupCodes;
	}
	*/

	/*
	 * Get the user executing the action
	 *
	 * @param user  The user calling the api
	 * @param login The login
	 * @return The operation user
	 * @throws RightException  Thrown if the user does not have the right to do this action
	 *                         while fetching the user in the database
	 * @throws NoUserException Thrown if no user is found
	 */
	/*
	private User getOperationUser(final User user, final String login) throws RightException, NoUserException, NoNodeException {
		if(login.equals(user.getLogin())) {
			return user;
		}
	
		Utils.checkRightAdmin(user);
		return userDAOService.getUserByLogin(login);
	}
	*/

	/*
	 * Retrieve the backup codes of the user
	 *
	 * @param user The user
	 * @return The backup codes
	 */
	/*
	private List<String> retrieveBackupCodes(final User user) {
		return user.getUserData().getOneUseTwoStepCodes().stream().map(code -> new Base32().encodeToString(code)).toList();
	}
	*/
}
