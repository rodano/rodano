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
import org.springframework.context.annotation.Import;

import ch.rodano.core.database.initializer.DatabaseInitializer;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.audit.AuditActionService;
import ch.rodano.core.services.dao.commons.cache.transaction.TransactionCacheDAOService;
import ch.rodano.core.services.project.ProjectIdResolver;

/**
 * Class used to execute tests that require a database and care about its state
 */
@Import({ TestProjectIdResolverConfig.class, TestDatabaseConfig.class })
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
	private ProjectIdResolver projectIdResolver;

	@Value("${rodano.test.project.id}")
	private String testProjectIdString;

	protected DatabaseActionContext context;
	protected Scope rootScope;
	protected UUID testProjectId;

	@BeforeEach
	public void setupDatabaseTest() throws IOException {
		testProjectId = UUID.fromString(testProjectIdString);

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

	private void initializeDatabase() throws IOException {
		logger.info("Initializing test database from SQL dump");

		projectIdResolver.setProjectId(testProjectId);
		studyService.loadStudyForProject(testProjectId);

		logger.info("✓ Test database initialized");
	}

	private void ensureRootScopeAccessible() {
		try {
			rootScope = scopeService.getRootScope();
			logger.info("Root scope loaded: {}", rootScope.getCode());
		} catch (IllegalStateException e) {
			logger.warn("Root scope not found - this is expected if test-db-init.sql is empty");
			rootScope = null;
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
	protected void emptyCacheAndReloadConfig() {
		transactionCacheDAOService.emptyCache();
	}

	protected boolean areDatesWithinASecond(final LocalDateTime firstDate, final LocalDateTime secondDate) {
		return secondDate.isAfter(firstDate.minusSeconds(1)) && secondDate.isBefore(firstDate.plusSeconds(1));
	}
}
