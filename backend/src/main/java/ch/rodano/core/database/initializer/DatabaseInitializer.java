package ch.rodano.core.database.initializer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.core.helpers.UserCreatorService;
import ch.rodano.core.helpers.builder.UserBuilder;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.audit.AuditActionService;
import ch.rodano.core.services.plugin.validator.exception.BadlyFormattedValue;
import ch.rodano.core.services.plugin.validator.exception.InvalidValueException;

import static ch.rodano.core.model.jooq.Tables.SCOPE;

@Component
@Profile({ "api", "test", "database" })
public class DatabaseInitializer {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final static String DATABASE_SCRIPTS_PATH = "/database_scripts/structure/";
	public static final String RATIONALE = "Database initialization";
	public static final String TEST_USER_EMAIL = "test@rodano.ch";
	public static final String DEFAULT_PASSWORD = "Password1!";

	private final DataSource dataSource;
	private final DSLContext create;
	private final StudyService studyService;
	private final ScopeService scopeService;
	private final EventService eventService;
	private final FormService formService;
	private final DatasetService datasetService;
	private final WorkflowStatusService workflowStatusService;
	private final AuditActionService auditActionService;
	private final UserCreatorService userCreatorService;
	private final TestDataInitializer testDataInitializer;
	private final TestChartsInitializer testChartsInitializer;
	private final DemoUsersInitializer demoUsersInitializer;
	private final UserSecurityService userSecurityService;

	private final String databaseName;

	private final String usersPassword;
	private final Boolean cleanPatchTable;
	private final String internalPatchTable;

	public DatabaseInitializer(
		final DataSource dataSource,
		final DSLContext create,
		final StudyService studyService,
		final ScopeService scopeService,
		final EventService eventService,
		final FormService formService,
		final DatasetService datasetService,
		final WorkflowStatusService workflowStatusService,
		final AuditActionService auditActionService,
		final UserCreatorService userCreatorService,
		final TestDataInitializer testDataInitializer,
		final TestChartsInitializer testChartsInitializer,
		final DemoUsersInitializer demoUsersInitializer,
		@Value("${rodano.database.name}") final String databaseName,
		@Value("${rodano.init.users-password:Password1!}") final String usersPassword,
		@Value("${rodano.init.clean-patch-table:true}") final Boolean cleanPatchTable,
		@Value("${rodano.migration.internal-patch-table}") final String internalPatchTable,
		final UserSecurityService userSecurityService
	) {
		this.dataSource = dataSource;
		this.create = create;
		this.studyService = studyService;
		this.scopeService = scopeService;
		this.eventService = eventService;
		this.formService = formService;
		this.datasetService = datasetService;
		this.workflowStatusService = workflowStatusService;
		this.auditActionService = auditActionService;
		this.userCreatorService = userCreatorService;
		this.testDataInitializer = testDataInitializer;
		this.testChartsInitializer = testChartsInitializer;
		this.demoUsersInitializer = demoUsersInitializer;
		this.userSecurityService = userSecurityService;
		this.databaseName = databaseName;
		this.usersPassword = StringUtils.defaultIfBlank(usersPassword, DEFAULT_PASSWORD);
		this.cleanPatchTable = cleanPatchTable;
		this.internalPatchTable = internalPatchTable;
	}

	private List<String> getTables() {
		//do not use create.meta(...) here as it will fetch all tables for all database
		//see here https://stackoverflow.com/questions/24741761/how-to-check-if-a-table-exists-in-jooq
		final var tableName = DSL.field("TABLE_NAME", String.class);
		return create.select(tableName).from("information_schema.TABLES")
			.where(DSL.field("TABLE_SCHEMA").eq(databaseName).and(DSL.field("TABLE_TYPE").eq("BASE TABLE")))
			.fetch(tableName);
	}

	//check if the database is blank (no table)
	public boolean isDatabaseBlank() {
		return getTables().isEmpty();
	}

	//check if the database is empty (existing structure, but no data)
	public boolean isDatabaseEmpty() {
		return create.selectCount().from(SCOPE).fetchOne(0, int.class).equals(0);
	}

	public void initializeStructure() {
		logger.info("Initializing database structure");
		final var databasePopulator = new ResourceDatabasePopulator();
		databasePopulator.addScript(new ClassPathResource(DATABASE_SCRIPTS_PATH + "tables.sql"));
		databasePopulator.addScript(new ClassPathResource(DATABASE_SCRIPTS_PATH + "indexes.sql"));
		databasePopulator.addScript(new ClassPathResource(DATABASE_SCRIPTS_PATH + "foreign_keys.sql"));
		databasePopulator.execute(this.dataSource);
	}

