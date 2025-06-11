package ch.rodano.core.services.bll.scope;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.database.initializer.DatabaseInitializer;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
@Transactional
public class ScopeTimeframeTest extends DatabaseTest {

	@Autowired
	private ScopeService scopeService;

	@Autowired
	private ScopeDAOService scopeDAOService;

	@Autowired
	private ScopeRelationService scopeRelationService;

	@Autowired
	private ActorService actorService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private UserDAOService userDAOService;

	@Test
	@DisplayName("Timeframe start and end date are calculated correctly")
	public void testDate() {
		final var user = userDAOService.getUserByEmail(DatabaseInitializer.TEST_USER_EMAIL);
		final var scope = scopeDAOService.getScopeByCode("FR-01");

		//if user has had right on the scope once, he always has the right since the beginning of time on this scope --weird but wished
		final var activeRoles = roleService.getActiveRoles(user);
		final var timeframe = actorService.getTimeframeForScope(user, scope, activeRoles).get();
		assertTrue(timeframe.startDate().isEmpty());
		assertTrue(timeframe.isInfiniteStartDate());
		assertTrue(timeframe.isInfiniteStopDate());
	}

	@Test
	@DisplayName("Timeframe is calculated correctly by a scope on itself")
	public void scopeTimeframeOnSelf() {
		final var patientScope = getPatient(0);

		final var timeframe = scopeService.getRelationTimeframe(patientScope, patientScope);

		assertTrue(timeframe.get().startDate().isPresent());
		assertEquals(patientScope.getStartDate(), timeframe.get().startDate().get());
		assertEquals(Optional.empty(), timeframe.get().stopDate());
	}

	@Test
	@DisplayName("Scope timeframe start date is calculated correctly")
	public void timeframeStartDateCalculated() {
		final var patientScope = getPatient(0);

		final var parentRelation = scopeRelationService.getDefaultParentRelation(patientScope);
		final var parentScope = scopeDAOService.getScopeByPk(parentRelation.getParentFk());

		final var timeframe = scopeService.getRelationTimeframe(parentScope, patientScope);

		assertEquals(parentRelation.getStartDate(), timeframe.get().startDate().get());
		assertNull(parentRelation.getEndDate());
		assertEquals(Optional.empty(), timeframe.get().stopDate());

		final var newStartDate = ZonedDateTime.now().minusYears(5);
		parentRelation.setStartDate(newStartDate);
		scopeRelationService.saveScopeRelation(parentRelation, context, "Testing");

		final var newTimeframe = scopeService.getRelationTimeframe(parentScope, patientScope);

		assertEquals(newStartDate, newTimeframe.get().startDate().get());
		assertEquals(Optional.empty(), newTimeframe.get().stopDate());
	}

	// TODO rewrite this
	/*
	@Test
	@DisplayName("Actor timeframe is calculated correctly")
	public void calculateActorTimeframe() {
		// TODO create new scopes for this test when scopes for tests become easily creatable
		final var patient = getPatient(1);
		final var parent = getCenter(0);

		// create a relation between a patient and a center
		final var startDate = ZonedDateTime.now().minusYears(10);
		final var endDate = ZonedDateTime.now().plusYears(1);
		final var parentRelation = scopeRelationService.createRelation(
			patient,
			parent,
			startDate,
			Optional.of(endDate),
			context,
			"Create a test relation"
		);

		// create new robot to test out the timeframes
		final var profile = studyService.getStudy().getProfile("ADMIN");
		final var robot = createRobot(profile, parent);

		final var timeframe = actorService.getTimeframeForScope(
			robot,
			patient,
			roleService.getRoles(robot)
		);

		// check that the timeframe corresponds to the relation dates
		assertEquals(Optional.empty(), timeframe.get().startDate());
		assertEquals(endDate, timeframe.get().stopDate().get());

		// create another relation that overlaps with the current relation
		final var newParent = getCenter(1);
		final var newStartDate = ZonedDateTime.now().minusYears(1);
		final var newEndDate = ZonedDateTime.now().plusYears(10);
		final var newParentRelation = scopeRelationService.createRelation(
			patient,
			newParent,
			newStartDate,
			Optional.of(newEndDate),
			context,
			"Create a test relation"
		);

		final var newRole = new Role(profile);
		newRole.setRobotFk(robot.getPk());
		newRole.setScopeFk(newParent.getPk());
		roleService.createRole(
			,
			newRole, ,
			context,
			"Create another test role"
		);

		final var newTimeframe = actorService.getTimeframeForScope(
			robot,
			patient,
			roleService.getRoles(robot)
		);

		// check that the timeframe dates have been adjusted according to the new relation
		assertEquals(Optional.empty(), newTimeframe.get().startDate());
		assertEquals(newEndDate, newTimeframe.get().stopDate().get());

		// end all relations
		final var allRelationsEndDate = ZonedDateTime.now();
		scopeRelationService.endRelation(
			parentRelation,
			allRelationsEndDate,
			context,
			"End first relation"
		);
		scopeRelationService.endRelation(
			newParentRelation,
			allRelationsEndDate,
			context,
			"End second relation"
		);

		final var noTimeframe = actorService.getTimeframeForScope(
			robot,
			patient,
			roleService.getRoles(robot)
		);

		// check that the timeframe is ended correctly
		assertEquals(Optional.empty(), noTimeframe.get().startDate());
		assertEquals(allRelationsEndDate, noTimeframe.get().stopDate().get());
	}

	 */

	private Scope getPatient(final int index) {
		final var patientScopeModel = studyService.getStudy().getScopeModel("PATIENT");
		return scopeService.getAll(patientScopeModel).get(index);
	}

	private Scope getCenter(final int index) {
		final var centerScopeModel = studyService.getStudy().getScopeModel("CENTER");
		return scopeService.getAll(centerScopeModel).get(index);
	}

	/*
	private Robot createRobot(final Profile profile, final Scope scope) {
		final var robot = new Robot();
		robot.setName(RandomStringUtils.randomAlphanumeric(32));
		robot.setKey("secret-key");
		final var role = new Role(profile);
		role.setScopeFk(scope.getPk());
		return robotService.createRobot(
			robot, ,
			role, ,
			context,
			"Create test robot"
		);
	}

	 */
}
