package ch.rodano.api;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ch.rodano.api.authentication.ChangePasswordDTO;
import ch.rodano.api.authentication.ResetPasswordDTO;
import ch.rodano.core.helpers.UserCreatorService;
import ch.rodano.core.helpers.builder.UserBuilder;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.user.UserService;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.test.ControllerTest;
import ch.rodano.test.SpringTestConfiguration;

import static ch.rodano.core.model.jooq.tables.User.USER;
import static ch.rodano.core.model.jooq.tables.UserSession.USER_SESSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Check that tokens already issued to a user stop working when the account or its credentials change
 */
@SpringTestConfiguration
public class SessionRevocationTest extends ControllerTest {
	private static final String NEW_PASSWORD = "AnotherPassword2!";

	@Autowired
	private UserCreatorService userCreatorService;

	@Autowired
	private ScopeService scopeService;

	@Autowired
	private DSLContext create;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private UserService userService;

	private final List<User> createdUsers = new ArrayList<>();

	/**
	 * Remove the users created by the test, as controller tests commit their data and other tests count users
	 */
	@AfterEach
	protected void removeCreatedUsers() {
		unitOfWorkService.clear();
		new TransactionTemplate(transactionManager).executeWithoutResult(_ -> {
			final var removalContext = createDatabaseActionContext();
			for(final var createdUser : createdUsers) {
				final var user = userService.getUserByPk(createdUser.getPk());
				if(!user.isRemoved()) {
					userService.deleteUser(user, removalContext, "Remove test user");
				}
			}
		});
		createdUsers.clear();
	}

	@Test
	@DisplayName("Removing a user revokes the tokens already issued to them")
	public void removingUserRevokesSessions() {
		final var user = createUser();
		authenticate(user.getEmail());
		final var userToken = token.get();
		assertTokenAccepted(userToken);

		authenticate(adminOnStudyEmail);
		client.put().uri("/users/{userPk}/remove", user.getPk()).exchange().expectStatus().isOk();

		assertTokenRejected(userToken);
		assertEquals(0, countSessions(user));
	}

	@Test
	@DisplayName("Tokens of removed or deactivated users are rejected even if their session still exists")
	public void inactiveUsersTokensAreRejected() {
		final var removedUser = createUser();
		authenticate(removedUser.getEmail());
		final var removedUserToken = token.get();
		final var deactivatedUser = createUser();
		authenticate(deactivatedUser.getEmail());
		final var deactivatedUserToken = token.get();

		//alter the accounts directly so their sessions are kept, as would be the case for sessions created before the revocation on removal
		new TransactionTemplate(transactionManager).executeWithoutResult(_ -> {
			create.update(USER).set(USER.REMOVED, true).where(USER.PK.eq(removedUser.getPk())).execute();
			create.update(USER).set(USER.ACTIVATED, false).where(USER.PK.eq(deactivatedUser.getPk())).execute();
		});
		unitOfWorkService.clear();

		assertTokenRejected(removedUserToken);
		assertTokenRejected(deactivatedUserToken);
	}

	@Test
	@DisplayName("Resetting a password revokes all the tokens issued to the user")
	public void resettingPasswordRevokesSessions() {
		final var user = createUser();
		authenticate(user.getEmail());
		final var firstToken = token.get();
		authenticate(user.getEmail());
		final var secondToken = token.get();
		assertEquals(2, countSessions(user));

		final var resetCode = UUID.randomUUID().toString();
		new TransactionTemplate(transactionManager).executeWithoutResult(_ -> create.update(USER)
			.set(USER.PASSWORD_RESET_CODE, resetCode)
			.set(USER.PASSWORD_RESET_DATE, ZonedDateTime.now())
			.where(USER.PK.eq(user.getPk()))
			.execute()
		);
		token = Optional.empty();
		client.post().uri("/auth/password/reset")
			.body(new ResetPasswordDTO(resetCode, NEW_PASSWORD))
			.exchange().expectStatus().isNoContent();

		assertTokenRejected(firstToken);
		assertTokenRejected(secondToken);
		assertEquals(0, countSessions(user));
	}

	@Test
	@DisplayName("Changing a password revokes the other tokens and keeps the one used for the change")
	public void changingPasswordRevokesOtherSessions() {
		final var user = createUser();
		authenticate(user.getEmail());
		final var otherToken = token.get();
		authenticate(user.getEmail());
		final var currentToken = token.get();
		assertNotEquals(otherToken, currentToken);

		client.post().uri("/auth/password/change")
			.body(new ChangePasswordDTO(TEST_USER_PASSWORD, NEW_PASSWORD))
			.exchange().expectStatus().isCreated();

		assertTokenAccepted(currentToken);
		assertTokenRejected(otherToken);
		assertEquals(1, countSessions(user));
	}

	private User createUser() {
		final var email = "test+session-" + UUID.randomUUID() + "@rodano.ch";
		final var builder = UserBuilder.createUser("Session User", email)
			.setHashedPassword(new BCryptPasswordEncoder(UserSecurityService.BCRYPT_STRENGTH).encode(TEST_USER_PASSWORD))
			.addRole(scopeService.getRootScope(), studyService.getStudy().getProfile("INVESTIGATOR"));
		//controller tests are not transactional, so the creation and its audit action are committed in a dedicated transaction
		final var user = new TransactionTemplate(transactionManager).execute(_ -> userCreatorService.createAndEnable(builder, createDatabaseActionContext()));
		createdUsers.add(user);
		return user;
	}

	private void assertTokenAccepted(final String userToken) {
		token = Optional.of(userToken);
		client.get().uri("/me").exchange().expectStatus().isOk();
	}

	private void assertTokenRejected(final String userToken) {
		token = Optional.of(userToken);
		client.get().uri("/me").exchange().expectStatus().isUnauthorized();
	}

	private int countSessions(final User user) {
		return create.fetchCount(USER_SESSION, USER_SESSION.USER_FK.eq(user.getPk()));
	}
}
