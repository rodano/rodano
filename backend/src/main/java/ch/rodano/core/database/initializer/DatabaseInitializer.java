package ch.rodano.core.database.initializer;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;

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
import ch.rodano.core.helpers.UserCreatorService;
import ch.rodano.core.helpers.builder.UserBuilder;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.core.services.dao.audit.AuditActionService;

import static ch.rodano.core.model.jooq.tables.User.USER;

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
	private final AuditActionService auditActionService;
	private final UserCreatorService userCreatorService;
	private final UserSecurityService userSecurityService;

	private final String databaseName;

	public DatabaseInitializer(
		final DataSource dataSource,
		final DSLContext create,
		final AuditActionService auditActionService,
		final UserCreatorService userCreatorService,
		@Value("${rodano.database.name}") final String databaseName,
		final UserSecurityService userSecurityService) {
		this.dataSource = dataSource;
		this.create = create;
		this.auditActionService = auditActionService;
		this.userCreatorService = userCreatorService;
		this.userSecurityService = userSecurityService;
		this.databaseName = databaseName;
	}

	public List<String> getTables() {
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
		return create.selectCount().from(USER).fetchOne(0, int.class).equals(0);
	}

	public void initializeStructure() {
		if(structureExists()) {
			logger.warn("Database structure already exists. Skipping initialization.");
			return;
		}

		logger.info("Initializing database structure");
		final var databasePopulator = new ResourceDatabasePopulator();
		databasePopulator.addScript(new ClassPathResource(DATABASE_SCRIPTS_PATH + "tables.sql"));
		databasePopulator.addScript(new ClassPathResource(DATABASE_SCRIPTS_PATH + "indexes.sql"));
		databasePopulator.addScript(new ClassPathResource(DATABASE_SCRIPTS_PATH + "foreign_keys.sql"));
		databasePopulator.execute(this.dataSource);
	}

	public boolean structureExists() {
		final List<String> tables = getTables();
		return tables.contains("project") && tables.contains("scope");
	}

	/**
	 * Bootstrap the database
	 *
	 */
	@Transactional
	public void bootstrap(final String userEmail, final String userPassword, final String userName) {
		final UUID systemProjectId = UUID.fromString("00000000-0000-0000-0000-000000000000");
		final var context = auditActionService.createAuditActionAndGenerateContext(
			Actor.SYSTEM,
			DatabaseInitializer.RATIONALE,
			ZonedDateTime.now(),
			systemProjectId
		);

		final var password = userSecurityService.encodePassword(userPassword);
		final var user = UserBuilder.createUser(userName, userEmail)
			.setHashedPassword(password)
			.setLanguage(LanguageStatic.en)
			.setSuperuser(true)
			.getUserAndRoles();

		userCreatorService.createAndEnable(user, context);
	}
}
