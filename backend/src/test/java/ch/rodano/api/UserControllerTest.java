package ch.rodano.api;

import java.util.Set;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ch.rodano.api.actor.ChangeEmailDTO;
import ch.rodano.api.actor.RobotCreationDTO;
import ch.rodano.api.actor.RobotDTO;
import ch.rodano.api.actor.UserDTO;
import ch.rodano.api.role.RoleCreationDTO;
import ch.rodano.core.model.mail.MailTemplate;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.user.UserService;
import ch.rodano.test.ControllerTest;
import ch.rodano.test.SpringTestConfiguration;

import static ch.rodano.core.model.jooq.tables.Mail.MAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
public class UserControllerTest extends ControllerTest {

	@Autowired
	private UserService userService;

	@Autowired
	private DSLContext create;

	@Test
	@DisplayName("A user can change their own email through verification")
	public void userCanChangeOwnEmail() {
		authenticate(investigatorOnStudyEmail);
		final var user = userService.getUserByEmail(investigatorOnStudyEmail);
		final var newEmail = "investigator-new@rodano.ch";

		final var updatedUser = client.post().uri("/users/{userPk}/email", user.getPk())
			.body(new ChangeEmailDTO(newEmail, TEST_USER_PASSWORD))
			.exchange().expectStatus().isOk().expectBody(UserDTO.class).returnResult().getResponseBody();

		assertEquals(newEmail, updatedUser.getPendingEmail());
		assertEquals(investigatorOnStudyEmail, updatedUser.getEmail());
	}

	@Test
	@DisplayName("An administrator changes emails directly and the user is notified at their previous email")
	public void adminChangesEmailDirectly() {
		//create a pending email change that the administrator change must discard
		authenticate(investigatorOnStudyEmail);
		final var user = userService.getUserByEmail(investigatorOnStudyEmail);
		client.post().uri("/users/{userPk}/email", user.getPk())
			.body(new ChangeEmailDTO("obsolete@rodano.ch", TEST_USER_PASSWORD))
			.exchange().expectStatus().isOk();

		authenticate(adminOnStudyEmail);
		assertDirectChange(user);
	}

	@Test
	@DisplayName("An administrator can change their own email directly")
	public void adminChangesOwnEmailDirectly() {
		authenticate(adminOnStudyEmail);
		assertDirectChange(userService.getUserByEmail(adminOnStudyEmail));
	}

	@Test
	@DisplayName("An administrator must confirm their password to change an email")
	public void adminMustConfirmPassword() {
		authenticate(adminOnStudyEmail);
		final var user = userService.getUserByEmail(investigatorOnStudyEmail);
		final var lastMailPk = getLastMailPk();
		for(final var password : new String[] { "", "incorrect-password" }) {
			client.post().uri("/users/{userPk}/email", user.getPk())
				.body(new ChangeEmailDTO("unconfirmed@rodano.ch", password))
				.exchange().expectStatus().isUnauthorized();
		}
		client.post().uri("/users/{userPk}/email", user.getPk())
			.body(new ChangeEmailDTO("unconfirmed@rodano.ch", null))
			.exchange().expectStatus().isBadRequest();

		unitOfWorkService.clear();
		assertEquals(investigatorOnStudyEmail, userService.getUserByPk(user.getPk()).getEmail());
		assertEquals(0, create.fetchCount(MAIL, MAIL.PK.gt(lastMailPk)));
	}

	@Test
	@DisplayName("Robots cannot change emails, even with administrator rights")
	public void robotsCannotChangeEmail() {
		final var robot = createAdminRobot();
		final var user = userService.getUserByEmail(investigatorOnStudyEmail);
		client.post().uri("/users/{userPk}/email", user.getPk())
			.headers(headers -> headers.setBasicAuth(robot.getName(), robot.getKey()))
			.body(new ChangeEmailDTO("robot@rodano.ch", robot.getKey()))
			.exchange().expectStatus().isUnauthorized();

		unitOfWorkService.clear();
		assertEquals(investigatorOnStudyEmail, userService.getUserByPk(user.getPk()).getEmail());
	}

