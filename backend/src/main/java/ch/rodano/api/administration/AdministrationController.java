package ch.rodano.api.administration;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.StringJoiner;

import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.boot.actuate.system.DiskSpaceHealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.exception.http.BadArgumentException;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.core.configuration.core.Configurator;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.utils.RightsService;

@Tag(name = "Administration")
@RestController
@RequestMapping("/administration")
@Validated
@Transactional(readOnly = true)
public class AdministrationController extends AbstractSecuredController {

	private final Configurator configurator;
	private final HealthEndpoint healthEndpoint;
	private final DiskSpaceHealthIndicator diskSpaceHealthIndicator;
	private final DataSourceHealthIndicator dataSourceHealthIndicator;
	private final ScheduledTaskHolder scheduledTaskHolder;

	public AdministrationController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final Configurator configurator,
		final HealthEndpoint healthEndpoint,
		final DiskSpaceHealthIndicator diskSpaceHealthIndicator,
		final DataSourceHealthIndicator dataSourceHealthIndicator,
		final ScheduledTaskHolder scheduledTaskHolder
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.configurator = configurator;
		this.healthEndpoint = healthEndpoint;
		this.diskSpaceHealthIndicator = diskSpaceHealthIndicator;
		this.dataSourceHealthIndicator = dataSourceHealthIndicator;
		this.scheduledTaskHolder = scheduledTaskHolder;
	}

	@Operation(summary = "Reload configuration", description = "Reload the study configuration (only available to administrators)")
	@PostMapping("reload")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Transactional
	public void reloadConfiguration() throws IOException {
		//TODO see if it is not possible to throw an exception (this will create a 403 instead of a 401)
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRightAdmin(currentActor, currentRoles);

		studyService.reload();
	}

	@SecurityRequirements
	@Operation(summary = "Check if the system is in maintenance mode")
	// Warning : if you change this API endpoint, do not forget to change it in the security configuration !
	@GetMapping("maintenance")
	@ResponseStatus(HttpStatus.OK)
	public Map<String, Boolean> inMaintenance() {
		return Collections.singletonMap("state", configurator.getMaintenanceMode());
	}

	// TODO convey in the API description that this is only for admins
	@Operation(summary = "Toggle the maintenance mode", description = "Toggle the maintenance mode (only available for the administrators)")
	@PostMapping("maintenance")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Transactional
	public void toggleMaintenance(
		@RequestBody final Map<String, Boolean> payload
	) {
		//TODO see if it is not possible to throw an exception (this will create a 403 instead of a 401)
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRightAdmin(currentActor, currentRoles);

		configurator.setMaintenanceMode(payload.getOrDefault("state", false));
	}

	@SecurityRequirements
	// Warning : if you change this API endpoint, do not forget to change it in the security configuration !
	@Operation(summary = "Check if the system is in debug mode")
	@GetMapping("debug")
	@ResponseStatus(HttpStatus.OK)
	public Map<String, Boolean> isInDebugMode() {
		final var loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		return Collections.singletonMap("state", Level.DEBUG.equals(loggerContext.getLogger("ch.rodano.core").getLevel()));
	}

	// TODO convey in the API description that this is only for admins
	@Operation(summary = "Toggle the debug mode")
	@PostMapping("debug")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Transactional
	public void toggleDebug(
		@RequestBody final Map<String, Boolean> payload
	) {
		//TODO see if it is not possible to throw an exception (this will create a 403 instead of a 401)
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		rightsService.checkRightAdmin(currentActor, currentRoles);

		final var loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.getLogger("ch.rodano.core").setLevel(payload.getOrDefault("state", false) ? Level.DEBUG : Level.INFO);
	}

	@SecurityRequirements
	@Operation(summary = "Check if the system functions correctly")
	// Warning : if you change this API endpoint, do not forget to change it in the security configuration !
	@GetMapping("is-online")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<Map<String, String>> isOnline() {
		final var study = studyService.getStudy();
		final var studyName = study.getDefaultLocalizedShortname();

		//system up is an aggregation of the status of all monitored components
		final var systemUp = healthEndpoint.health().getStatus().equals(Status.UP);

		//no error detected
		if(systemUp) {
			return ResponseEntity.ok(Collections.singletonMap("message", studyName + " study is up"));
		}

		//there is something wrong
		final var error = studyName + " study has the following problem(s): ";
		final var problems = new StringJoiner(", ");
		final var datasourceUp = dataSourceHealthIndicator.health().getStatus().equals(Status.UP);
		final var diskUp = diskSpaceHealthIndicator.health().getStatus().equals(Status.UP);
		if(!datasourceUp) {
			problems.add("connection with the database does not work");
		}
		if(!diskUp) {
			problems.add("there is not enough disk space left");
		}
		return ResponseEntity
			.status(HttpStatus.SERVICE_UNAVAILABLE)
			.body(Collections.singletonMap("message", error.concat(problems.toString())));
	}

	@Operation(summary = "Manually execute a scheduled task")
	@PutMapping("execute-scheduled-task")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public void executeScheduledTask(
		@RequestParam final String scheduledTaskName
	) {
		rightsService.checkRightAdmin(currentActor(), currentRoles());

		final var scheduledTask = scheduledTaskHolder.getScheduledTasks().stream()
			.filter(task -> task.getTask().toString().equals(scheduledTaskName))
			.findAny()
			.orElseThrow(() -> new BadArgumentException("Could not find a scheduled task named " + scheduledTaskName));

		scheduledTask.getTask().getRunnable().run();
		//taskScheduler.schedule(scheduledTask.getTask().getRunnable(), Instant.now());
	}
}
