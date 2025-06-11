package ch.rodano.core.services.bll.user;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.configuration.interceptor.MustChangePasswordInterceptor;
import ch.rodano.api.configuration.security.Authority;
import ch.rodano.core.model.exception.NoEnabledRoleException;
import ch.rodano.core.model.exception.security.AlreadyUsedPassword;
import ch.rodano.core.model.exception.security.MustChangePasswordException;
import ch.rodano.core.model.exception.security.WeakPasswordException;
import ch.rodano.core.model.exception.security.WrongCredentialsException;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.session.SessionService;
import ch.rodano.core.services.dao.role.RoleDAOService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;

import static ch.rodano.core.services.bll.user.UserSecurityService.BCRYPT_STRENGTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
@Transactional
public class TwentyOneCFRTest extends DatabaseTest {
	@Autowired
	private UserDAOService userDAOService;

	@Autowired
	private UserSecurityService userSecurityService;

	@Autowired
	private RoleDAOService roleDAOService;

	@Autowired
	private ScopeService scopeService;

	private User createVerifiedUser() {
		final var user = new User();
		user.setName("John Doe");
		user.setEmail("john.doe@rodano.ch");
		user.setActivated(true);
		return user;
	}

	private String login(final User user, final String password) {
		final var sessionToken = userSecurityService.login(
			user.getEmail(),
			password,
			null,
			null,
			null,
			"",
			"",
			context
		);

		// Simulate the authentication procedure and set authentication.
		// This is necessary for the MustChangePassword interceptor
		final var authentication = new UsernamePasswordAuthenticationToken(user, sessionToken, Collections.singleton(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.name())));
		SecurityContextHolder.getContext().setAuthentication(authentication);

