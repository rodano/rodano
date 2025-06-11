package ch.rodano.api.epro;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.api.scope.ScopeDTOService;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.robot.RobotService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.robot.RobotDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;

import static ch.rodano.core.model.jooq.tables.Robot.ROBOT;
import static ch.rodano.core.model.jooq.tables.Scope.SCOPE;

@Tag(name = "ePRO")
@RestController
@RequestMapping(value = "/epro")
@Validated
@Transactional(readOnly = true)
public class EproController extends AbstractSecuredController {
	private final RobotService robotService;
	private final RobotDAOService robotDAOService;
	private final ScopeDAOService scopeDAOService;
	private final ScopeDTOService scopeDTOService;
	private final DSLContext create;

	public EproController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final RobotService robotService,
		final RobotDAOService robotDAOService,
		final ScopeDAOService scopeDAOService,
		final DSLContext create,
		final ScopeDTOService scopeDTOService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.robotService = robotService;
		this.robotDAOService = robotDAOService;
		this.scopeDAOService = scopeDAOService;
		this.scopeDTOService = scopeDTOService;
		this.create = create;
	}

	@Operation(summary = "Get the invited robots")
	@GetMapping("robots")
	@ResponseStatus(HttpStatus.OK)
	public List<EproRobotDTO> invitedRobots() {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		final var leafScopeModel = studyService.getStudy().getLeafScopeModel();

		rightsService.checkRight(currentActor, currentRoles, leafScopeModel, Rights.READ);

		// Retrieve all roles that are linked to a leaf scope
		final var query = create.select(SCOPE.PK.as("scope_pk"), ROBOT.NAME.as("robot_name"), ROBOT.KEY.as("robot_key"))
			.from(ROBOT)
			.innerJoin(SCOPE).on(SCOPE.ID.eq(ROBOT.NAME))
			.where(SCOPE.SCOPE_MODEL_ID.eq(leafScopeModel.getId()).and(ROBOT.DELETED.isFalse()));

		return query.fetchInto(EproRobotDTO.class);
	}

	//allows to retrieve the robot name from the key
	// TODO Currently the ePRO root needs robot name and key to use the API
	// TODO The robot name should not be required to perform API requests
	// TODO Basic auth should not rely on robot name, just the robot key
	@Deprecated
	@SecurityRequirements
	@Operation(summary = "Get robot")
	// Warning : if you change this API endpoint, do not forget to change it in the security configuration !
	@PostMapping("/robot")
	public EproRobotDTO getRobot(
		@RequestParam final String key
	) throws InvalidKeyException {
		//retrieve robot
		final var robot = robotDAOService.getRobotByKey(key);
		if(robot == null) {
			throw new InvalidKeyException(String.format("No robot found for key %s", key));
		}
		final var scope = scopeDAOService.getScopeById(robot.getName());
		return new EproRobotDTO(scope.getPk(), robot.getName(), robot.getKey());
	}

	@Operation(summary = "Invite a user")
	@PutMapping("{scopePk}/invite")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public EPROInvitationDTO invite(
		@PathVariable final Long scopePk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.WRITE);

		//check of a robot does not already exists
		var robot = robotDAOService.getRobotByName(scope.getId());

		final var rationale = "Invite ePro user";
		// Create robot if it does not already exist
		if(robot == null) {
			// create the robot object
			robot = new Robot();
			robot.setName(scope.getId());
			robot.setKey(generateKey());

			// create the robot role
			final var eproProfile = studyService.getStudy().getEproProfile();

			robotService.createRobot(
				robot,
				eproProfile,
				scope,
				currentContext(),
				rationale
			);
		}
		// Restore robot if is has been deleted
		else if(robot.getDeleted()) {
			robotService.restoreRobot(robot, currentContext(), rationale);
			robot.setKey(generateKey());
			robotDAOService.saveRobot(robot, currentContext(), rationale);
		}

		final var scopeDTO = scopeDTOService.createDTO(scope, acl);
		return new EPROInvitationDTO(scopeDTO, studyService.getStudy().getUrl(), robot.getKey());
	}

	@Operation(summary = "Revoke user access")
	@PutMapping("{scopePk}/revoke")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void revoke(
		@PathVariable final Long scopePk
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();

		final var scope = scopeDAOService.getScopeByPk(scopePk);
		rightsService.checkRight(currentActor, currentRoles, scope.getScopeModel(), Rights.WRITE);

		final var robot = robotDAOService.getRobotByName(scope.getId());
		robotService.deleteRobot(robot, currentContext(), "Revoke user access to ePro");
	}

	private String generateKey() {
		return UUID.randomUUID().toString().substring(0, 8);
	}
}
