package ch.rodano.core.services.bll;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.mail.MailStatus;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.role.RoleStatus;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.mail.MailService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.dao.role.RoleDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.core.utils.RightsService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
@Transactional
public class RoleServiceTest extends DatabaseTest {
	@Autowired
	private UserDAOService userDAOService;

	@Autowired
	private ActorService actorService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private RoleDAOService roleDAOService;

	@Autowired
	private ScopeDAOService scopeDAOService;

	@Autowired
	private RightsService rightsService;

	@Autowired
	private MailService mailService;

	Scope rootScope;
	Profile adminProfile;
	Profile investigatorProfile;
	User user;
	User unactivatedUser;

	@BeforeAll
	public void initRoleTests() {
		rootScope = scopeDAOService.getRootScope();
		adminProfile = studyService.getStudy().getProfile("ADMIN");
		investigatorProfile = studyService.getStudy().getProfile("INVESTIGATOR");
	}

	@BeforeEach
	public void initUser() {
		user = createUser();
		unactivatedUser = createUnactivatedUser();
	}

	@Test
	@DisplayName("Role creation")
	public void roleCreationTest() {
		final var role = roleService.createRole(
			user,
			adminProfile,
			rootScope,
			context
		);

		assertEquals(user.getPk(), role.getUserFk());
		assertEquals(role.getScopeFk(), rootScope.getPk());
		assertEquals(role.getStatus(), RoleStatus.PENDING);

		// search for the role invitation mail
		final var userMails = mailService.getMails(MailStatus.PENDING, Integer.MAX_VALUE).stream()
			.filter(mail -> mail.getRecipients().contains(user.getEmail()))
			.toList();

		assertEquals(1, userMails.size());

		// check that the role creation e-mail is sent correctly
		assertTrue(
			mailService.getMails(MailStatus.PENDING, Integer.MAX_VALUE).stream()
				.anyMatch(mail -> {
					return mail.getRecipients().contains(user.getEmail()) && mail.getSubject().contains("role creation") && mail.getTextBody().contains(
						role.getProfile().getDefaultLocalizedShortname()
					);
				})
		);
	}

	@Test
	@DisplayName("If a user adds a role to themselves, it is instantly enabled")
	public void selfRoleCreationEnableTest() {
		// Set the user in the context
		final var customContext = new DatabaseActionContext(context.auditAction(), Optional.of(user));

		final var role = roleService.createRole(
			user,
			adminProfile,
			rootScope,
			customContext
		);

		assertEquals(user.getPk(), role.getUserFk());
		assertEquals(role.getScopeFk(), rootScope.getPk());
		assertEquals(role.getStatus(), RoleStatus.ENABLED);

		// search for the role invitation mail
		mailService.getMails(MailStatus.PENDING, Integer.MAX_VALUE).stream()
			.filter(mail -> mail.getRecipients().contains(user.getEmail()))
			.toList();

		// check that no role activation mail has been sent
		assertFalse(
			mailService.getMails(MailStatus.PENDING, Integer.MAX_VALUE).stream()
				.anyMatch(mail -> {
					return mail.getRecipients().contains(user.getEmail()) && mail.getSubject().contains("role creation") && mail.getTextBody().contains(
						role.getProfile().getDefaultLocalizedShortname()
					);
				})
		);
	}

	@Test
	@DisplayName("Role enable")
	public void roleEnableTest() {
		// create an investigator role on a user
		final var role = createAndEnableInvestigatorRole(user);

		assertEquals(RoleStatus.ENABLED, role.getStatus());

		// Check that the role enable notification was sent correctly
		assertTrue(
			mailService.getMails(MailStatus.PENDING, Integer.MAX_VALUE).stream()
				.anyMatch(mail -> {
					return mail.getRecipients().contains(user.getEmail()) && mail.getSubject().contains("role activation") && mail.getTextBody().contains(
						role.getProfile().getDefaultLocalizedShortname()
					);
				})
		);
	}

	@Test
	@DisplayName("Already enabled role can not be enabled")
	public void enabledRoleCanNotBeEnabled() {
		// create an investigator role on a user
		final var role = createAndEnableInvestigatorRole(user);

		assertThrows(
			UnsupportedOperationException.class,
			() -> roleService.enableRole(
				user,
				role,
				context
			),
			"Enabled role can be enabled"
		);
	}

	@Test
	@DisplayName("User that has not been activated can not have their role enabled")
	public void userWithoutPasswordRoleCanNotBeEnabled() {
		// create a user with a role
		final var role = roleService.createRole(
			unactivatedUser,
			investigatorProfile,
			rootScope,
			context
		);

		assertThrows(
			UnsupportedOperationException.class,
			() -> roleService.enableRole(
				unactivatedUser,
				role,
				context
			),
			"Enabled role can be enabled"
		);
	}

	@Test
	@DisplayName("Externally managed user can be enabled without password")
	public void externallyManagedUserCanBeEnabledWithoutPassword() {
		unactivatedUser.setExternallyManaged(true);
		userDAOService.saveUser(unactivatedUser, context, TEST_RATIONALE);

		final var role = roleService.createRole(
			unactivatedUser,
			investigatorProfile,
			rootScope,
			context
		);
		roleService.enableRole(
			unactivatedUser,
			role,
			context
		);

		assertTrue(role.isEnabled());
	}

