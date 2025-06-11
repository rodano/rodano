package ch.rodano.core.services.dao;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ch.rodano.core.database.initializer.DatabaseInitializer;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.ScopeExtension;
import ch.rodano.core.model.user.User;
import ch.rodano.core.model.user.UserSearch;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
public class UserDAOServiceTest extends DatabaseTest {
	@Autowired
	private RoleService roleService;

	@Autowired
	private UserDAOService userDAOService;

	@Autowired
	private ScopeService scopeService;

	@Autowired
	private ScopeDAOService scopeDAOService;

	@Test
	@DisplayName("Retrieve user")
	public void testRetrieveUser() {
		//retrieve user from email
		assertEquals(DatabaseInitializer.TEST_USER_EMAIL, userDAOService.getUserByEmail(DatabaseInitializer.TEST_USER_EMAIL).getEmail());
	}

	@Test
	@DisplayName("Find investigators on center")
	public void testFindInvestigatorsOnCenter() {
		final var predicate = new UserSearch();
		predicate.enforceScopePks(Collections.singleton(scopeDAOService.getScopeByCode("FR-01").getPk()));
		predicate.enforceExtension(ScopeExtension.DESCENDANTS);
		predicate.enforceProfileIds(Collections.singleton("INVESTIGATOR"));
		predicate.enforceEnabled(true);

		final var users = userDAOService.search(predicate).getObjects();
		assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("test+iinves@rodano.ch")));
	}

	@Test
	@DisplayName("Find investigators on study")
	public void testFindInvestigatorsOnStudy() {
		final var predicate = new UserSearch();
		predicate.enforceScopePks(Collections.singleton(scopeService.getRootScope().getPk()));
		predicate.enforceExtension(ScopeExtension.NONE);
		predicate.enforceProfileIds(Collections.singleton("INVESTIGATOR"));
		predicate.enforceEnabled(true);

		final var users = userDAOService.search(predicate).getObjects();
		assertAll(
			"Find the investigator on study",
			() -> assertEquals(1, users.size()),
			() -> assertTrue(users.stream().flatMap(u -> roleService.getActiveRoles(u).stream()).map(Role::getProfileId).anyMatch(id -> id.equals("INVESTIGATOR")))
		);
	}

	@Test
	@DisplayName("Find investigators on study and descendants")
	public void testFindInvestigatorsOnStudyAndDescendants() {
		final var predicate = new UserSearch();
		predicate.enforceScopePks(Collections.singleton(scopeService.getRootScope().getPk()));
		predicate.enforceExtension(ScopeExtension.DESCENDANTS);
		predicate.enforceProfileIds(Collections.singleton("INVESTIGATOR"));
		predicate.enforceEnabled(true);

		final var users = userDAOService.search(predicate).getObjects();

		assertAll(
			"Find the investigators on study and descendants",
			() -> assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("test+test-investigator@rodano.ch"))),
			() -> assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("test+iinves@rodano.ch")))
		);
	}

	@Test
	@DisplayName("Find all investigators on study and descendants")
	public void testFindAllInvestigatorsOnStudyAndDescendants() {
		final var predicate = new UserSearch();
		predicate.enforceScopePks(Collections.singleton(scopeService.getRootScope().getPk()));
		predicate.enforceExtension(ScopeExtension.DESCENDANTS);
		final Set<String> profileIds = new HashSet<>();
		profileIds.add("PRINCIPAL_INVESTIGATOR");
		profileIds.add("INVESTIGATOR");
		predicate.enforceProfileIds(profileIds);
		predicate.enforceEnabled(true);

		final var userEmails = userDAOService.search(predicate).getObjects().stream().map(User::getEmail).collect(Collectors.toList());

		assertAll(
			"Find all the investigators on study and descendants",
			() -> assertTrue(userEmails.contains("test+iinves@rodano.ch")),
			() -> assertTrue(userEmails.contains("test+pinves@rodano.ch")),
			() -> assertTrue(userEmails.contains("test+test-investigator@rodano.ch")),
			() -> assertTrue(userEmails.contains("test+test-principal_investigator@rodano.ch"))
		);
	}

	@Test
	@DisplayName("Find admins on study")
	public void testFindAdminsOnStudy() {
		final var predicate = new UserSearch();
		predicate.enforceScopePks(Collections.singleton(scopeService.getRootScope().getPk()));
		predicate.enforceExtension(ScopeExtension.DESCENDANTS);
		predicate.enforceProfileIds(Collections.singleton("ADMIN"));
		predicate.enforceEnabled(true);

		final var userEmails = userDAOService.search(predicate).getObjects().stream().map(User::getEmail).collect(Collectors.toList());

		assertAll(
			"Find all the administrators on study and descendants",
			() -> assertTrue(userEmails.contains(DatabaseInitializer.TEST_USER_EMAIL)),
			() -> assertFalse(userEmails.contains("test+iinves@rodano.ch"))
		);
	}
}
