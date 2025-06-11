package ch.rodano.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.rodano.core.services.bll.scope.ScopeAncestorServiceImpl;

@Profile("migration")
@Configuration
@ComponentScan(basePackages = { "ch.rodano.core", "ch.rodano.studies" })
public class MigrationConfiguration {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public MigrationConfiguration(
		final ScopeAncestorServiceImpl scopeAncestorService
	) {
		logger.info("Starting migration profile");
		scopeAncestorService.updateView();
	}
}