	@Test
	@DisplayName("If the user has only one active role, it can not be disabled")
	public void lastUserRoleCanNotBeDisabled() {
		// create an investigator role on a user
		final var role = createAndEnableInvestigatorRole(user);

		assertThrows(
			UnsupportedOperationException.class,
			() -> roleService.disableRole(user, role, context),
			"Last active user role can be disabled"
		);
	}

	@Test
	@DisplayName("User root scope is based on user role")
	public void userRootScopeTest() {
		// user root scope is empty on creation
		assertEquals(Optional.empty(), actorService.getRootScope(user));

		// when a role is assigned, it should return the root scope of the role
		final var userRole = createAndEnableInvestigatorRole(user);
		final var rootScope = actorService.getRootScope(user).orElseThrow();
		assertEquals(rootScope, scopeDAOService.getScopeByPk(userRole.getScopeFk()));
	}

	// TODO this test should be in a separate rights test class
	@Test
	@DisplayName("Test the user root scope rights")
	public void testSimpleRights() {
		final var study = studyService.getStudy();
		final var center = scopeDAOService.getScopeByCode("FR-01");
		final var role = roleService.createRole(
			user,
			investigatorProfile,
			center,
			context
		);
		roleService.enableRole(
			user,
			role,
			context
		);

		assertAll(
			"Check the root scope",
			() -> assertEquals(center, actorService.getRootScope(user, FeatureStatic.NOTIFY_RESOURCE_PUBLISHED).orElseThrow()),
			() -> assertEquals(center, actorService.getRootScope(user, study.getProfile(role.getProfileId())).orElseThrow())
		);
	}

	// TODO this test should be in a separate rights test class
	@Test
	@DisplayName("Investigator does not have admin rights")
	public void investigatorDoesNotHaveAdminRights() {
		createAndEnableInvestigatorRole(user);

		assertFalse(rightsService.hasRightAdmin(roleService.getActiveRoles(user)));
	}

	// TODO this test should be in a separate rights test class
	@Test
	@DisplayName("Check the role enable and disable operations")
	public void testComplexRights() {
		final var study = studyService.getStudy();
		final var studyScope = scopeDAOService.getScopeByCode("Test");
		final var center = scopeDAOService.getScopeByCode("FR-01");
		final var role = roleService.createRole(
			user,
			investigatorProfile,
			center,
			context
		);
		roleService.enableRole(
			user,
			role,
			context
		);

		//add an extra admin role
		final var adminRole = addAdminRole(user);

		assertAll(
			"Check enabled roles",
			() -> assertTrue(adminRole.isEnabled()),
			() -> assertEquals(2, roleService.getActiveRoles(user).size()),
			() -> assertEquals(1, roleService.getActiveRoles(user, studyScope).size()),

			() -> assertEquals(2, actorService.getActiveProfiles(user).size()),
			() -> assertEquals(1, actorService.getActiveProfiles(user, studyScope).size()),
			() -> assertEquals(2, actorService.getActiveProfiles(user, center).size()),

			() -> assertEquals(studyScope, actorService.getRootScope(user, FeatureStatic.NOTIFY_RESOURCE_PUBLISHED).orElseThrow()),
			() -> assertNotSame(actorService.getRootScope(user, study.getProfile("INVESTIGATOR")).orElseThrow(), studyScope)
		);

		// disable the admin role
		roleService.disableRole(user, adminRole, context);

		assertAll(
			"Check that the admin role has been disabled",
			() -> assertFalse(adminRole.isEnabled()),
			() -> assertEquals(1, roleService.getActiveRoles(user).size()),
			() -> assertEquals(1, roleService.getActiveRoles(user, center).size()),

			() -> assertEquals(1, actorService.getActiveProfiles(user).size()),
			() -> assertEquals(1, actorService.getActiveProfiles(user, center).size()),

			() -> assertEquals(center, actorService.getRootScope(user, FeatureStatic.NOTIFY_RESOURCE_PUBLISHED).orElseThrow()),
			() -> assertFalse(rightsService.hasRightAdmin(roleService.getActiveRoles(user)))
		);
	}

	private User createUser() {
		final var user = new User();
		user.setName("Luke Skywalker");
		user.setEmail("luke.skywalker@rodano.ch");
		user.setPassword("VERySeKuREPass!@#");
		user.setActivated(true);
		userDAOService.saveUser(user, context, TEST_RATIONALE);
		return user;
	}

	private Role createAndEnableInvestigatorRole(final User user) {
		final var role = roleService.createRole(
			user,
			investigatorProfile,
			rootScope,
			context
		);

		// enable the created role
		roleService.enableRole(
			user,
			role,
			context
		);

		return role;
	}

	private User createUnactivatedUser() {
		final var user = new User();
		user.setName("Darth Vader");
		user.setEmail("darth.vader@rodano.ch");
		userDAOService.saveUser(user, context, TEST_RATIONALE);
		return user;
	}

	private Role addAdminRole(final User user) {
		final var role = new Role();
		role.setProfile(adminProfile);
		role.setUserFk(user.getPk());
		role.setScopeFk(scopeDAOService.getRootScope().getPk());
		role.enable();
		roleDAOService.saveRole(role, context, TEST_RATIONALE);
		return role;
	}
}
