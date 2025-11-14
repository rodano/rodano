package ch.rodano.api;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.rodano.api.actor.RobotCreationDTO;
import ch.rodano.api.actor.RobotDTO;
import ch.rodano.api.actor.RobotUpdateDTO;
import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.role.RoleCreationDTO;
import ch.rodano.test.ControllerTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
public class RobotControllerTest extends ControllerTest {

	@Test
	@DisplayName("Robot search endpoint works")
	public void robotsAreSearchable() {
		// login as an admin
		authenticate(adminOnStudyEmail);

		// look for robots
		final var uri = "/robots?pageSize={pageSize}&pageIndex={pageIndex}";
		final var robots = client.get().uri(uri, MAX_PAGE_SIZE, 0).exchange().expectStatus().isOk().expectBody(PagedResult.class).returnResult().getResponseBody();
		assertTrue(robots.getObjects().size() > 0);
	}

	@Test
	@DisplayName("Only an admin can create a robot")
	public void checkRobotCreationRights() {
		// login as a data manager
		authenticate(dataManagerOnStudyEmail);

		// try to create a robot
		final var robotCreationDTO = createRobotDTO(Optional.empty(), Optional.empty());
		client.post().uri("/robots").body(robotCreationDTO).exchange().expectStatus().isUnauthorized();
	}

	@Test
	@DisplayName("Robot creation works")
	public void testRobotCreation() {
		// login as an admin
		authenticate(adminOnStudyEmail);

		// create a robot
		final var robotDTO = createRobotDTO(Optional.of("TestBot"), Optional.empty());
		final var createdRobot = post("/robots", robotDTO, RobotDTO.class);

		// should succeed
		assertEquals(1, createdRobot.getRoles().size());
		assertEquals(studyService.getStudy().getEproProfileId(), createdRobot.getRoles().get(0).getProfileId());
		assertEquals("TestBot", createdRobot.getName());
		assertNotNull(createdRobot.getKey());

		// we should be able to find the created robot using GET
		final var robotPk = createdRobot.getPk();
		final var robot = client.get().uri("/robots/{robotPk}", Map.of("robotPk", robotPk)).exchange().expectStatus().isOk().expectBody(RobotDTO.class).returnResult().getResponseBody();

		assertEquals(robotPk, robot.getPk());
		assertEquals("TestBot", robot.getName());
	}

	@Test
	@DisplayName("Creating two robots with the same name returns an error")
	public void creatingTwoRobotsWithSameNameThrowsError() {
		// login as an admin
		authenticate(adminOnStudyEmail);

		final var robotName = Optional.of("Terminattore");

		final var robotDTO = createRobotDTO(robotName, Optional.empty());
		post("/robots", robotDTO, RobotDTO.class);

		client.post().uri("/robots").body(robotDTO).exchange().expectStatus().isBadRequest();
	}

	@Test
	@DisplayName("Robot update works")
	public void testRobotUpdate() {
		// login as an admin
		authenticate(adminOnStudyEmail);

		// create a robot
		final var robotDTO = createRobotDTO(Optional.empty(), Optional.empty());
		final var createdRobot = post("/robots", robotDTO, RobotDTO.class);

		// modify the created robot
		final var newName = "UpdatedBot";
		final var newKey = "SupaSekretKey";
		final var robotUpdateDTO = new RobotUpdateDTO(newName, newKey);

		// this change should not be taken into account
		createdRobot.setRoles(Collections.emptyList());

		// update the robot
		final var updatedRobot = client.put().uri("/robots/{robotPk}", Map.of("robotPk", createdRobot.getPk())).body(robotUpdateDTO).exchange().expectBody(RobotDTO.class).returnResult()
			.getResponseBody();

		// check that the robot has been modified correctly
		assertEquals(newName, updatedRobot.getName());
		assertEquals(newKey, updatedRobot.getKey());

		// check that the robot role has remained the same
		assertEquals(studyService.getStudy().getEproProfileId(), updatedRobot.getRoles().get(0).getProfileId());
	}

	@Test
	@DisplayName("Trying to update a robot with an already used key returns an error")
	public void updateRobotWithAnAlreadyExistingKeyThrowsError() {
		// login an admin
		authenticate(adminOnStudyEmail);

		// create a robot and get its key
		final var robotDTO1 = createRobotDTO(Optional.of("NewBot"), Optional.empty());
		final var createdRobot1 = post("/robots", robotDTO1, RobotDTO.class);
		final var key = createdRobot1.getKey();

		// create another robot
		final var robotDTO2 = createRobotDTO(Optional.of("NewerBot"), Optional.empty());
		final var createdRobot2 = post("/robots", robotDTO2, RobotDTO.class);

		// try to update the second robot with an already used key
		final var robotDTO3 = new RobotUpdateDTO(createdRobot2.getName(), key);
		client.put().uri("/robots/{robotPk}", Map.of("robotPk", createdRobot2.getPk())).body(robotDTO3).exchange().expectStatus().isBadRequest();
	}

	private RobotCreationDTO createRobotDTO(
		final Optional<String> name,
		final Optional<String> key
	) {
		final var profile = studyService.getStudy().getEproProfile();
		final var roleCreationDTO = new RoleCreationDTO();
		roleCreationDTO.setScopePk(1L);
		roleCreationDTO.setProfileId(profile.getId());

		final var robotName = name.orElseGet(() -> RandomStringUtils.secure().nextAlphanumeric(10));

		return new RobotCreationDTO(
			robotName,
			roleCreationDTO,
			key.orElse(null)
		);
	}
}
