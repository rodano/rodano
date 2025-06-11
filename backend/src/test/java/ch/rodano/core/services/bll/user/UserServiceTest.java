package ch.rodano.core.services.bll.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.controller.user.exception.InvalidEmailException;
import ch.rodano.core.model.mail.MailStatus;
import ch.rodano.core.model.role.RoleStatus;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.mail.MailService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
@Transactional
public class UserServiceTest extends DatabaseTest {

	@Autowired
	private UserService userService;

	@Autowired
	private UserDAOService userDAOService;

	@Autowired
	private ScopeService scopeService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private MailService mailService;

	@Test
	@DisplayName("User creation works")
	public void userCreationTest() {
		// create a user with a role
		final var user = new User();
		user.setName("John Doe");
		user.setEmail("john.doe@rodano.ch");
		user.setExternallyManaged(false);

		final var investigatorProfile = studyService.getStudy().getProfile("INVESTIGATOR");
		final var rootScope = scopeService.getRootScope();

		final var createdUser = userService.createUser(
			user,
			investigatorProfile,
			rootScope,
			"rodano.ch",
			context
		);
		final var createdRole = roleService.getRoles(user).getFirst();

		// check that the user is not activated and their activation code is set
		assertFalse(user.isActivated());
		assertNotNull(user.getActivationCode());

		// check that the user role is pending
		assertEquals(RoleStatus.PENDING, createdRole.getStatus());

		// check that the user account activation e-mail is sent
		assertTrue(
			mailService.getMails(MailStatus.PENDING, Integer.MAX_VALUE).stream()
				.anyMatch(mail -> {
					return mail.getRecipients().contains(createdUser.getEmail()) && mail.getSubject().contains("account activation") && mail.getTextBody().contains(user.getActivationCode());
				})
		);
	}

	@Test
	@DisplayName("Externally managed user creation works")
	public void extManagedUserCreationTest() {
		// create a user with a role
		final var user = new User();
		user.setName("Jane Doe");
		user.setEmail("jane.doe@rodano.ch");
		user.setExternallyManaged(true);

		final var investigatorProfile = studyService.getStudy().getProfile("INVESTIGATOR");
		final var rootScope = scopeService.getRootScope();

		final var createdUser = userService.createUser(
			user,
			investigatorProfile,
			rootScope,
			"rodano.ch",
			context
		);
		final var createdAndEnabledRole = roleService.getRoles(user).getFirst();

		// externally managed user must be already activated
		assertTrue(user.isActivated());
		assertNull(user.getActivationCode());

		// check that the user role is already enabled
		assertEquals(RoleStatus.ENABLED, createdAndEnabledRole.getStatus());

		// check that the user account activation e-mail has not been sent
		assertFalse(
			mailService.getMails(MailStatus.PENDING, Integer.MAX_VALUE).stream()
				.anyMatch(mail -> {
					return mail.getRecipients().contains(createdUser.getEmail()) && mail.getSubject().contains("account activation") && mail.getTextBody().contains(user.getActivationCode());
				})
		);
	}

	@Test
	@DisplayName("Can not save a user who does not have a name")
	public void canNotCreateUserWithoutAName() {
		final var user = new User();

		assertThrows(
			org.jooq.exception.IntegrityConstraintViolationException.class,
			() -> userDAOService.saveUser(user, context, TEST_RATIONALE),
			"The user is saved with no name provided"
		);
	}

	@Test
	@DisplayName("Can not save a user with an invalid e-mail")
	public void canNotCreateUserWithInvalidEmail() {
		final var user = new User();
		user.setName("John Doe");
		user.setEmail("john.doe@rodano.ch ");

		final var adminProfile = studyService.getStudy().getProfile("ADMIN");

		assertThrows(
			InvalidEmailException.class,
			() -> userService.createUser(
				user,
				adminProfile,
				scopeService.getRootScope(),
				"test.link",
				context
			),
			"The user is created with an invalid e-mail"
		);
	}
}