		return sessionToken;
	}

	// This test tests the fact that the user is identified by his e-mail and a personal password
	// TODO This test should probably be implemented in the API tests, as soon as they are functional
	@Test
	@DisplayName("USER_ID")
	public void testUserID() {
		final var user = createVerifiedUser();

		final var userPassword = "SuperStronkPassword!!!111111";
		final var incorrectPassword = "NotVeryStronkPassword1!!!!!!";

		userSecurityService.forceSetNewPassword(user, userPassword, context);

		// Try to retrieve a user with an incorrect e-mail
		assertNull(userDAOService.getUserByEmail("jane.doe@rodano.ch"));

		// Try login with incorrect password
		assertThrows(
			WrongCredentialsException.class,
			() -> login(user, incorrectPassword),
			"User can login with an incorrect password"
		);
	}

	// TODO This test should probably be implemented in the API tests, as soon as they are functional
	@Test
	@DisplayName("USER_CAN_CHANGE_PASSWORD")
	public void testPasswordChange() {
		final var user = createVerifiedUser();

		final var userPassword = "SuperStronkPassword!!!111111";
		userSecurityService.forceSetNewPassword(user, userPassword, context);

		final var newPassword = "ANOTHERSuperStronkPassword!!!111111";
		userSecurityService.changePassword(user, userPassword, newPassword, context);

		assertTrue(new BCryptPasswordEncoder(BCRYPT_STRENGTH).matches(newPassword, user.getPassword()));
	}

	@Test
	@DisplayName("CHECK_PASSWORD_LENGTH, REQUIRED_PASSWORD_CHARACTERS, COMPARE_TO_PREVIOUS_PASSWORDS")
	public void testPasswordStrength() {
		final var study = studyService.getStudy();

		study.setPasswordStrong(false);
		study.setPasswordUniqueness(false);

		final var user = createVerifiedUser();
		final var userPassword = "thePassword";

		userSecurityService.forceSetNewPassword(user, userPassword, context);
		assertNotNull(user.getPasswordChangedDate());
		assertFalse(user.isShouldChangePassword());

		//password versioning
		assertTrue(new BCryptPasswordEncoder().matches(userPassword, user.getPreviousPasswords()));

		//CHECK_PASSWORD_LENGTH password length
		study.setPasswordLength(BCRYPT_STRENGTH);
		assertThrows(
			WeakPasswordException.class,
			() -> userSecurityService.forceSetNewPassword(user, userPassword, context),
			"Password could be set even though it does not respect the password length parameter"
		);

		//REQUIRED_PASSWORD_CHARACTERS password required characters
		study.setPasswordStrong(true);
		assertThrows(
			WeakPasswordException.class,
			() -> userSecurityService.forceSetNewPassword(user, "thePasswordLong", context),
			"Password could be set even though it does not contain the required characters"
		);

		//COMPARE_TO_PREVIOUS_PASSWORDS previous passwords storage
		final var newUserPassword = "thePasswordLong1!";
		userSecurityService.forceSetNewPassword(user, newUserPassword, context);

		final var previousHashes = user.getPreviousPasswords().split(",");
		assertTrue(new BCryptPasswordEncoder(BCRYPT_STRENGTH).matches(userPassword, previousHashes[0]));
		assertTrue(new BCryptPasswordEncoder(BCRYPT_STRENGTH).matches(newUserPassword, previousHashes[1]));

		//reset password
		userSecurityService.forceSetNewPassword(user, "thePasswordLong1!", context);
	}

	@Test
	@DisplayName("LIMITED_PASSWORD_ATTEMPTS")
	public void testAuthenticationAttempts() {
		final var user = createVerifiedUser();
		final var userPassword = "thePasswordLong1?";

		userSecurityService.forceSetNewPassword(user, userPassword, context);
		user.setPasswordAttempts(0);
		user.setLoginBlockingDate(null);
		userDAOService.saveUser(user, context, TEST_RATIONALE);

		final var adminProfile = studyService.getStudy().getProfile("ADMIN");

		//try to connect without role
		assertThrows(
			NoEnabledRoleException.class,
			() -> login(user, userPassword),
			"A user can login without a role"
		);

		//add a role
		final var role = new Role();
		role.setProfile(adminProfile);
		role.setUserFk(user.getPk());
		role.setScopeFk(scopeService.getRootScope().getPk());
		role.enable();

		roleDAOService.saveRole(role, context, TEST_RATIONALE);

		//try to connect again
		final var sessionToken = login(user, "thePasswordLong1?");
		assertEquals(SessionService.SESSION_TOKEN_STRING_LENGTH, sessionToken.length());
		assertEquals(0, user.getPasswordAttempts());

		assertThrows(
			WrongCredentialsException.class,
			() -> login(user, "badPassword"),
			"User can login with a wrong password"
		);

		assertEquals(1, user.getPasswordAttempts());

		for(var i = 0; i < 4; i++) {
			assertThrows(
				WrongCredentialsException.class,
				() -> login(user, "badPassword"),
				"User can login with a wrong password"
			);
			assertEquals(2 + i, user.getPasswordAttempts());
		}

		//testing a wrong password again
		assertThrows(
			WrongCredentialsException.class,
			() -> login(user, "badPassword"),
			"User can login with a wrong password"
		);
		assertEquals(5, user.getPasswordAttempts());

		//cheating to reset date
		user.setLoginBlockingDate(ZonedDateTime.now().minusDays(1).minusSeconds(1));

		for(var i = 0; i < 2; i++) {
			assertThrows(
				WrongCredentialsException.class,
				() -> login(user, "badPassword"),
				"User can login with a wrong password"
			);
		}
		//login with good password
		login(user, "thePasswordLong1?");

		//testing a wrong password again
		for(var i = 0; i < 5; i++) {
			assertThrows(
				WrongCredentialsException.class,
				() -> login(user, "badPassword"),
				"User can login with a wrong password"
			);
		}

		//testing a good password
		assertThrows(
			WrongCredentialsException.class,
			() -> login(user, "thePasswordLong1?"),
			"Too many wrong login attempts have not been triggered"
		);
	}

	/**
	 * This tests the MustChangePasswordInterceptor which is the interceptor responsible for checking if a user must change password.
	 */
	@Test
	@DisplayName("MUST_CHANGE_PASSWORD")
	public void testNeedToChangePassword() {
		final var mustChangePasswordInterceptor = new MustChangePasswordInterceptor(userSecurityService);

		final var user = createVerifiedUser();
		final var userPassword = "thePasswordLong1?";

		userSecurityService.forceSetNewPassword(user, userPassword, context);

		final var adminProfile = studyService.getStudy().getProfile("ADMIN");

		//add a role
		final var role = new Role();
		role.setProfile(adminProfile);
		role.setUserFk(user.getPk());
		role.setScopeFk(scopeService.getRootScope().getPk());
		role.enable();
		roleDAOService.saveRole(role, context, TEST_RATIONALE);

		assertFalse(user.isShouldChangePassword());

		//set an old password changed date
		user.setPasswordChangedDate(ZonedDateTime.now().minusYears(10));
		login(user, userPassword);

		assertThrows(
			MustChangePasswordException.class,
			() -> mustChangePasswordInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null),
			"User can login although he must change his password"
		);

		user.setPasswordChangedDate(ZonedDateTime.now());
		login(user, userPassword);

		//MUST_CHANGE_PASSWORD user should change password
		user.setShouldChangePassword(true);
		login(user, userPassword);
		assertThrows(
			MustChangePasswordException.class,
			() -> mustChangePasswordInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null),
			"User can login although he must change his password"
		);

		final var newPassword = "thePasswordLong2?";
		userSecurityService.forceSetNewPassword(user, newPassword, context);

		login(user, "thePasswordLong2?");
		assertTrue(mustChangePasswordInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null));
	}

	@Test
	@DisplayName("COMPARE_TO_PREVIOUS_PASSWORDS")
	public void testChangePassword() {
		final var user = createVerifiedUser();
		final var userPassword = "thePasswordLong1?";

		//set same password twice
		userSecurityService.forceSetNewPassword(user, userPassword, context);

		studyService.getStudy().setPasswordUniqueness(true);
		assertThrows(
			AlreadyUsedPassword.class,
			() -> userSecurityService.forceSetNewPassword(user, userPassword, context),
			"User can set an already used password"
		);

		final var newUserPassword = "thePasswordLong10!";
		userSecurityService.forceSetNewPassword(user, newUserPassword, context);
		assertThrows(
			AlreadyUsedPassword.class,
			() -> userSecurityService.forceSetNewPassword(user, newUserPassword, context),
			"User can set an already used password"
		);
	}

	/**
	 * This tests the MustChangePasswordInterceptor which is the interceptor responsible for checking if a user must change password.
	 */
	@Test
	@DisplayName("PASSWORD_DURATION")
	public void testTimeToChangePassword() {
		final var mustChangePasswordInterceptor = new MustChangePasswordInterceptor(userSecurityService);

		final var study = studyService.getStudy();
		final var adminProfile = study.getProfile("ADMIN");

		study.setPasswordValidityDuration(0);

		final var user = createVerifiedUser();

		//add a role
		final var role = new Role();
		role.setProfile(adminProfile);
		role.enable();

		// Save user and role
		userDAOService.saveUser(user, context, TEST_RATIONALE);
		role.setUserFk(user.getPk());
		role.setScopeFk(scopeService.getRootScope().getPk());
		roleDAOService.saveRole(role, context, TEST_RATIONALE);

		final var userPassword = "thePasswordLong1?";
		userSecurityService.forceSetNewPassword(user, userPassword, context);
		user.setPasswordAttempts(0);

		//cheating to set an old password date
		user.setPasswordChangedDate(ZonedDateTime.now().minusDays(100));

		//PASSWORD_DURATION password validity date
		//try to connect with an old password without special configuration
		login(user, "thePasswordLong1?");

		//change config and try to connect with an old password
		study.setPasswordValidityDuration(30);
		login(user, "thePasswordLong1?");
		assertThrows(
			MustChangePasswordException.class,
			() -> mustChangePasswordInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null),
			"User can login with a previously used password"
		);

		//change password
		final var newUserPassword = "thePasswordLong2?";
		userSecurityService.forceSetNewPassword(user, newUserPassword, context);
		login(user, newUserPassword);
		assertTrue(mustChangePasswordInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null));
	}

	@RepeatedTest(10)
	@DisplayName("USER_LOGIN_TRAIL")
	public void testLoginTrail() {
		final var user = createVerifiedUser();
		final var password = "thePasswordLong1?";
		final var adminProfile = studyService.getStudy().getProfile("ADMIN");

		//add a role
		final var role = new Role();
		role.setProfile(adminProfile);
		role.enable();
		userDAOService.saveUser(user, context, TEST_RATIONALE);
		role.setUserFk(user.getPk());
		role.setScopeFk(scopeService.getRootScope().getPk());
		roleDAOService.saveRole(role, context, TEST_RATIONALE);

		userSecurityService.forceSetNewPassword(user, password, context);

		final var sessionToken = userSecurityService.login(
			user.getEmail(),
			password,
			0,
			null,
			null,
			"fakeURL",
			"fakeAgent",
			context
		);

		final var now = LocalDateTime.now();

		final var userTrails = userDAOService.getAuditTrails(user, Optional.empty(), Optional.empty());
		final var lastUserTrail = userTrails.last();
		final var loginDateString = String.join("T", lastUserTrail.getLoginDate().toString().split(" "));
		final var loginDate = LocalDateTime.parse(loginDateString, DateTimeFormatter.ISO_DATE_TIME);
		assertTrue(areDatesWithinASecond(now, loginDate));

		// Logout the user
		userSecurityService.logout(user, sessionToken, context);

		// Check if the logout date is trailed correctly
		final var newUserTrails = userDAOService.getAuditTrails(user, Optional.empty(), Optional.empty());
		final var newLastUserTrail = newUserTrails.last();
		final var logoutDateString = String.join("T", newLastUserTrail.getLogoutDate().toString().split(" "));
		final var logoutDate = LocalDateTime.parse(logoutDateString, DateTimeFormatter.ISO_DATE_TIME);
		assertTrue(areDatesWithinASecond(now, logoutDate));

		final var fakePassword = "FakestPWD!!!11111";
		assertThrows(
			WrongCredentialsException.class,
			() -> login(user, fakePassword),
			"User could login with a wrong password"
		);

		// Check if the login blocking date is trailed correctly
		final var loginBlockingTrails = userDAOService.getAuditTrails(user, Optional.empty(), Optional.empty());
		final var loginBlockingTrail = loginBlockingTrails.last();
		final var loginBlockingDateString = String.join("T", loginBlockingTrail.getLoginBlockingDate().toString().split(" "));
		final var loginBlockingDate = LocalDateTime.parse(loginBlockingDateString, DateTimeFormatter.ISO_DATE_TIME);
		assertTrue(areDatesWithinASecond(now, loginBlockingDate));

		// Check if the number of unsuccessful password attempts is trailed correctly
		final var passwordAttemptsTrails = userDAOService.getAuditTrails(user, Optional.empty(), Optional.empty());
		final var passwordAttemptsTrail = passwordAttemptsTrails.last();
		final var passwordAttempts = passwordAttemptsTrail.getPasswordAttempts();
		assertEquals(1, passwordAttempts);
	}
}
