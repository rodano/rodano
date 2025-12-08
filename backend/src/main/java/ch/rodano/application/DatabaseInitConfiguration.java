package ch.rodano.application;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.SimpleThreadScope;

import ch.rodano.core.database.initializer.DatabaseInitializer;
import ch.rodano.core.services.bll.scope.ScopeAncestorServiceImpl;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.project.ProjectIdResolver;

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

	private final StudyService studyService;

	private final ProjectIdResolver projectIdResolver;

	public DatabaseInitConfiguration(
		@Value("${rodano.init.with-users:false}") final Boolean withUsers,
		@Value("${rodano.init.with-data:false}") final Boolean withData,
		final DatabaseInitializer databaseInitializer,
		final ScopeAncestorServiceImpl scopeAncestorService,
		final StudyService studyService,
		final ProjectIdResolver projectIdResolver
	) {
		logger.info("Starting database profile");
		this.withUsers = withUsers;
		this.withData = withData;
		this.databaseInitializer = databaseInitializer;
		this.scopeAncestorService = scopeAncestorService;
		this.studyService = studyService;
		this.projectIdResolver = projectIdResolver;
	}

	@Bean
	public static CustomScopeConfigurer sessionScopeConfigurer() {
		final CustomScopeConfigurer configurer = new CustomScopeConfigurer();
		configurer.addScope("session", new SimpleThreadScope());
		return configurer;
	}

	@Override
	public void run(final String... args) throws Exception {
		if(!projectIdResolver.hasProject()) {
			projectIdResolver.setProjectId(UUID.randomUUID());
		}

		studyService.loadStudyForProject(projectIdResolver.id());

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
