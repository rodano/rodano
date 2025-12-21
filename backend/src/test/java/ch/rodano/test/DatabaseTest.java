package ch.rodano.test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StopWatch;

import ch.rodano.core.database.initializer.DatabaseInitializer;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.export.views.AggregateWorkflowViewService;
import ch.rodano.core.services.bll.export.views.ExportViewService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.ModelCatalogSyncService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.audit.AuditActionService;
import ch.rodano.core.services.dao.commons.cache.transaction.TransactionCacheDAOService;
import ch.rodano.core.services.plugin.validator.exception.BadlyFormattedValue;
import ch.rodano.core.services.plugin.validator.exception.InvalidValueException;
import ch.rodano.core.services.project.ProjectIdResolver;

/**
 * Class used to execute tests that require a database and care about its state
 */
public class DatabaseTest {

	private static boolean init = false;

	protected static final String TEST_RATIONALE = "Unit tests";

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	protected StudyService studyService;

	@Autowired
	protected ScopeService scopeService;

	@Autowired
	protected TransactionCacheDAOService transactionCacheDAOService;

	@Autowired
	protected AuditActionService auditActionService;

	@Autowired
	private DatabaseInitializer databaseInitializer;

	@Autowired
	private ExportViewService exportViewService;

	@Autowired
	private AggregateWorkflowViewService aggregateWorkflowViewService;

	@Autowired
	protected ModelCatalogSyncService modelCatalogSyncService;

	@Autowired
	private ProjectIdResolver projectIdResolver;

	@Value("${rodano.test.project-id:}")
	private String testProjectIdConfig;

	protected DatabaseActionContext context;
	protected Scope rootScope;
	protected UUID testProjectId;

	@BeforeEach
	public void setupDatabaseTest() throws InvalidValueException, BadlyFormattedValue, IOException {
		if(!init) {
			initializeDatabase();
			init = true;
		}

		if(!studyService.isStudyLoaded()) {
			studyService.loadStudyForProject(testProjectId);
		}

		ensureRootScopeAccessible();
		updateDatabaseActionContext();
	}

	private void initializeDatabase() throws InvalidValueException, BadlyFormattedValue, IOException {
		// Clear the transaction cache before initializing the database
		transactionCacheDAOService.emptyCache();

		final var watch = new StopWatch();
		watch.start();
		logger.info("Resetting the database by truncating all tables and re-adding required data");

		if(testProjectIdConfig != null && !testProjectIdConfig.isBlank()) {
			testProjectId = projectIdResolver.resolveCode(testProjectIdConfig);
			if(testProjectId == null) {
				testProjectId = UUID.randomUUID();
				logger.info("Generated new test project ID: {}", testProjectId);
			}
		}
		else {
			testProjectId = UUID.randomUUID();
			logger.info("No test project ID configured, using generated: {}", testProjectId);
		}

		projectIdResolver.setProjectId(testProjectId);

		if(databaseInitializer.isDatabaseBlank()) {
			logger.info("Database is blank, creating structure");
			databaseInitializer.initializeStructure();
		}
		else {
			logger.info("Database exists, truncating tables");
			databaseInitializer.truncateTables();
		}

		logger.info("Loading test study configuration for project: {}", testProjectId);
		studyService.loadStudyForProject(testProjectId);

		final var study = studyService.getStudy();

		databaseInitializer.initializeDatabaseContent(true, true);
		aggregateWorkflowViewService.updateView();

		modelCatalogSyncService.syncModelUuidsFromDatabase(study);

		watch.stop();
		logger.info("Database has been reset in {} ms", watch.getTotalTimeMillis());

		rootScope = scopeService.getRootScope();
		logger.info("Root scope initialized: code={}, projectId={}", rootScope.getCode(), rootScope.getProjectId());
	}

	private void ensureRootScopeAccessible() {
		try {
			rootScope = scopeService.getRootScope();
		}
		catch(IllegalStateException e) {
			final var allScopes = scopeService.getAllIncludingRemoved();
			allScopes.forEach(s -> logger.info("Scope: code={}, projectId={}, scopeModelId={}",
				s.getCode(), s.getProjectId(), s.getScopeModelId()));

			throw new IllegalStateException("Root scope should exist after database initialization", e);
		}
	}

	protected DatabaseActionContext createDatabaseActionContext(final String rationale) {
		return auditActionService.createAuditActionAndGenerateContext(Actor.SYSTEM, rationale, testProjectId);
	}

	protected DatabaseActionContext createDatabaseActionContext() {
		return createDatabaseActionContext(DatabaseInitializer.RATIONALE);
	}

	protected void updateDatabaseActionContext() {
		context = createDatabaseActionContext();
	}

	@AfterEach
	protected void emptyCacheAndReloadConfig() throws IOException {
		transactionCacheDAOService.emptyCache();

		final var originalProjectId = studyService.getCurrentProjectId();

		if(originalProjectId != null) {
			studyService.loadStudyForProject(originalProjectId);
		}

		try {
			rootScope = scopeService.getRootScope();
		}
		catch(IllegalStateException e) {
			logger.warn("Could not refresh root scope after config reload", e);
		}
	}

	protected boolean areDatesWithinASecond(final LocalDateTime firstDate, final LocalDateTime secondDate) {
		return secondDate.isAfter(firstDate.minusSeconds(1)) && secondDate.isBefore(firstDate.plusSeconds(1));
	}
}
