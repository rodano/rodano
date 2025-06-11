package ch.rodano.core.services.bll.user;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import ch.rodano.api.controller.user.exception.EmailAlreadyUsedException;
import ch.rodano.api.controller.user.exception.UserNotActivatedException;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.workflow.WorkflowAction;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.exception.InvalidOneUseCodeException;
import ch.rodano.core.model.exception.NoEnabledRoleException;
import ch.rodano.core.model.exception.UnauthorizedException;
import ch.rodano.core.model.exception.UserNotFoundException;
import ch.rodano.core.model.exception.security.AlreadyUsedPassword;
import ch.rodano.core.model.exception.security.TooManyAttemptsException;
import ch.rodano.core.model.exception.security.WeakPasswordException;
import ch.rodano.core.model.exception.security.WrongCredentialsException;
import ch.rodano.core.model.exception.security.WrongPasswordException;
import ch.rodano.core.model.mail.DefinedTemplatedMail;
import ch.rodano.core.model.mail.MailOrigin;
import ch.rodano.core.model.mail.MailTemplate;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.mail.MailService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.session.SessionService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.core.services.rule.RuleService;

import static ch.rodano.core.services.bll.user.UserService.PENDING_EMAIL_EXPIRY_LIMIT_IN_DAYS;

@Service
public class UserSecurityService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static final int BCRYPT_STRENGTH = 12;

	public static final int PASSWORD_MAX_ATTEMPTS = 5;
	private static final String PASSWORD_CHARACTERS = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`";
	private static final Pattern PASSWORD_PATTERN_UPPERCASE = Pattern.compile("[A-Z]+");
	private static final Pattern PASSWORD_PATTERN_DIGIT = Pattern.compile("\\d+");

	private static final int PASSWORD_RESET_CODE_EXPIRY_IN_MINUTES = 15;

	private final StudyService studyService;
	private final SessionService sessionService;
	private final UserService userService;
	private final UserDAOService userDAOService;
	private final ActorService actorService;
	private final RoleService roleService;
	private final MailService mailService;
	private final RuleService ruleService;

	public UserSecurityService(
		final StudyService studyService,
		final SessionService sessionService,
		final UserDAOService userDAOService,
		final RoleService roleService,
		final MailService mailService,
		final RuleService ruleService,
		final ActorService actorService,
		final UserService userService
	) {
		this.studyService = studyService;
		this.sessionService = sessionService;
		this.userService = userService;
		this.userDAOService = userDAOService;
		this.actorService = actorService;
		this.roleService = roleService;
		this.mailService = mailService;
		this.ruleService = ruleService;
	}

	/**
	 * Login a user
	 *
	 * @param email       User e-mail
	 * @param password    User password
	 * @param tsToken     2FA Token
	 * @param tsCode      2FA Code
	 * @param tsKey       2FA Key
	 * @param contextURL  The current server URL
	 * @param agent       User agent
	 * @param context     Database context
	 * @return            User session token
	 */
	public String login(
		final String email,
		final String password,
		final Integer tsToken,
		final String tsCode,
		final String tsKey,
		final String contextURL,
		final String agent,
		final DatabaseActionContext context
	) {
		final var user = userDAOService.getUserByEmail(email);
		//consider a wrong e-mail as wrong credentials to prevent hackers from testing valid e-mails
		if(user == null) {
			logger.info("Someone tried to log in with the email [{}] that does not exist in the database", email);
			throw new WrongCredentialsException();
		}

		// If the user has not yet activated their account, reject them
		if(!user.isActivated()) {
			throw new UserNotActivatedException();
		}

		try {
			authenticate(
				user,
				password,
				tsToken,
				StringUtils.defaultIfBlank(tsCode, null),
				tsKey,
				contextURL,
				context
			);
		}
		catch(final WrongPasswordException | TooManyAttemptsException e) {
			logger.info("User {} failed to log in with the following error: {}", email, e.getLocalizedMessage());
			//consider any known exception as wrong credentials to prevent hackers from testing valid e-mails
			throw new WrongCredentialsException();
		}

		updateUserLoginInfo(
			user,
			agent,
			context
		);

		// Create and save the session object
		final var session = sessionService.createSession(user);

		// Log login
		logger.info("User {} logged in", email);

		return session.getToken();
	}

	public String delegateLogin(
		final Actor actor,
		final String delegateEmail,
		final String agent,
		final DatabaseActionContext context
	) {
		// Retrieve delegateUser
		final var delegateUser = userDAOService.getUserByEmail(delegateEmail);
		//here, it's possible to return the fact the user does not exist because the actor performing the delegate login is already logged in
		//this cannot be used by hackers to test for valid e-mails
		if(delegateUser == null) {
			throw new UserNotFoundException(delegateEmail);
		}

		final var delegateUserRoles = roleService.getActiveRoles(delegateUser);

		// Check active role
		if(delegateUserRoles.isEmpty()) {
			// No activated role
			logger.warn("Robot {} tried to delegate login for user {} which does not have any activated role", actor.getName(), delegateUser.getPk());
			throw new NoEnabledRoleException();
		}

		// Check if the user has been deleted
		if(delegateUser.getDeleted()) {
			throw new WrongCredentialsException();
		}

		// Update user login info
		updateUserLoginInfo(
			delegateUser,
			agent,
			context
		);

		// Create and save the session object
		final var session = sessionService.createSession(delegateUser);

		// Log login
		logger.info("User {} logged in", delegateUser.getEmail());

		// Send normal response
		return session.getToken();
	}

	/**
	 * Log in the user and update his login status info.
	 *
	 * @param user      User to log in
	 * @param agent     User agent
	 * @param context   Database action context
	 */
	private void updateUserLoginInfo(
		final User user,
		final String agent,
		final DatabaseActionContext context
	) {
		// Manage login and logout date
		if(user.getLoginDate() != null && user.getLogoutDate() == null) {
			logger.info("Logout has been done automatically for user {}", user.getName());
		}

		// Set user information
		user.setLogoutDate(null);
		user.updateLoginDates(ZonedDateTime.now());
		user.setUserAgent(agent);

		// Execute new rule
		final var study = studyService.getStudy();

		if(study.getEventActions().containsKey(WorkflowAction.USER_LOGIN)) {
			final var rules = study.getEventActions().get(WorkflowAction.USER_LOGIN);

			final var rootScope = actorService.getRootScope(user).orElseThrow();
			final var state = new DataState(rootScope);
			ruleService.execute(state, rules, context);
		}

		//Save user modifications
		userDAOService.saveUser(user, context, "Login");
	}

	/**
	 * Log out the user and delete the user session.
	 * @param user      User to log out
	 * @param token     User's session token
	 * @param context   Database action context
	 */
	public void logout(
		final User user,
		final String token,
		final DatabaseActionContext context
	) {
		user.setLogoutDate(ZonedDateTime.now());
		userDAOService.saveUser(user, context, "Logout");

		final var session = sessionService.getSessionByToken(token);
		sessionService.deleteSession(session);
	}

	/**
	 * Authenticate a user
	 * @param user          User to authenticate
	 * @param password      User password
	 * @param token         User 2FA token
	 * @param code          User 2FA code
	 * @param key           User 2FA key
	 * @param url           The server URL (used for sending password recovery e-mails)
	 * @param context       The database action context
	 * @throws NoEnabledRoleException                 Thrown if the user does not have any enabled role
	 */
	private void authenticate(
		final User user,
		final String password,
		final Integer token,
		final String code,
		final String key,
		final String url,
		final DatabaseActionContext context
	) {
		// Check if the user has been deleted
		if(user.getDeleted()) {
			throw new WrongCredentialsException();
		}

		// No login when password is not set
		if(StringUtils.isBlank(user.getPassword())) {
			throw new RuntimeException("No user password has been set");
		}

		//reset attempts because of time
		if(user.getLoginBlockingDate() != null && user.getLoginBlockingDate().plusDays(1).isBefore(ZonedDateTime.now())) {
			user.setPasswordAttempts(0);
		}

		//check attempts
		if(user.getPasswordAttempts() >= PASSWORD_MAX_ATTEMPTS) {
			//setup recovery process only once
			if(StringUtils.isBlank(user.getRecoveryCode())) {
				final var recoveryCode = UUID.randomUUID().toString();
				user.setRecoveryCode(recoveryCode);
				userDAOService.saveUser(user, context, String.format("Account is locked due to %s failed attempts to login", PASSWORD_MAX_ATTEMPTS));
				mailService.sendAccountLockedEmail(user, url, context);
			}
			throw new TooManyAttemptsException(PASSWORD_MAX_ATTEMPTS);
		}

		//check password
		if(!isPasswordValid(user, password)) {
			user.setPasswordAttempts(user.getPasswordAttempts() + 1);
			user.setLoginBlockingDate(ZonedDateTime.now());
			userDAOService.saveUser(user, context, "User tried to login");
			throw new WrongPasswordException();
		}

		// TODO 2FA
		//check two step if it has been activated
		/*
		if(user.getUserData().isTwoStep()) {
			//check if client is trusted
			var valid = false;
			if(key != null) {
				try {
					final var client = user.getUserData().getUserClient(key);
					if(client.isValid()) {
						client.setLastUse(ZonedDateTime.now());
						valid = true;
					}
				}
				catch(@SuppressWarnings("unused") final Exception e) {
					//unable to find client
					//e.printStackTrace();
				}
			}

			if(!valid) {
				if(token == null && code == null) {
					throw new MustUseTokenException();
				}
				//token and code must not been used at the same time
				if(token != null && code != null) {
					throw new UnsupportedOperationException("Token and code must not been used at the same time");
				}

				//check token
				if(token != null) {
					//this should never throw a wrong password exception as password should be valid here
					if(isValidToken(user, password, token)) {
						valid = true;
					}
				}

				if(code != null) {
					//remove all dashes
					final var oneUseCode = new Base32().decode(code.replaceAll("-", ""));
					for(final var userOneUseCode : new ArrayList<>(user.getUserData().getOneUseTwoStepCodes())) {
						if(Arrays.equals(userOneUseCode, oneUseCode)) {
							//consume one use code
							user.getUserData().getOneUseTwoStepCodes().remove(userOneUseCode);
							valid = true;
							//send e-mail to user
							final var text = "Hi,"
								.concat(System.lineSeparator())
								.concat("We would like to inform you that someone used a backup code to login with your account.")
								.concat(System.lineSeparator())
								.concat(
									"If this is not you, please login the application as soon as possible and generate new backup codes on your account page. Please check trusted clients list and revoke suspect clients if any."
								)
								.concat(System.lineSeparator())
								.concat("If this was you, you can disregard this e-mail.");

							final var mail = new Mail();
							mail.setSender(study.getEmail());
							mail.setRecipients(Collections.singleton(user.getEmail()));
							mail.setSubject(String.format("Login using a backup code on %s", study.getDefaultLocalizedLongname()));
							mail.setTextBody(text);
							mail.setOrigin(MailOrigin.SYSTEM);
							mail.setIntent("Authenticate user using backup code");

							mailService.createMail(mail, null, "Authenticate user using backup code");
						}
					}
				}

				if(!valid) {
					user.setPasswordAttempts(user.getPasswordAttempts() + 1);
					user.setLoginBlockingDate(ZonedDateTime.now());
					throw new WrongTwoStepTokenException();
				}
			}
		}
		 */

		//reset attempts because of a successful login
		user.setPasswordAttempts(0);
		//destroy password reset code because of a successful login
		//that's because a random guy could have clicked the "I forgot my password" button with the e-mail of another user
		user.setPasswordResetCode(null);
		user.setPasswordResetDate(null);
		userDAOService.saveUser(user, context, "User entered a valid password");

		//check active role
		if(roleService.getActiveRoles(user).isEmpty()) {
			throw new NoEnabledRoleException();
		}
	}

	public boolean mustChangePassword(final User user) {
		final var study = studyService.getStudy();

		//user who have a password but are externally managed are exempted from changing their password
		//a user may have a password while being externally managed if they had a standard account before being externally managed
		if(user.isExternallyManaged()) {
			return false;
		}

		//check if user must change password
		if(user.isShouldChangePassword()) {
			return true;
		}
		//password is new and must be changed
		if(user.getPasswordChangedDate() == null) {
			return true;
		}
		//user password is too old and must be changed
		if(study.getPasswordValidityDuration() > 0) {
			final var passwordChangedDate = user.getPasswordChangedDate().plusDays(study.getPasswordValidityDuration());
			if(passwordChangedDate.isBefore(ZonedDateTime.now())) {
				return true;
			}
		}

		return false;
	}

	public void triggerPasswordReset(final User user, final String url, final DatabaseActionContext context) {
		final var study = studyService.getStudy();

		//external user
		if(user.isExternallyManaged()) {
			mailService.sendExternalUserCanNotRecoverPasswordNotification(user, context);
			logger.info("Externally managed user {} tried to reset his password", user.getEmail());
			return;
		}

		//user not activated
		if(!user.isActivated()) {
			final var mail = new DefinedTemplatedMail(
				MailTemplate.VALIDATE_REGISTRATION_FIRST, Map.ofEntries(
					Map.entry("study", study),
					Map.entry("user", user)
				)
			);
			mail.setSender(study.getEmail());
			mail.setRecipients(Collections.singleton(user.getEmail()));
			mail.setOrigin(MailOrigin.SYSTEM);
			mail.setIntent("Validate registration to recover password");
			mailService.createMail(mail, context, "Validate registration to recover password");

			logger.info("User {} tried to reset his password but has not validated his account", user.getEmail());
			return;
		}

		// Normal case

		// TODO 2FA
		// Two step must be disabled as two step key is encrypted with user password
		// That's very embarrassing, and that's the weak point of the security because two step can be disable for every account this way
		// Is it better to store two step key uncrypted or to disable two step when user loses its password?
		/*
		if(user.getUserData().isTwoStep()) {
			userService.disableTwoStep(user);

			userService.saveUser(user, null, "Disable two step as password recovering email has been sent");
		}
		 */

		final var passwordRecoveryCode = UUID.randomUUID().toString();
		user.setPasswordResetCode(passwordRecoveryCode);
		user.setPasswordResetDate(ZonedDateTime.now());
		userDAOService.saveUser(user, context, "User forgot his password");

		mailService.sendPasswordResetEmail(user, PASSWORD_RESET_CODE_EXPIRY_IN_MINUTES, url, context);
	}

	public void resetPassword(final String resetCode, final String newPassword, final DatabaseActionContext context) {
		final var user = userDAOService.getUserByResetCode(resetCode);
		// HTTP exception is thrown here but OK
		if(user == null) {
			throw new InvalidOneUseCodeException();
		}
		final var resetCodeAge = ChronoUnit.MINUTES.between(user.getPasswordResetDate(), ZonedDateTime.now());
		if(resetCodeAge > PASSWORD_RESET_CODE_EXPIRY_IN_MINUTES) {
			throw new InvalidOneUseCodeException();
		}
		validateAndSetPassword(user, newPassword);
		//destroy password reset code
		user.setPasswordResetCode(null);
		user.setPasswordResetDate(null);
		unblockUser(user, context);
		userDAOService.saveUser(user, context, "Reset password using reset code");
	}

	public void recoverUserAccount(final String recoveryCode, final DatabaseActionContext context) {
		final var user = userDAOService.getUserByRecoveryCode(recoveryCode);
		// HTTP exception is thrown here but OK
		if(user == null) {
			throw new InvalidOneUseCodeException();
		}

		unblockUser(user, context);
		userDAOService.saveUser(user, context, "User account recovered using recovery code");
	}

	/**
	 * Set new e-mail for user
	 * @param user                  User to modify
	 * @param newEmail              The new e-mail
	 * @param actorOfChange         The actor performing the modification
	 * @param emailVerificationURL  The e-mail verification URL (used for e-mail notification)
	 * @param context               Database action context
	 */
	public void changeEmail(
		final User user,
		final String newEmail,
		final Actor actorOfChange,
		final String emailVerificationURL,
		final DatabaseActionContext context
	) {
		if(user.getEmail().equals(newEmail)) {
			throw new EmailAlreadyUsedException();
		}
		userService.checkEmailCanBeUsed(newEmail);

		// If the user's e-mail has been changed, the user will need to verify the new e-mail address
		setPendingEmail(user, newEmail);
		notifyOfPendingEmail(user, newEmail, actorOfChange, emailVerificationURL, context);

		userDAOService.saveUser(user, context, "Change e-mail");
	}

	/**
	 * Check the user's current password and set a new password.
	 *
	 * @param user              User to modify
	 * @param currentPassword   User's current password
	 * @param newPassword       New password
	 * @param context           Database action context
	 */
	public void changePassword(
		final User user,
		final String currentPassword,
		final String newPassword,
		final DatabaseActionContext context
	) {
		if(user.isExternallyManaged()) {
			throw new UnsupportedOperationException("Unable to change the password of a user managed externally");
		}

		//Check if the old password is the same as before
		if(currentPassword.equals(newPassword)) {
			throw new AlreadyUsedPassword();
		}

		//Check if the password that is about to be replaced is correct
		if(!isPasswordValid(user, currentPassword)) {
			throw new WrongCredentialsException();
		}

		//try to set password
		validateAndSetPassword(user, newPassword);
		userDAOService.saveUser(user, context, "Change Password");
	}

	/**
	 * Activate a user using the user activation code.
	 * @param activationCode The user activation code
	 * @param acceptPolicies Did the user accept the privacy policies ?
	 * @param password      Provided password
	 * @param context       Database context
	 */
	public void activateUser(
		final String activationCode,
		final boolean acceptPolicies,
		final String password,
		final DatabaseActionContext context
	) {
		// Get the user associated with the given activation code
		final var user = userDAOService.getUserByActivationCode(activationCode);

		if(user == null) {
			throw new InvalidOneUseCodeException();
		}

		// TODO maybe this should be in the controller ?
		// We create a new context with the user set as the actor of the changes
		// This way we can have a user that activates himself in the audit trails instead of the system.
		final var newContext = new DatabaseActionContext(
			context.auditAction(),
			Optional.of(user)
		);

		final var userHasPrivacyPolicies = roleService.getRoles(user).stream()
			.map(Role::getProfile)
			.anyMatch(profile -> !profile.getPrivacyPolicies().isEmpty());
		if(!acceptPolicies && userHasPrivacyPolicies) {
			throw new UnauthorizedException("Privacy policy has not been accepted");
		}

		// Check if the user does not have a password.
		if(StringUtils.isBlank(user.getPassword())) {
			// Try to set the new password
			try {
				// Set the new password.
				forceSetNewPassword(user, password, newContext);
			}
			catch(@SuppressWarnings("unused") final WeakPasswordException e) {
				final var message = studyService.getStudy().getLocalizedPasswordMessage(user.getLanguageId());
				throw new WeakPasswordException(message);
			}
		}

		// Set the user to activated
		user.setActivated(true);
		// Remove user's activation code
		user.setActivationCode(null);
		userDAOService.saveUser(user, newContext, "User activated");

		// Enable the roles
		for(final var role : roleService.getRoles(user)) {
			roleService.enableRoleWithoutNotification(user, role, newContext);
		}

		// Send the user activation confirmation e-mail
		mailService.sendUserAccountActivationConfirmation(user, newContext);
	}

	/**
	 * Set new user password. Used in cases where the user's current password is unavailable (e.g. user activation or reset by an admin).
	 * @param user          Use to modify
	 * @param newPassword   New password
	 * @param context       Database action context
	 */
	public void forceSetNewPassword(
		final User user,
		final String newPassword,
		final DatabaseActionContext context
	) {
		// validate and set the user's password
		validateAndSetPassword(user, newPassword);

		// unblock the user
		unblockUser(user, context);

		userDAOService.saveUser(user, context, "Force new password");
	}

	/**
	 * Check if the given user password is valid
	 * @param user          The user
	 * @param rawPassword   The password
	 * @return              True if the password is valid, false otherwise
	 */
	public boolean isPasswordValid(
		final User user,
		final String rawPassword
	) {
		if(StringUtils.isBlank(rawPassword)) {
			return false;
		}

		final var encodedPasswordCheck = new BCryptPasswordEncoder(BCRYPT_STRENGTH).matches(rawPassword, user.getPassword());
		if(encodedPasswordCheck) {
			return true;
		}
		final var md5Hash = generateMD5FromString(rawPassword);
		if(user.getPassword().equals(md5Hash)) {
			migratePasswordHashToBCrypt(user, rawPassword);
			return true;
		}

		return false;
	}

	/**
	 * Send an e-mail verification notification to the user.
	 * @param user      User to modify
	 * @param newEmail  New e-mail
	 * @param actor     The actor performing the change
	 * @param url       E-mail verification URL
	 * @param context   Database action context
	 */
	public void notifyOfPendingEmail(
		final User user,
		final String newEmail,
		final Actor actor,
		final String url,
		final DatabaseActionContext context
	) {
		mailService.sendEmailChangeNotification(user, (User) actor, newEmail, PENDING_EMAIL_EXPIRY_LIMIT_IN_DAYS, context);
		mailService.sendUserEmailVerificationEmail(user, PENDING_EMAIL_EXPIRY_LIMIT_IN_DAYS, url, context);
	}

	public String encodePassword(final String password) {
		return new BCryptPasswordEncoder(BCRYPT_STRENGTH).encode(password);
	}

	/**
	 * Validate password strength and uniqueness, encode the password and set new password for user
	 * @param user          User to modify
	 * @param newPassword   New password to validate and set
	 */
	private void validateAndSetPassword(
		final User user,
		final String newPassword
	) {
		final var study = studyService.getStudy();
		if(StringUtils.isBlank(newPassword)) {
			throw new IllegalArgumentException("Password cannot be blank");
		}
		// check the password strength
		if(!isPasswordStrongEnough(study, newPassword)) {
			final var message = studyService.getStudy().getLocalizedPasswordMessage(studyService.getStudy().getDefaultLanguageId());
			throw new WeakPasswordException(message);
		}

		// retrieve previous password
		final List<String> previousPasswords;
		if(StringUtils.isBlank(user.getPreviousPasswords())) {
			previousPasswords = new ArrayList<>();
		}
		else {
			previousPasswords = new ArrayList<>(Arrays.asList(StringUtils.split(user.getPreviousPasswords(), ',')));
		}

		// test if password has already been used
		if(study.getPasswordUniqueness()) {
			if(previousPasswords.stream().anyMatch(p -> new BCryptPasswordEncoder(BCRYPT_STRENGTH).matches(newPassword, p))) {
				throw new AlreadyUsedPassword();
			}
		}

		// generate the new password hash
		final var encodedPassword = encodePassword(newPassword);

		// save the new hash to the previously used password hashes
		previousPasswords.add(encodedPassword);
		user.setPreviousPasswords(StringUtils.join(previousPasswords, ','));

		user.setShouldChangePassword(false);
		user.setPasswordChangedDate(ZonedDateTime.now());
		user.setPassword(encodedPassword);

		/*if(getUserData().twoStep) {
		
		//reencrypt stuff if old password is available
		//make this work with helperPassword
		if(oldPassword != null || helperPassword != null) {
			final PrivateKey privateKey = unlockPrivateKey(oldPassword);

			//reencrypt private key with new password
			final SecretKeySpec key = generateSecretKeySpec(newPassword);

			try {
				//encrypt private key with new password key
				final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
				cipher.init(Cipher.ENCRYPT_MODE, key);
				final byte[] encryptedPrivateKey = cipher.doFinal(privateKey.getEncoded());

				//store keys
				getUserData().encryptedPrivateKey = new String(Hex.encodeHex(encryptedPrivateKey));
				save();
			}
			catch(InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
				//no way to come here
				logger.error(e.getLocalizedMessage(), e);
			}
		}
		else {
			try {
				disableTwoStep();
			}
			catch(final WrongDataConditionException e) {
				//now way to come here as two step is enabled
			}
		}
		}*/
	}

	/**
	 * Unblock user
	 * @param user      User to unblock
	 * @param context   Database action context
	 */
	private void unblockUser(
		final User user,
		final DatabaseActionContext context
	) {
		user.setPasswordAttempts(0);
		user.setRecoveryCode(null);
	}

	/**
	 * Set the given e-mail as pending.
	 * @param user      User to modify
	 * @param newEmail  New e-mail
	 */
	private void setPendingEmail(
		final User user,
		final String newEmail
	) {
		user.setPendingEmail(newEmail);
		user.setEmailModificationDate(ZonedDateTime.now());
		user.setEmailVerificationCode(UUID.randomUUID().toString());
	}

	private String generateMD5FromString(final String inputString) {
		try {
			final var md = MessageDigest.getInstance("MD5");
			md.update(inputString.getBytes(StandardCharsets.UTF_8));
			final var md5 = md.digest();
			return new String(Base64.encodeBase64(md5));
		}
		catch(final NoSuchAlgorithmException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return null;
	}

	private boolean isPasswordStrongEnough(final Study study, final String password) {
		if(password.length() < study.getPasswordLength()) {
			return false;
		}
		if(study.isPasswordStrong()) {
			final var upperCaseMatcher = PASSWORD_PATTERN_UPPERCASE.matcher(password);
			final var digitMatcher = PASSWORD_PATTERN_DIGIT.matcher(password);
			return upperCaseMatcher.find() && digitMatcher.find() && StringUtils.containsAny(password, PASSWORD_CHARACTERS);
		}
		return true;
	}

	private void migratePasswordHashToBCrypt(final User user, final String rawPassword) {
		final var encodedPassword = new BCryptPasswordEncoder(BCRYPT_STRENGTH).encode(rawPassword);
		user.setPassword(encodedPassword);
	}

	// TODO 2FA
	/*
	private int hashToInt(final byte[] bytes, final int start) {
		try(final var input = new DataInputStream(new ByteArrayInputStream(bytes, start, bytes.length - start))) {
			return input.readInt();
		}
		catch(final IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static List<byte[]> generateTSOneUseCodes() {
		//generate 5 one use code
		final List<byte[]> codes = new ArrayList<>();
		for(var i = 0; i < 5; i++) {
			final var code = new byte[10];
			new Random().nextBytes(code);
			codes.add(code);
		}
		return codes;
	}
	
	private static byte[] generateTSKey() {
		//create two step key
		final var tsKey = new byte[10];
		new Random().nextBytes(tsKey);
		return tsKey;
	}
	
	private SecretKeySpec generateSecretKeySpec(final String password) {
		try {
			//generate AES key from password
			final var skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			//improve salt - no, "rodano123?!" is not good salt...
			final KeySpec ks = new PBEKeySpec(password.toCharArray(), "rodano123?!".getBytes(), 3, 128);
			return new SecretKeySpec(skf.generateSecret(ks).getEncoded(), "AES");
		}
		catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
			logger.error(e.getLocalizedMessage(), e);
			//no way to come here, there is an algorithm and spec is fine, thanks
			return null;
		}
	}
	
	
	private boolean isValidAuthKey(final User user, final String authkey) {
		return !StringUtils.isBlank(authkey) && Objects.equals(generateMD5FromString(authkey), user.getAuthkey());
	}

	public KeyPair generateRSAKeys(final User user, final String password) throws WrongPasswordException {
		//check user password
		if(!isValidPassword(user, password)) {
			throw new WrongPasswordException();
		}
		try {
			//generate rsa private and public keys
			final var kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			final var kp = kpg.genKeyPair();

			//encode two step key with user password
			final var key = generateSecretKeySpec(password);

			//encrypt private key with password key
			final var cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			final var encryptedPrivateKey = cipher.doFinal(kp.getPrivate().getEncoded());

			//store keys
			user.getUserData().setEncryptedPrivateKey(new String(Hex.encodeHex(encryptedPrivateKey)));
			user.getUserData().setPublicKey(kp.getPublic());

			return kp;
		}
		catch(final NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			//no way to come here
			logger.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

	public PrivateKey unlockPrivateKey(final User user, final String password) {
		final var key = generateSecretKeySpec(password);

		try {
			//decrypt private key with password key
			final var cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			final var privateKey = cipher.doFinal(Hex.decodeHex(user.getUserData().getEncryptedPrivateKey().toCharArray()));

			user.getUserData().setPrivateKey(KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKey)));
			return user.getUserData().getPrivateKey();

		}
		catch(InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | DecoderException | InvalidKeySpecException e) {
			//no way to come here
			logger.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

	public byte[] encrypt(final User user, final byte[] data) {
		try {
			final var cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, user.getUserData().getPublicKey());
			return cipher.doFinal(data);
		}
		catch(InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			//no way to come here
			logger.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

	public byte[] decrypt(final User user, final byte[] data, final String password) {
		return decrypt(data, unlockPrivateKey(user, password));
	}

	public byte[] decrypt(final byte[] data, final PrivateKey key) {
		try {
			final var cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(data);
		}
		catch(InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			//no way to come here
			logger.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

	public void regenerateTSOneUseCodes(final User user) {
		//save one use code
		user.getUserData().setOneUseTwoStepCodes(generateTSOneUseCodes());
	}

	public byte[] generateTSMaterials(final User user) {
		//check requirements
		if(user.getUserData().getPublicKeyAsString() == null) {
			throw new UnsupportedOperationException("A RSA key is needed to generate TS material");
		}

		final var tsKey = generateTSKey();
		//encrypt two step key with user public key and store it in user
		user.getUserData().setEncryptedTwoStepKey(encrypt(user, tsKey));

		//generate and save one use code
		user.getUserData().setOneUseTwoStepCodes(generateTSOneUseCodes());

		return tsKey;
	}

	public boolean isValidToken(final User user, final String password, final Integer token) throws WrongPasswordException {
		try {
			//decrypt two step key with user private key (unlocked thanks to user password)
			final var tsKey = decrypt(user, user.getUserData().getEncryptedTwoStepKeyAsBytes(), password);

			//build mac
			final var tsKeyHMAC = new SecretKeySpec(tsKey, "HmacSHA1");
			final var mac = Mac.getInstance("HmacSHA1");
			mac.init(tsKeyHMAC);

			final var currentTime = ZonedDateTime.now().toInstant().toEpochMilli() / 30000;

			//do the check with the current password, the password before and the password after
			for(var i = -1; i <= 1; i++) {
				//transform time to byte array
				final var time = currentTime + i;
				final var data = ByteBuffer.allocate(8).putLong(time).array();

				//find hash
				final var hash = mac.doFinal(data);

				//calculate offset
				final var offset = hash[hash.length - 1] & 0xF;

				final var truncatedHash = hashToInt(hash, offset) & 0x7FFFFFFF;
				final var pin = truncatedHash % 1000000;

				if(token == pin) {
					return true;
				}
			}

			return false;

		}
		catch(@SuppressWarnings("unused") InvalidKeyException | NoSuchAlgorithmException | IllegalStateException e) {
			//unable to decrypt data with provided password
			throw new WrongPasswordException();
		}
	}

	public String addTrustedClient(
		final User user,
		final String userTSKey,
		final String remoteHost,
		final String userAgent,
		final DatabaseActionContext context
	) {
		String tsKey;
		try {
			final var userClient = user.getUserData().getUserClient(userTSKey);
			tsKey = userClient.getKey();
		}
		catch(@SuppressWarnings("unused") final Exception e) {
			final var client = new UserClient(remoteHost, userAgent);
			user.getUserData().getTwoStepTrustedClients().add(client);
			tsKey = client.getKey();
		}

		// Save user and commitAndDispose changes
		userDAOService.saveUser(user, context, "Login");

		return tsKey;
	}
	 */
}
