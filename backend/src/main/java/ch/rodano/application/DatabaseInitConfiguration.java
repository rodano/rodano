package ch.rodano.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.rodano.core.database.initializer.DatabaseInitializer;
import ch.rodano.core.services.bll.scope.ScopeAncestorServiceImpl;
import ch.rodano.core.services.unitofwork.UnitOfWorkService;

/*
 * Allow to programmatically initialize the database during application startup.
 * In standard processes, the database must be initialized during the main application's bootstrap phase through the web interface.
 */
@Profile("database")
@Configuration
@ComponentScan(basePackages = "ch.rodano.core")
public class DatabaseInitConfiguration implements CommandLineRunner {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Boolean withUsers;
	private final Boolean withData;

	private final String creatorUserEmail;
	private final String creatorUserName;
	private final String usersPassword;

	private final DatabaseInitializer databaseInitializer;

	private final ScopeAncestorServiceImpl scopeAncestorService;

	private final UnitOfWorkService unitOfWorkService;

	public DatabaseInitConfiguration(
		@Value("${rodano.init.with-users:false}") final Boolean withUsers,
		@Value("${rodano.init.with-data:false}") final Boolean withData,
		@Value("${rodano.init.creator-user-email}") final String creatorUserEmail,
		@Value("${rodano.init.creator-user-name}") final String creatorUserName,
		@Value("${rodano.init.users-password}") final String usersPassword,
		final DatabaseInitializer databaseInitializer,
		final ScopeAncestorServiceImpl scopeAncestorService,
		final UnitOfWorkService unitOfWorkService
	) {
		logger.info("Starting database profile");
		this.withUsers = withUsers;
		this.withData = withData;
		this.creatorUserEmail = creatorUserEmail;
		this.usersPassword = usersPassword;
		this.creatorUserName = creatorUserName;
		this.databaseInitializer = databaseInitializer;
		this.scopeAncestorService = scopeAncestorService;
		this.unitOfWorkService = unitOfWorkService;
	}

	@Override
	public void run(final String... args) throws Exception {
		unitOfWorkService.run(false, () -> {
			if(databaseInitializer.isDatabaseBlank()) {
				databaseInitializer.initializeStructure();
			}
			else {
				databaseInitializer.truncateTables();
			}
			scopeAncestorService.updateView();
			databaseInitializer.initializeDatabaseContent(withUsers, withData, creatorUserEmail, creatorUserName, usersPassword);
			return null;
		});
	}

}
