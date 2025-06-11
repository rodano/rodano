package ch.rodano.core.services.bll.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.controller.user.exception.UserNotActivatedException;
import ch.rodano.core.database.initializer.DatabaseInitializer;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringTestConfiguration
@Transactional
public class UserSecurityServiceTest extends DatabaseTest {

	@Autowired
	private UserService userService;

	@Autowired
	private UserSecurityService userSecurityService;

	@Test
	@DisplayName("Deleted user can not log into the system")
	public void deletedUserCanNotLogin() {
		final var adminUser = userService.getUserByEmail("test+test-admin@rodano.ch");

		// See if the login works
		final var sessionToken = userSecurityService.login(
			adminUser.getEmail(),
			DatabaseInitializer.DEFAULT_PASSWORD,
			0,
			null,
			null,
			"",
			"",
			context
		);

		assertFalse(sessionToken.isEmpty());

		// Delete the user and see that the exception is thrown
		userService.deleteUser(adminUser, context, "Testing");
		assertThrows(
			UserNotActivatedException.class,
			() -> userSecurityService.login(
				adminUser.getEmail(),
				DatabaseInitializer.DEFAULT_PASSWORD,
				null,
				"",
				"",
				"",
				"Test Agent",
				context
			)
		);
	}
}
