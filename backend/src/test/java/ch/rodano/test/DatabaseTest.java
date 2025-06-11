package ch.rodano.test;

import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import ch.rodano.core.database.initializer.DatabaseInitializer;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.services.bll.export.views.AggregateWorkflowViewService;
import ch.rodano.core.services.bll.export.views.ExportViewService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.audit.AuditActionService;
import ch.rodano.core.services.dao.commons.cache.transaction.TransactionCacheDAOService;

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
	protected TransactionCacheDAOService transactionCacheDAOService;

	@Autowired
	protected AuditActionService auditActionService;

	@Autowired
	private DatabaseInitializer databaseInitializer;

	@Autowired
	private ExportViewService exportViewService;

	@Autowired
	private AggregateWorkflowViewService aggregateWorkflowViewService;

	protected DatabaseActionContext context;

	@BeforeAll
	protected void initializeTests() throws Exception {
		if(!init) {
			// Clear the transaction cache before initializing the database
			transactionCacheDAOService.emptyCache();

			final var watch = new StopWatch();
			watch.start();
			logger.info("Resetting the database by truncating all tables and re-adding required data");

			//initialize database if it is blank
			if(databaseInitializer.isDatabaseBlank()) {
				databaseInitializer.initializeStructure();
			}
			// empty database
			else {
				databaseInitializer.truncateTables();
			}
			databaseInitializer.initializeDatabaseContent(true, true);
			exportViewService.updateViews();
			aggregateWorkflowViewService.updateView();

			watch.stop();
			logger.info("Database has been reset in " + watch.getTotalTimeMillis() + " ms");

			init = true;
		}
	}

	protected DatabaseActionContext createDatabaseActionContext(final String rationale) {
		return auditActionService.createAuditActionAndGenerateContext(Actor.SYSTEM, rationale);
	}

	protected DatabaseActionContext createDatabaseActionContext() {
		return createDatabaseActionContext(DatabaseInitializer.RATIONALE);
	}

	@BeforeEach
	protected void updateDatabaseActionContext() {
		context = createDatabaseActionContext();
	}

	@AfterEach
	protected void emptyCacheAndReloadConfig() throws IOException {
		transactionCacheDAOService.emptyCache();
		studyService.reload();
	}

	protected boolean areDatesWithinASecond(final LocalDateTime firstDate, final LocalDateTime secondDate) {
		return secondDate.isAfter(firstDate.minusSeconds(1)) && secondDate.isBefore(firstDate.plusSeconds(1));
	}
}