	private Scope createRootScope(final DatabaseActionContext context, final ZonedDateTime origin, final String scopeName) {
		final Study study = studyService.getStudy();

		// Create the root scope
		final ScopeModel rootScopeModel = study.getRootScopeModel();
		final Scope root = new Scope();
		root.setScopeModel(rootScopeModel);
		root.setId(UUID.randomUUID().toString());
		root.setCode(study.getId());
		root.setShortname(scopeName);
		root.setStartDate(origin);

		scopeService.create(root, null, context, "Create root scope");

		// Initialize root scope data
		final var family = new DataFamily(root);
		workflowStatusService.createAll(family, root, null, context, RATIONALE);
		eventService.createAll(root, context, RATIONALE);
		datasetService.createAll(root, context, RATIONALE);
		formService.createAll(root, context, RATIONALE);

		return root;
	}

	/**
	 * Bootstrap the database
	 *
	 */
	@Transactional
	public void bootstrap(final String rootScopeName, final String userEmail, final String userPassword, final String userName) {
		final var origin = ZonedDateTime.now().minusMinutes(1).truncatedTo(ChronoUnit.MILLIS);
		final var context = auditActionService.createAuditActionAndGenerateContext(Actor.SYSTEM, DatabaseInitializer.RATIONALE);

		final Study study = studyService.getStudy();

		final var root = createRootScope(context, origin, rootScopeName);

		// Create first user
		final var adminProfile = study.getProfile("ADMIN");
		final var password = userSecurityService.encodePassword(userPassword);
		final var users = new ArrayList<UserCreatorService.UserCreation>();
		users.add(
			UserBuilder.createUser(userName, userEmail)
				.setHashedPassword(password)
				.setLanguage(LanguageStatic.en)
				.addRole(root, adminProfile)
				.getUserAndRoles()
		);

		// Save the first user
		userCreatorService.batchCreateAndEnable(users, context);
	}

	@Transactional
	public void initializeDatabaseContent(final boolean withUsers, final boolean withData) throws InvalidValueException, BadlyFormattedValue, IOException {
		logger.info("Initializing database content");
		// Set the reference date
		final ZonedDateTime origin;
		// If demo data, choose a date far in the past to let some room for demo event models
		if(withData) {
			origin = ZonedDateTime.now().minusYears(3).truncatedTo(ChronoUnit.MILLIS);
		}
		// If no demo data, use a date slightly in the past to make tests work properly
		else {
			origin = ZonedDateTime.now().minusMinutes(1).truncatedTo(ChronoUnit.MILLIS);
		}

		final var context = auditActionService.createAuditActionAndGenerateContext(Actor.SYSTEM, DatabaseInitializer.RATIONALE);

		addRequiredData(context, origin);

		// Add demo users
		if(withUsers) {
			logger.info("Add demo users");
			demoUsersInitializer.initialize(TEST_USER_EMAIL, usersPassword, context);
		}

		// Add demo data
		if(withData) {
			logger.info("Add demo data");
			testDataInitializer.initialize(origin);
			testChartsInitializer.initializeCharts();
		}
	}

	/**
	 * Add required data to the database
	 * This method requires the application to be started
	 *
	 * @param origin The origin date time
	 */
	private void addRequiredData(final DatabaseActionContext context, final ZonedDateTime origin) {
		logger.info("Add required data");

		final Study study = studyService.getStudy();

		final var root = createRootScope(context, origin, study.getDefaultLocalizedShortname());

		// Create the users
		final var adminProfile = study.getProfile("ADMIN");
		final var users = new ArrayList<UserCreatorService.UserCreation>();
		users.add(
			UserBuilder.createUser("Test user", TEST_USER_EMAIL)
				.setHashedPassword(userSecurityService.encodePassword(DEFAULT_PASSWORD))
				.setLanguage(LanguageStatic.en)
				.addRole(root, adminProfile)
				.getUserAndRoles()
		);

		// Save the users
		userCreatorService.batchCreateAndEnable(users, context);
	}

	/**
	 * Truncate all tables
	 */
	public void truncateTables() {
		logger.info("Emptying database");
		//retrieve list of all tables to truncate
		final List<String> tables = getTables();
		if(!cleanPatchTable) {
			tables.remove(internalPatchTable);
		}

		// Disable foreign key checks
		create.execute("set FOREIGN_KEY_CHECKS=0;");
		for(final var table : tables) {
			create.truncate(table).execute();
		}
		// Re-enable foreign key checks
		create.execute("set FOREIGN_KEY_CHECKS=1;");

		logger.info("Tables {} have been truncated successfully", tables);
	}
}
