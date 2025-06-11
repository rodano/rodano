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

//TODO delete this and the associated "database" Spring profile. Initialization must be made by the main application
@Profile("database")
@Configuration
@ComponentScan(basePackages = "ch.rodano.core")
public class DatabaseInitConfiguration implements CommandLineRunner {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Boolean withUsers;

	private final Boolean withData;

	private final DatabaseInitializer databaseInitializer;

	private final ScopeAncestorServiceImpl scopeAncestorService;

	public DatabaseInitConfiguration(
		@Value("${rodano.init.with-users:false}") final Boolean withUsers,
		@Value("${rodano.init.with-data:false}") final Boolean withData,
		final DatabaseInitializer databaseInitializer,
		final ScopeAncestorServiceImpl scopeAncestorService
	) {
		logger.info("Starting database profile");
		this.withUsers = withUsers;
		this.withData = withData;
		this.databaseInitializer = databaseInitializer;
		this.scopeAncestorService = scopeAncestorService;
	}

	@Override
	public void run(final String... args) throws Exception {
		if(databaseInitializer.isDatabaseBlank()) {
			databaseInitializer.initializeStructure();
		}
		else {
			databaseInitializer.truncateTables();
		}
		scopeAncestorService.updateView();
		databaseInitializer.initializeDatabaseContent(withUsers, withData);
	}

}
