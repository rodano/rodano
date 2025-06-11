package ch.rodano.api.audit;

import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.api.utils.URLConsistencyUtils;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.audit.models.DatasetAuditTrail;
import ch.rodano.core.model.audit.models.EventAuditTrail;
import ch.rodano.core.model.audit.models.FieldAuditTrail;
import ch.rodano.core.model.audit.models.FormAuditTrail;
import ch.rodano.core.model.audit.models.RobotAuditTrail;
import ch.rodano.core.model.audit.models.RoleAuditTrail;
import ch.rodano.core.model.audit.models.ScopeAuditTrail;
import ch.rodano.core.model.audit.models.WorkflowStatusAuditTrail;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.exception.UnauthorizedException;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.dataset.DatasetDAOService;
import ch.rodano.core.services.dao.event.EventDAOService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.dao.form.FormDAOService;
import ch.rodano.core.services.dao.robot.RobotDAOService;
import ch.rodano.core.services.dao.role.RoleDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.core.services.dao.workflow.WorkflowStatusDAOService;
import ch.rodano.core.utils.RightsService;
import ch.rodano.core.utils.UtilsService;

@Tag(name = "Versions")
@Profile("!migration")
@RestController
@Transactional(readOnly = true)
public class VersionsController extends AbstractSecuredController {
	private final ScopeService scopeService;
	private final ScopeDAOService scopeDAOService;
	private final EventDAOService eventDAOService;
	private final DatasetDAOService datasetDAOService;
	private final FormDAOService formDAOService;
	private final FieldDAOService fieldDAOService;
	private final WorkflowStatusDAOService workflowStatusDAOService;
	private final UserDAOService userDAOService;
	private final RobotDAOService robotDAOService;
	private final RoleDAOService roleDAOService;
	private final UtilsService utilsService;

