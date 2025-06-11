package ch.rodano.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.rodano.core.database.initializer.DatabaseInitializer;
import ch.rodano.core.services.bll.export.views.AggregateWorkflowViewService;
import ch.rodano.core.services.bll.export.views.ExportViewService;
import ch.rodano.core.services.bll.scope.ScopeAncestorServiceImpl;

@Profile({ "api" })
@Configuration
@ComponentScan(basePackages = { "ch.rodano.core", "ch.rodano.api", "ch.rodano.studies" })
public class ApiConfiguration implements InitializingBean {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final DatabaseInitializer databaseInitializer;
	private final ScopeAncestorServiceImpl scopeAncestorService;
	private final AggregateWorkflowViewService aggregateWorkflowViewService;
	private final ExportViewService exportViewService;

	public ApiConfiguration(
		final DatabaseInitializer databaseInitializer,
		final ScopeAncestorServiceImpl scopeAncestorService,
		final AggregateWorkflowViewService aggregateWorkflowViewService,
		final ExportViewService exportViewService
	) {
		this.databaseInitializer = databaseInitializer;
		this.scopeAncestorService = scopeAncestorService;
		this.aggregateWorkflowViewService = aggregateWorkflowViewService;
		this.exportViewService = exportViewService;
	}

	@Override
	public void afterPropertiesSet() {
		logger.info("Starting api profile");

		//initialize database if it is blank
		if(databaseInitializer.isDatabaseBlank()) {
			logger.info("Database is blank, initializing database");
			databaseInitializer.initializeStructure();
		}

		//initialize views in all cases
		scopeAncestorService.updateView();
		aggregateWorkflowViewService.updateView();
		exportViewService.updateViews();
	}
}
