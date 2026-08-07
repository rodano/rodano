package ch.rodano.api.request.context;

import java.util.Optional;

import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;

public interface RequestContextService {
	/**
	 * Record the actor and rationale of an audited request. Non-audited requests never call this.
	 */
	void setAuditContext(Optional<Actor> actor, String rationale);

	/**
	 * The audit context for the current request, or {@code null} if the request is not audited.
	 */
	DatabaseActionContext getDatabaseActionContext();
}
