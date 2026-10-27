package ch.rodano.core.services.bll.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.controller.user.exception.UserNotActivatedException;
import ch.rodano.api.controller.user.exception.UserRemovedException;
import ch.rodano.core.model.exception.security.WrongCredentialsException;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringTestConfiguration
@Transactional
public class UserSecurityServiceTest extends DatabaseTest {
	private static final String TEST_USER_EMAIL = "test+test-admin@rodano.ch";

	@Autowired
	private UserService userService;

	@Autowired
	private UserSecurityService userSecurityService;

	@Autowired
	private UserDAOService userDAOService;

	@Test
	@DisplayName("Deleted user can not log into the system")
	public void deletedUserCanNotLogin() {
		final var user = userService.getUserByEmail(TEST_USER_EMAIL);
		assertFalse(login(user, TEST_USER_PASSWORD).isEmpty());

		userService.deleteUser(user, context, "Testing");

		final var exception = assertThrows(UserRemovedException.class, () -> login(user, TEST_USER_PASSWORD));
		assertEquals("The user has been removed", exception.getMessage());
	}

	@Test
	@DisplayName("User who has not activated their account can not log into the system")
	public void notActivatedUserCanNotLogin() {
		final var user = userService.getUserByEmail(TEST_USER_EMAIL);
		user.setActivated(false);
		userDAOService.saveUser(user, context, "Testing");

		final var exception = assertThrows(UserNotActivatedException.class, () -> login(user, TEST_USER_PASSWORD));
		assertEquals("The user has not been activated", exception.getMessage());
	}

	@Test
	@DisplayName("Removed and not activated accounts get an explicit feedback whatever the password")
	public void removedAndNotActivatedAccountsGetExplicitFeedback() {
		final var removedUser = userService.getUserByEmail(TEST_USER_EMAIL);
		userService.deleteUser(removedUser, context, "Testing");
		assertThrows(UserRemovedException.class, () -> login(removedUser, "wrong password"));

		final var notActivatedUser = userService.getUserByEmail("test+test-investigator@rodano.ch");
		notActivatedUser.setActivated(false);
		notActivatedUser.setPassword(null);
		userDAOService.saveUser(notActivatedUser, context, "Testing");
		assertThrows(UserNotActivatedException.class, () -> login(notActivatedUser, "wrong password"));

		//these failed attempts must not lock accounts that can not log in anyway
		assertEquals(0, userService.getUserByPk(removedUser.getPk()).getPasswordAttempts());
		assertEquals(0, userService.getUserByPk(notActivatedUser.getPk()).getPasswordAttempts());
	}

	@Test
	@DisplayName("An activated account without password is reported as wrong credentials")
	public void accountWithoutPasswordIsReportedAsWrongCredentials() {
		final var user = userService.getUserByEmail(TEST_USER_EMAIL);
		user.setPassword(null);
		userDAOService.saveUser(user, context, "Testing");
		assertThrows(WrongCredentialsException.class, () -> login(user, TEST_USER_PASSWORD));
	}

	private String login(final User user, final String password) {
		return userSecurityService.login(user.getEmail(), password, null, "", "", "", "Test Agent", context);
	}
}
