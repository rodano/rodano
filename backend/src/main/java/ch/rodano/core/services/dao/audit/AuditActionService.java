package ch.rodano.core.services.dao.audit;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.AuditAction;
import ch.rodano.core.model.audit.DatabaseActionContext;

import static ch.rodano.core.model.jooq.Tables.AUDIT_ACTION;

@Service
public class AuditActionService {

	private final DSLContext create;

	public AuditActionService(
		final DSLContext create
	) {
		this.create = create;
	}

	public DatabaseActionContext createAuditActionAndGenerateContext(final Optional<Actor> actor, final String rationale, final ZonedDateTime date) {
		final var action = new AuditAction(actor, rationale, date);
		final var record = create.newRecord(AUDIT_ACTION, action);
		record.store();
		action.setPk(record.getPk());
		return new DatabaseActionContext(action, actor);
	}

	public DatabaseActionContext createAuditActionAndGenerateContext(final Optional<Actor> actor, final String rationale) {
		return createAuditActionAndGenerateContext(actor, rationale, ZonedDateTime.now());
	}

	/**
	 * Get the audit action context of the current transaction, creating it lazily on first use.
	 * Creating it on demand, rather than eagerly when the transaction begins, lets it be attributed to whichever actor
	 * is known by the time it is actually needed, instead of requiring the actor to be resolved up front.
	 * The audit action row and the audit trail rows referencing it must commit or roll back together, which is why the
	 * context is bound to the transaction and not to the wider unit of work.
	 */
	public DatabaseActionContext getContext(final Optional<Actor> actor, final String rationale) {
		if(TransactionSynchronizationManager.hasResource(this)) {
			return (DatabaseActionContext) TransactionSynchronizationManager.getResource(this);
		}

		if(!TransactionSynchronizationManager.isSynchronizationActive()) {
			throw new IllegalStateException("An audit action context can only be created within a transaction");
		}

		final var context = createAuditActionAndGenerateContext(actor, rationale);
		TransactionSynchronizationManager.bindResource(this, context);

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(final int status) {
				TransactionSynchronizationManager.unbindResourceIfPossible(AuditActionService.this);
			}
		});

		return context;
	}
}
