package ch.rodano.core.configuration.jooq;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.web.context.request.RequestContextHolder;

import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.core.services.dao.audit.AuditActionService;

/**
 * Overrides the transaction manager provided by Spring.
 */
public class AuditActionTransactionManager extends DataSourceTransactionManager {
	private static final long serialVersionUID = -3326997135903793557L;

	@Autowired
	@Lazy
	AuditActionService auditActionService;

	@Autowired
	@Lazy
	RequestContextService requestContextService;

	public AuditActionTransactionManager(
		final DataSource dataSource
	) {
		super(dataSource);
	}

	/**
	 * Overrides the normal transaction start behaviour.
	 * If the transaction is new, the request context service is present and the request is an audited request, then
	 * it creates a new audit action and sets it as the current database action context in the request context service.
	 */
	@Override
	public void doBegin(final Object transaction, final TransactionDefinition definition) {
		final var existingTransaction = isExistingTransaction(transaction);
		super.doBegin(transaction, definition);

		// RequestContextHolder.getRequestAttributes() is used here to check if the spring REQUEST scope is available.
		if(!existingTransaction && RequestContextHolder.getRequestAttributes() != null && requestContextService.isAuditedRequest()) {

			final var context = auditActionService.createAuditActionAndGenerateContext(requestContextService.getActor(), requestContextService.getRationale());
			requestContextService.setDatabaseActionContext(context);

			logger.info(String.format("Generating database context with actor %s", context.getActorName()));
		}
	}
}
