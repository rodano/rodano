package ch.rodano.core.services.dao.audit;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

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

}
