package ch.rodano.api.administration;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.configuration.security.IsAdmin;
import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.core.configuration.core.Configurator;
import ch.rodano.core.configuration.core.Environment;
import ch.rodano.core.database.initializer.DatabaseInitializer;
import ch.rodano.core.database.initializer.DemoUsersInitializer;
import ch.rodano.core.database.initializer.RandomDataInitializer;
import ch.rodano.core.model.exception.UnauthorizedException;
import ch.rodano.core.model.exception.WrongDataConditionException;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.database.DatabaseUpdateService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.utils.RightsService;

@Tag(name = "Database administration")
@RestController
@RequestMapping("/administration/database")
@Validated
@Transactional(readOnly = true)
public class DatabaseController extends AbstractSecuredController {

	private final Configurator configurator;
	private final DatabaseInitializer databaseInitializer;
	private final DemoUsersInitializer demoUsersInitializer;
	private final RandomDataInitializer randomDataInitializer;
	private final TaskExecutor taskExecutor;
	private final DatabaseUpdateService databaseUpdateService;

	public DatabaseController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final Configurator configurator,
		final DatabaseInitializer databaseInitializer,
		final DemoUsersInitializer demoUsersInitializer,
		final RandomDataInitializer randomDataInitializer,
		final TaskExecutor taskExecutor,
		final DatabaseUpdateService databaseUpdateService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.configurator = configurator;
		this.databaseInitializer = databaseInitializer;
		this.demoUsersInitializer = demoUsersInitializer;
		this.randomDataInitializer = randomDataInitializer;
		this.taskExecutor = taskExecutor;
		this.databaseUpdateService = databaseUpdateService;
	}

	//TODO use a Spring actuator
	@Operation(summary = "Return the status of the database")
	@GetMapping("status")
	@ResponseStatus(HttpStatus.OK)
	public Map<String, String> checkStatus() {
		return Map.of("status", databaseInitializer.isDatabaseEmpty() ? "empty" : "initialized");
	}

	@Operation(summary = "Bootstrap an empty database")
	@PostMapping("bootstrap")
	@ResponseStatus(HttpStatus.OK)
	public void bootstrap(
		@Valid @RequestBody final BootstrapDTO bootstrap
	) {
		if(!databaseInitializer.isDatabaseEmpty()) {
			throw new WrongDataConditionException("Database has already been bootstraped");
		}

		databaseInitializer.initializeStructure();
		databaseInitializer.bootstrap(
			bootstrap.rootScopeName(),
			bootstrap.userEmail(),
			bootstrap.userPassword(),
			bootstrap.userName()
		);
	}

	@Operation(summary = "Add demo user in the database")
	@PostMapping("create-demo-users")
	@ResponseStatus(HttpStatus.OK)
	@IsAdmin
	public void createDemoUsers(
		@Valid @RequestBody final DemoUserSchemeDTO scheme
	) {
		if(Environment.PROD.equals(configurator.getEnvironment())) {
			throw new UnauthorizedException("Database can be updated only in non production mode");
		}

		demoUsersInitializer.initialize(scheme.baseEmail(), scheme.password(), currentContext());
	}

	@Operation(summary = "Fill the database with random data")
	@PostMapping("generate-random-data")
	@ResponseStatus(HttpStatus.OK)
	@IsAdmin
	public void generateRandomData(
		@RequestParam final Integer scale
	) {
		if(Environment.PROD.equals(configurator.getEnvironment())) {
			throw new UnauthorizedException("Database can be updated only in non production mode");
		}

		taskExecutor.execute(() -> {
			randomDataInitializer.fillDatabase(scale);
		});
	}

	@Operation(summary = "Check and optionally fix database consistency against the study configuration")
	@PostMapping("update")
	@ResponseStatus(HttpStatus.OK)
	@IsAdmin
	@Transactional
	public List<DatabaseIssueDTO> updateDatabase(
		@RequestBody final Map<String, Boolean> payload
	) {
		final var dryRun = payload.getOrDefault("dryRun", true);
		final var issues = databaseUpdateService.updateDatabase(dryRun, currentContext(), "Database consistency update");
		return issues.stream()
			.map(i -> new DatabaseIssueDTO(i.entity(), i.pk(), i.error(), i.status()))
			.toList();
	}

}