	public VersionsController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final ScopeService scopeService,
		final ScopeDAOService scopeDAOService,
		final EventDAOService eventDAOService,
		final DatasetDAOService datasetDAOService,
		final FormDAOService formDAOService,
		final FieldDAOService fieldDAOService,
		final WorkflowStatusDAOService workflowStatusDAOService,
		final UserDAOService userDAOService,
		final RobotDAOService robotDAOService,
		final RoleDAOService roleDAOService,
		final UtilsService utilsService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.scopeService = scopeService;
		this.scopeDAOService = scopeDAOService;
		this.eventDAOService = eventDAOService;
		this.datasetDAOService = datasetDAOService;
		this.formDAOService = formDAOService;
		this.fieldDAOService = fieldDAOService;
		this.workflowStatusDAOService = workflowStatusDAOService;
		this.userDAOService = userDAOService;
		this.robotDAOService = robotDAOService;
		this.roleDAOService = roleDAOService;
		this.utilsService = utilsService;
	}

	@GetMapping("scopes/{scopePk}/versions")
	@ResponseStatus(HttpStatus.OK)
	public NavigableSet<ScopeAuditTrail> getForScope(
		@PathVariable final Long scopePk,
		@RequestParam final Optional<Long> auditActorPk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scope);

		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.VIEW_AUDIT_TRAIL);
		rightsService.checkRight(currentActor, currentRoles, scope.getScopeModel(), Rights.READ);

		final var timeframe = actorService.getTimeframeForScope(currentActor, scope, currentRoles);
		return scopeDAOService.getAuditTrails(scope, timeframe, auditActorPk);
	}

	@GetMapping("workflows/{workflowPk}/versions")
	@ResponseStatus(HttpStatus.OK)
	public NavigableSet<WorkflowStatusAuditTrail> getForWorkflowStatus(
		@PathVariable final Long workflowPk,
		@RequestParam final Optional<Long> auditActorPk
	) {
		final var workflowStatus = workflowStatusDAOService.getWorkflowStatusByPk(workflowPk);
		utilsService.checkNotNull(WorkflowStatus.class, workflowStatus, workflowPk);

		final var scope = scopeService.get(workflowStatus);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scope);

		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.VIEW_AUDIT_TRAIL);
		rightsService.checkRight(currentActor, currentRoles, workflowStatus.getWorkflow());

		final var timeframe = actorService.getTimeframeForScope(currentActor, scope, currentRoles);
		return workflowStatusDAOService.getAuditTrails(workflowStatus, timeframe, auditActorPk);
	}

	@GetMapping("scopes/{scopePk}/events/{eventPk}/versions")
	@ResponseStatus(HttpStatus.OK)
	public NavigableSet<EventAuditTrail> getForEvent(
		@PathVariable final Long scopePk,
		@PathVariable final Long eventPk,
		@RequestParam final Optional<Long> auditActorPk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventDAOService.getEventByPk(eventPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);

		URLConsistencyUtils.checkConsistency(scope, event);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scope);

		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.VIEW_AUDIT_TRAIL);
		rightsService.checkRight(currentActor, currentRoles, scope.getScopeModel(), Rights.READ);
		rightsService.checkRight(currentActor, currentRoles, event.getEventModel(), Rights.READ);

		final var timeframe = actorService.getTimeframeForScope(currentActor, scope, currentRoles);
		return eventDAOService.getAuditTrails(event, timeframe, auditActorPk);
	}

	@GetMapping({ "scopes/{scopePk}/datasets/{datasetPk}/versions", "scopes/{scopePk}/events/{eventPk}/datasets/{datasetPk}/versions" })
	@ResponseStatus(HttpStatus.OK)
	public NavigableSet<DatasetAuditTrail> getForDataset(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@PathVariable final Long datasetPk,
		@RequestParam final Optional<Long> auditActorPk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var dataset = datasetDAOService.getDatasetByPk(datasetPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		utilsService.checkNotNull(Dataset.class, dataset, datasetPk);

		URLConsistencyUtils.checkConsistency(scope, event, dataset);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scope);

		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.VIEW_AUDIT_TRAIL);
		rightsService.checkRight(currentActor, currentRoles, scope.getScopeModel(), Rights.READ);
		event.ifPresent(e -> rightsService.checkRight(currentActor, currentRoles, e.getEventModel(), Rights.READ));
		rightsService.checkRight(currentActor, currentRoles, dataset.getDatasetModel(), Rights.READ);

		final var timeframe = actorService.getTimeframeForScope(currentActor, scope, currentRoles);
		return datasetDAOService.getAuditTrails(dataset, timeframe, auditActorPk);
	}

	@GetMapping({ "scopes/{scopePk}/datasets/{datasetPk}/fields/{fieldPk}/versions", "scopes/{scopePk}/events/{eventPk}/datasets/{datasetPk}/fields/{fieldPk}/versions" })
	@ResponseStatus(HttpStatus.OK)
	public NavigableSet<FieldAuditTrail> getForField(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@PathVariable final Long datasetPk,
		@PathVariable final Long fieldPk,
		@RequestParam final Optional<Long> auditActorPk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var dataset = datasetDAOService.getDatasetByPk(datasetPk);
		final var field = fieldDAOService.getFieldByPk(fieldPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		utilsService.checkNotNull(Dataset.class, dataset, datasetPk);
		utilsService.checkNotNull(Field.class, field, fieldPk);

		URLConsistencyUtils.checkConsistency(scope, event, dataset, field);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scope);

		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.VIEW_AUDIT_TRAIL);
		rightsService.checkRight(currentActor, currentRoles, scope.getScopeModel(), Rights.READ);
		event.ifPresent(e -> rightsService.checkRight(currentActor, currentRoles, e.getEventModel(), Rights.READ));
		rightsService.checkRight(currentActor, currentRoles, dataset.getDatasetModel(), Rights.READ);

		final var timeframe = actorService.getTimeframeForScope(currentActor, scope, currentRoles);
		return fieldDAOService.getAuditTrails(field, timeframe, auditActorPk);
	}

	@GetMapping({ "scopes/{scopePk}/forms/{formPk}/versions", "scopes/{scopePk}/events/{eventPk}/forms/{formPk}/versions" })
	@ResponseStatus(HttpStatus.OK)
	public NavigableSet<FormAuditTrail> getForForm(
		@PathVariable final Long scopePk,
		@PathVariable final Optional<Long> eventPk,
		@PathVariable final Long formPk,
		@RequestParam final Optional<Long> auditActorPk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var event = eventPk.map(eventDAOService::getEventByPk);
		final var form = formDAOService.getFormByPk(formPk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Event.class, event, eventPk);
		utilsService.checkNotNull(Form.class, form, formPk);

		URLConsistencyUtils.checkConsistency(scope, event, form);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scope);

		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.VIEW_AUDIT_TRAIL);
		rightsService.checkRight(currentActor, currentRoles, scope.getScopeModel(), Rights.READ);
		event.ifPresent(e -> rightsService.checkRight(currentActor, currentRoles, e.getEventModel(), Rights.READ));
		rightsService.checkRight(currentActor, currentRoles, form.getFormModel(), Rights.READ);

		final var timeframe = actorService.getTimeframeForScope(currentActor, scope, currentRoles);
		return formDAOService.getAuditTrails(form, timeframe, auditActorPk);
	}

	@GetMapping("users/{userPk}/versions")
	@ResponseStatus(HttpStatus.OK)
	public NavigableSet<UserAuditTrailDTO> getForUser(
		@PathVariable final Long userPk,
		@RequestParam final Optional<Long> auditActorPk
	) {
		final var user = userDAOService.getUserByPk(userPk);

		utilsService.checkNotNull(User.class, user, userPk);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();

		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.VIEW_AUDIT_TRAIL);
		rightsService.checkRight(currentActor, currentRoles, user, Rights.READ);

		final var userAuditTrails = userDAOService.getAuditTrails(user, Optional.empty(), auditActorPk);

		return userAuditTrails.stream()
			.map(UserAuditTrailDTO::new)
			.collect(Collectors.toCollection(TreeSet::new));
	}

	@GetMapping("robots/{robotPk}/versions")
	@ResponseStatus(HttpStatus.OK)
	public NavigableSet<RobotAuditTrail> getForRobot(
		@PathVariable final Long robotPk,
		@RequestParam final Optional<Long> auditActorPk
	) {
		final var robot = robotDAOService.getRobotByPk(robotPk);

		utilsService.checkNotNull(Robot.class, robot, robotPk);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.VIEW_AUDIT_TRAIL);
		rightsService.checkRightAdmin(currentActor, currentRoles);

		return robotDAOService.getAuditTrails(robot, Optional.empty(), auditActorPk);
	}

	@GetMapping("users/{userPk}/roles/{rolePk}/versions")
	@ResponseStatus(HttpStatus.OK)
	public NavigableSet<RoleAuditTrail> getForUserRole(
		@PathVariable final Long userPk,
		@PathVariable final Long rolePk,
		@RequestParam final Optional<Long> auditActorPk
	) {

		final var user = userDAOService.getUserByPk(userPk);
		final var role = roleDAOService.getRoleByPk(rolePk);

		utilsService.checkNotNull(User.class, user, userPk);
		utilsService.checkNotNull(Role.class, role, rolePk);

		URLConsistencyUtils.checkConsistency(user, role);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();

		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.VIEW_AUDIT_TRAIL);
		if(!rightsService.hasRightOnActorRole(currentActor, currentRoles, user, role, Rights.READ)) {
			throw new UnauthorizedException("You don't have sufficient rights to access this role's history");
		}

		return roleDAOService.getAuditTrails(role, Optional.empty(), auditActorPk);
	}

	@GetMapping("robots/{robotPk}/roles/{rolePk}/versions")
	@ResponseStatus(HttpStatus.OK)
	public NavigableSet<RoleAuditTrail> getForRobotRole(
		@PathVariable final Long robotPk,
		@PathVariable final Long rolePk,
		@RequestParam final Optional<Long> auditActorPk
	) {

		final var robot = robotDAOService.getRobotByPk(robotPk);
		final var role = roleDAOService.getRoleByPk(rolePk);

		utilsService.checkNotNull(User.class, robot, robotPk);
		utilsService.checkNotNull(Role.class, role, rolePk);

		URLConsistencyUtils.checkConsistency(robot, role);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();

		rightsService.checkRight(currentActor, currentRoles, FeatureStatic.VIEW_AUDIT_TRAIL);
		rightsService.checkRightAdmin(currentActor, currentRoles);

		return roleDAOService.getAuditTrails(role, Optional.empty(), auditActorPk);
	}
}
