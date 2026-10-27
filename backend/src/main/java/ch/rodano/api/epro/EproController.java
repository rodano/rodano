package ch.rodano.api.epro;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.exception.http.ForbiddenOperationException;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.api.scope.ScopeDTOService;
import ch.rodano.api.scope.ScopeMiniDTO;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeSearch;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.robot.RobotService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.robot.RobotDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;
import ch.rodano.core.utils.UtilsService;

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
	private final ScopeService scopeService;
	private final UtilsService utilsService;
	private final Integer defaultPageSize;

	public EproController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final RobotService robotService,
		final RobotDAOService robotDAOService,
		final ScopeDAOService scopeDAOService,
		final ScopeService scopeService,
		final ScopeDTOService scopeDTOService,
		final UtilsService utilsService,
		@Value("${rodano.pagination.maximum-page-size}") final Integer defaultPageSize
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.robotService = robotService;
		this.robotDAOService = robotDAOService;
		this.scopeDAOService = scopeDAOService;
		this.scopeDTOService = scopeDTOService;
		this.scopeService = scopeService;
		this.utilsService = utilsService;
		this.defaultPageSize = defaultPageSize;
	}

	@Operation(summary = "Search for the leaf scopes that can be invited to ePRO", description = "The robot keys are never returned")
	@GetMapping("scopes")
	@ResponseStatus(HttpStatus.OK)
	public PagedResult<EproScopeDTO> searchScopes(
		@Parameter(description = "Full text search on code, shortname and longname") @RequestParam("fullText") final Optional<String> fullText,
		@Parameter(description = "Scope parent PKs") @RequestParam("parentPks") final Optional<List<Long>> parentPks,
		@Parameter(description = "Page size") @RequestParam("pageSize") final Optional<Integer> pageSize,
		@Parameter(description = "Page index") @RequestParam("pageIndex") final Optional<Integer> pageIndex
	) {
		checkEproEnabled();

		final var currentRoles = currentActiveRoles();
		final var leafScopeModel = studyService.getStudy().getLeafScopeModel();

		//only the leaf scopes located under the scopes on which the actor has a role granting the read right are returned
		final var search = new ScopeSearch()
			.setScopeModelId(Optional.of(leafScopeModel.getId()))
			.setScopeModelAncestorPks(scopeService.buildActorRightPredicate(currentRoles, Optional.of(leafScopeModel.getId())))
			.setLeaf(Optional.of(true))
			.setParentPks(parentPks)
			.setFullText(fullText.filter(StringUtils::isNotBlank))
			.setIncludeRemoved(false)
			.setPageSize(pageSize.isEmpty() ? Optional.of(defaultPageSize) : pageSize)
			.setPageIndex(pageIndex.isEmpty() ? Optional.of(0) : pageIndex);

		final var result = scopeService.search(search);
		final var invitedScopeIds = robotDAOService.getActiveRobotNames(result.getObjects().stream().map(Scope::getId).toList());

		return result.withObjectTransformation(scope -> new EproScopeDTO(new ScopeMiniDTO(scope), invitedScopeIds.contains(scope.getId())));
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
		@RequestParam("key") final String key
	) throws InvalidKeyException {
		checkEproEnabled();

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
		@PathVariable("scopePk") final Long scopePk
	) {
		checkEproEnabled();

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
		else if(robot.isRemoved()) {
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
		@PathVariable("scopePk") final Long scopePk
	) {
		checkEproEnabled();

		final var scope = scopeDAOService.getScopeByPk(scopePk);
		utilsService.checkNotNull(Scope.class, scope, scopePk);

		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.WRITE);

		final var robot = robotDAOService.getRobotByName(scope.getId());
		if(robot == null || robot.isRemoved()) {
			throw new NotFoundException(Robot.class, scope.getId());
		}
		robotService.deleteRobot(robot, currentContext(), "Revoke user access to ePro");
	}

	private String generateKey() {
		return UUID.randomUUID().toString().substring(0, 8);
	}

	private void checkEproEnabled() {
		if(!studyService.getStudy().isEproEnabled()) {
			throw new ForbiddenOperationException("ePRO is not enabled for this study");
		}
	}
}
