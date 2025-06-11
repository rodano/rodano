package ch.rodano.test;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ch.rodano.core.database.initializer.DatabaseInitializer;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.audit.AuditActionService;
import ch.rodano.core.services.dao.commons.cache.transaction.TransactionCacheDAOService;

/**
 * Class used to execute tests that require a database but don't care about its state
 */
public class StatelessDatabaseTest {

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

	protected DatabaseActionContext context;

	@BeforeAll
	protected void initializeTests() {
		// Clear the transaction cache before initializing the database
		transactionCacheDAOService.emptyCache();

		logger.info("Ensuring that a database exists");

		//initialize database if it is blank
		if(databaseInitializer.isDatabaseBlank()) {
			databaseInitializer.initializeStructure();
		}
	}

	protected DatabaseActionContext createDatabaseActionContext() {
		return auditActionService.createAuditActionAndGenerateContext(Actor.SYSTEM, DatabaseInitializer.RATIONALE);
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
}
