package ch.rodano.api.request.context;

import java.util.Optional;

import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;

public interface RequestContextService {
	boolean isAuditedRequest();

	void setAuditedRequest(boolean auditedRequest);

	Optional<Actor> getActor();

	void setActor(Optional<Actor> actor);

	String getRationale();

	void setRationale(String rationale);

	void setDatabaseActionContext(final DatabaseActionContext context);

	DatabaseActionContext getDatabaseActionContext();
}
