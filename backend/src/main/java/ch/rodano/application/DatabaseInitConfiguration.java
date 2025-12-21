package ch.rodano.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.SimpleThreadScope;

import ch.rodano.core.database.initializer.DatabaseInitializer;
import ch.rodano.core.services.bll.scope.ScopeAncestorServiceImpl;

//TODO delete this and the associated "database" Spring profile. Initialization must be made by the main application
@Profile("database")
@Configuration
@ComponentScan(basePackages = "ch.rodano.core")
public class DatabaseInitConfiguration implements CommandLineRunner {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final DatabaseInitializer databaseInitializer;
	private final ScopeAncestorServiceImpl scopeAncestorService;

	public DatabaseInitConfiguration(
		final DatabaseInitializer databaseInitializer,
		final ScopeAncestorServiceImpl scopeAncestorService
	) {
		logger.info("Starting database profile");
		this.databaseInitializer = databaseInitializer;
		this.scopeAncestorService = scopeAncestorService;
	}

	@Bean
	public static CustomScopeConfigurer sessionScopeConfigurer() {
		final CustomScopeConfigurer configurer = new CustomScopeConfigurer();
		configurer.addScope("session", new SimpleThreadScope());
		return configurer;
	}

	@Override
	public void run(final String... args) throws Exception {

		if(databaseInitializer.isDatabaseBlank()) {
			databaseInitializer.initializeStructure();
			scopeAncestorService.updateView();
		}
		else if(databaseInitializer.structureExists()) {
			System.exit(1);
		}
	}

}
