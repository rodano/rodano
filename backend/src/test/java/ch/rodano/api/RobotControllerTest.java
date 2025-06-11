package ch.rodano.api;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
		final var response = restTemplate.exchange(
			"/robots?pageSize={pageSize}&pageIndex={pageIndex}",
			HttpMethod.GET,
			null,
			PagedResult.class,
			Map.of(
				"pageSize", MAX_PAGE_SIZE,
				"pageIndex", 0
			)
		);

		// get some results
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody().getObjects().size() > 0);
	}

	@Test
	@DisplayName("Only an admin can create a robot")
	public void checkRobotCreationRights() {
		// login as a data manager
		authenticate(dataManagerOnStudyEmail);

		// try to create a robot
		final var robotCreationDTO = createRobotDTO(Optional.empty(), Optional.empty());
		final var robotCreationEntity = new HttpEntity<>(robotCreationDTO);
		final var response = executePost("/robots", robotCreationEntity, RobotDTO.class);

		// must fail
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}

	@Test
	@DisplayName("Robot creation works")
	public void testRobotCreation() {
		// login as an admin
		authenticate(adminOnStudyEmail);

		// try to create a robot
		final var responseBody = postNewRobotAndReturnBody(Optional.of("TestBot"), Optional.empty());

		// should succeed
		assertEquals(1, responseBody.getRoles().size());
		assertEquals(studyService.getStudy().getEproProfileId(), responseBody.getRoles().get(0).getProfileId());
		assertEquals("TestBot", responseBody.getName());
		assertNotNull(responseBody.getKey());

		// we should be able to find the created robot using GET
		final var robotPk = responseBody.getPk();
		final var getRobotResponse = restTemplate.exchange(
			"/robots/{robotPk}",
			HttpMethod.GET,
			null,
			RobotDTO.class,
			Map.of(
				"robotPk", robotPk
			)
		);

		assertEquals(HttpStatus.OK, getRobotResponse.getStatusCode());
		assertEquals(robotPk, getRobotResponse.getBody().getPk());
	}

	@Test
	@DisplayName("Creating two robots with the same name returns an error")
	public void creatingTwoRobotsWithSameNameThrowsError() {
		// login as an admin
		authenticate(adminOnStudyEmail);

		final var robotName = "Terminattore";
		// create a robot
		postNewRobotAndReturnBody(Optional.of(robotName), Optional.empty());

		// create a second robot with the same name
		final var duplicateRobotResponse = postNewRobot(Optional.of(robotName), Optional.empty());

		assertEquals(HttpStatus.BAD_REQUEST, duplicateRobotResponse.getStatusCode());
	}

	@Test
	@DisplayName("Robot update works")
	public void testRobotUpdate() {
		// login as an admin
		authenticate(adminOnStudyEmail);

		// create a robot
		final var robotDTO = postNewRobotAndReturnBody(Optional.empty(), Optional.empty());

		// modify the created robot
		final var newName = "UpdatedBot";
		final var newKey = "SupaSekretKey";
		final var robotUpdateDTO = new RobotUpdateDTO(newName, newKey);

		// this change should not be taken into account
		robotDTO.setRoles(Collections.emptyList());

		// update the robot
		final var updatedRobotResponse = testRobotUpdate(robotDTO.getPk(), robotUpdateDTO);
		final var updatedRobot = updatedRobotResponse.getBody();

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
		final var robotResponse = postNewRobot(Optional.of("NewBot"), Optional.empty());

		assertEquals(HttpStatus.CREATED, robotResponse.getStatusCode());

		final var key = robotResponse.getBody().getKey();

		// create another robot
		final var secondRobotDTO = postNewRobotAndReturnBody(Optional.of("NewerBot"), Optional.empty());

		// try to update the second robot with an already used key
		final var secondRobotUpdateDTO = new RobotUpdateDTO(secondRobotDTO.getName(), key);
		final var updatedRobotResponse = testRobotUpdate(secondRobotDTO.getPk(), secondRobotUpdateDTO);

		assertEquals(HttpStatus.BAD_REQUEST, updatedRobotResponse.getStatusCode());
	}

	private RobotDTO postNewRobotAndReturnBody(
		final Optional<String> name,
		final Optional<String> key
	) {
		return postNewRobot(name, key).getBody();
	}

	private ResponseEntity<RobotDTO> postNewRobot(
		final Optional<String> name,
		final Optional<String> key
	) {
		final var robotCreationDTO = createRobotDTO(name, key);
		final var robotCreationEntity = new HttpEntity<>(robotCreationDTO);
		return executePost("/robots", robotCreationEntity, RobotDTO.class);
	}

	private RobotCreationDTO createRobotDTO(
		final Optional<String> name,
		final Optional<String> key
	) {
		final var profile = studyService.getStudy().getEproProfile();
		final var roleCreationDTO = new RoleCreationDTO();
		roleCreationDTO.setScopePk(1L);
		roleCreationDTO.setProfileId(profile.getId());

		final var robotName = name.orElseGet(() -> RandomStringUtils.randomAlphanumeric(10));

		return new RobotCreationDTO(
			robotName,
			roleCreationDTO,
			key.orElse(null)
		);
	}

	private ResponseEntity<RobotDTO> testRobotUpdate(final Long robotPk, final RobotUpdateDTO robotDTO) {
		final var robotEntity = new HttpEntity<>(robotDTO);
		return restTemplate.exchange(
			"/robots/{robotPk}",
			HttpMethod.PUT,
			robotEntity,
			RobotDTO.class,
			Map.of(
				"robotPk", robotPk
			)
		);
	}
}