	@Test
	@DisplayName("A non-administrator cannot change another user's email")
	public void userCannotChangeAnotherUsersEmail() {
		authenticate(dataManagerOnStudyEmail);
		final var user = userService.getUserByEmail(adminOnStudyEmail);
		final var pendingEmail = user.getPendingEmail();
		client.post().uri("/users/{userPk}/email", user.getPk())
			.body(new ChangeEmailDTO("unauthorized@rodano.ch", TEST_USER_PASSWORD))
			.exchange().expectStatus().isUnauthorized();

		unitOfWorkService.clear();
		final var unchangedUser = userService.getUserByPk(user.getPk());
		assertEquals(adminOnStudyEmail, unchangedUser.getEmail());
		assertEquals(pendingEmail, unchangedUser.getPendingEmail());
	}

	@Test
	@DisplayName("Self-service email changes require the correct password")
	public void userMustConfirmPassword() {
		authenticate(investigatorOnStudyEmail);
		final var user = userService.getUserByEmail(investigatorOnStudyEmail);
		client.post().uri("/users/{userPk}/email", user.getPk())
			.body(new ChangeEmailDTO("unconfirmed@rodano.ch", "incorrect-password"))
			.exchange().expectStatus().isUnauthorized();
	}

	@Test
	@DisplayName("Administrators cannot assign an invalid or already used email")
	public void adminEmailMustBeValidAndUnique() {
		authenticate(adminOnStudyEmail);
		final var user = userService.getUserByEmail(investigatorOnStudyEmail);
		for(final var email : new String[] { "invalid", adminOnStudyEmail }) {
			client.post().uri("/users/{userPk}/email", user.getPk())
				.body(new ChangeEmailDTO(email, TEST_USER_PASSWORD))
				.exchange().expectStatus().isBadRequest();
		}
	}

	@Test
	@DisplayName("Anonymous callers cannot change a user's email")
	public void anonymousCannotChangeEmail() {
		final var user = userService.getUserByEmail(adminOnStudyEmail);
		client.post().uri("/users/{userPk}/email", user.getPk())
			.body(new ChangeEmailDTO("anonymous@rodano.ch", TEST_USER_PASSWORD))
			.exchange().expectStatus().isUnauthorized();
	}

	private RobotDTO createAdminRobot() {
		authenticate(adminOnStudyEmail);
		final var role = new RoleCreationDTO();
		role.setScopePk(1L);
		role.setProfileId("ADMIN");
		return post("/robots", new RobotCreationDTO(UUID.randomUUID().toString(), role, null), RobotDTO.class);
	}

	private long getLastMailPk() {
		return create.select(DSL.coalesce(DSL.max(MAIL.PK), 0L)).from(MAIL).fetchOne(0, Long.class);
	}

	/**
	 * Change the email of the user as the currently authenticated administrator and check that the change is direct
	 */
	private void assertDirectChange(final User user) {
		final var originalEmail = user.getEmail();
		final var newEmail = UUID.randomUUID() + "@rodano.ch";
		final var lastMailPk = getLastMailPk();
		try {
			final var updatedUser = client.post().uri("/users/{userPk}/email", user.getPk())
				.body(new ChangeEmailDTO(newEmail, TEST_USER_PASSWORD))
				.exchange().expectStatus().isOk().expectBody(UserDTO.class).returnResult().getResponseBody();
			assertEquals(newEmail, updatedUser.getEmail());
			assertNull(updatedUser.getPendingEmail());

			unitOfWorkService.clear();
			final var savedUser = userService.getUserByPk(user.getPk());
			assertEquals(newEmail, savedUser.getEmail());
			assertNull(savedUser.getPendingEmail());
			assertNull(savedUser.getEmailVerificationCode());
			assertNull(savedUser.getEmailModificationDate());

			//only the notification to the previous email must be sent
			final var mails = create.selectFrom(MAIL).where(MAIL.PK.gt(lastMailPk)).fetch();
			assertEquals(1, mails.size());
			final var mail = mails.get(0);
			assertEquals(Set.of(originalEmail), mail.getRecipients());
			assertEquals(MailTemplate.EMAIL_CHANGED_BY_ADMINISTRATOR.getSubject(), mail.getSubject());
			assertTrue(mail.getTextBody().contains(newEmail));
		}
		finally {
			client.post().uri("/users/{userPk}/email", user.getPk())
				.body(new ChangeEmailDTO(originalEmail, TEST_USER_PASSWORD))
				.exchange().expectStatus().isOk();
			unitOfWorkService.clear();
		}
	}
}
