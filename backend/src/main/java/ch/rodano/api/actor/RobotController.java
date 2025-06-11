package ch.rodano.api.actor;

import java.util.Optional;

import jakarta.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.model.robot.RobotSearch;
import ch.rodano.core.model.robot.RobotSortBy;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.robot.RobotService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.robot.RobotDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;
import ch.rodano.core.utils.UtilsService;

@Tag(name = "Robot")
@RestController
@RequestMapping("/robots")
@Validated
@Transactional(readOnly = true)
public class RobotController extends AbstractSecuredController {
	private final RobotService robotService;
	private final RobotDAOService robotDAOService;
	private final ActorDTOService actorDTOService;
	private final UtilsService utilsService;
	private final ScopeDAOService scopeDAOService;

	private final Integer defaultPageSize;

	public RobotController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final RobotService robotService,
		final RobotDAOService robotDAOService,
		final ActorDTOService actorDTOService,
		final UtilsService utilsService,
		final ScopeDAOService scopeDAOService,
		@Value("${rodano.pagination.maximum-page-size}") final Integer defaultPageSize
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.robotService = robotService;
		this.robotDAOService = robotDAOService;
		this.actorDTOService = actorDTOService;
		this.utilsService = utilsService;
		this.scopeDAOService = scopeDAOService;
		this.defaultPageSize = defaultPageSize;
	}

	@Operation(summary = "Search robots")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public PagedResult<RobotDTO> search(
		@Parameter(description = "Robot name") @RequestParam final Optional<String> name,
		@Parameter(description = "Profile ID") @RequestParam final Optional<String> profileId,
		@Parameter(description = "Sort the results by which property?") @RequestParam final Optional<RobotSortBy> sortBy,
		@Parameter(description = "Use the ascending order?") @RequestParam final Optional<Boolean> orderAscending,
		@Parameter(description = "Page size") @RequestParam final Optional<Integer> pageSize,
		@Parameter(description = "Page index") @RequestParam final Optional<Integer> pageIndex
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRightAdmin(currentActor, currentRoles);

		final var search = new RobotSearch()
			.setName(name.filter(StringUtils::isNotBlank))
			.setProfileId(profileId.filter(StringUtils::isNotBlank))
			.setIncludeDeleted(rightsService.hasRight(currentRoles, FeatureStatic.MANAGE_DELETED_DATA))
			.setPageSize(pageSize.isEmpty() ? Optional.of(defaultPageSize) : pageSize)
			.setPageIndex(pageIndex.isEmpty() ? Optional.of(0) : pageIndex);
		//set sort if provided
		sortBy.map(search::setSortBy);
		orderAscending.map(search::setSortAscending);

		return robotService.search(search)
			.withObjectsTransformation(u -> actorDTOService.createRobotDTOs(u, currentActor, currentRoles));
	}

	@Operation(summary = "Get a robot")
	@GetMapping("{robotPk}")
	@ResponseStatus(HttpStatus.OK)
	public RobotDTO getRobot(
		@PathVariable final Long robotPk
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRightAdmin(currentActor, currentRoles);

		// Retrieve user and check right
		final var robot = robotDAOService.getRobotByPk(robotPk);
		utilsService.checkNotNull(Robot.class, robot, robotPk);

		return actorDTOService.createRobotDTO(robot, currentActor, currentRoles);
	}

	@Operation(summary = "Create a robot")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public RobotDTO createRobot(
		@Valid @RequestBody final RobotCreationDTO robotCreationDTO
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();

		// Check if the user is an admin
		rightsService.checkRightAdmin(currentActor, currentRoles);

		// Check if the provided role scope is not null and that the user has rights to it
		final var scope = scopeDAOService.getScopeByPk(robotCreationDTO.role().getScopePk());
		utilsService.checkNotNull(Scope.class, scope, robotCreationDTO.role().getScopePk());
		rightsService.checkRight(currentActor, currentRoles, scope.getScopeModel(), Rights.WRITE);

		// Prepare the needed objects
		final var updatedRobot = actorDTOService.generateRobot(robotCreationDTO);
		final var profile = studyService.getStudy().getProfile(robotCreationDTO.role().getProfileId());
		final var roleScope = scopeDAOService.getScopeByPk(robotCreationDTO.role().getScopePk());

		// create the robot
		final var newRobot = robotService.createRobot(
			updatedRobot,
			profile,
			roleScope,
			currentContext(),
			"Create robot"
		);

		return actorDTOService.createRobotDTO(newRobot, currentActor, currentRoles);
	}

	@Operation(summary = "Update robot")
	@PutMapping("/{robotPk}")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public RobotDTO updateRobot(
		@PathVariable final Long robotPk,
		@Valid @RequestBody final RobotUpdateDTO robotDTO
	) {
		//retrieve robot
		final var robot = robotDAOService.getRobotByPk(robotPk);
		utilsService.checkNotNull(Robot.class, robot, robotPk);

		//check rights
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRightAdmin(currentActor, currentRoles);

		//update the existing user with the data from the DTO
		actorDTOService.updateRobot(robot, robotDTO);
		robotService.saveRobot(robot, currentContext(), "Update robot");

		return actorDTOService.createRobotDTO(robot, currentActor, currentRoles);
	}

	@Operation(summary = "Remove robot")
	@PutMapping("/{robotPk}/remove")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public RobotDTO removeRobot(
		@PathVariable final Long robotPk
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRightAdmin(currentActor, currentRoles);

		// Retrieve user and check right
		final var robot = robotDAOService.getRobotByPk(robotPk);
		utilsService.checkNotNull(Robot.class, robot, robotPk);

		robotService.deleteRobot(robot, currentContext(), "Remove robot");

		return actorDTOService.createRobotDTO(robot, currentActor, currentRoles);
	}

	@Operation(summary = "Restore robot")
	@PutMapping("/{robotPk}/restore")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public RobotDTO restoreRobot(
		@PathVariable final Long robotPk
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRightAdmin(currentActor, currentRoles);

		// Retrieve user and check right
		final var robot = robotDAOService.getRobotByPk(robotPk);
		utilsService.checkNotNull(Robot.class, robot, robotPk);

		robotService.restoreRobot(robot, currentContext(), "Restore robot");

		return actorDTOService.createRobotDTO(robot, currentActor, currentRoles);
	}
}
