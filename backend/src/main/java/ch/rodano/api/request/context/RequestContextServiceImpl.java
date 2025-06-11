package ch.rodano.api.request.context;

import java.util.Optional;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;

/**
 * Stores the HTTP request related info.
 */
@Service
@Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
public class RequestContextServiceImpl implements RequestContextService {

	// Is the request and audited one or not
	private boolean auditedRequest = false;
	// The actor of the request
	private Optional<Actor> actor;
	// The request context/rationale. Usually the HTTP method plus URL string
	private String rationale;

	// The database action context used by the DAO for audit operations
	private DatabaseActionContext databaseActionContext;

	@Override
	public boolean isAuditedRequest() {
		return auditedRequest;
	}

	@Override
	public void setAuditedRequest(final boolean auditedRequest) {
		this.auditedRequest = auditedRequest;
	}

	@Override
	public Optional<Actor> getActor() {
		return actor;
	}

	@Override
	public void setActor(final Optional<Actor> actor) {
		this.actor = actor;
	}

	@Override
	public String getRationale() {
		return rationale;
	}

	@Override
	public void setRationale(final String rationale) {
		this.rationale = rationale;
	}

	@Override
	public void setDatabaseActionContext(final DatabaseActionContext context) {
		databaseActionContext = context;
	}

	@Override
	public DatabaseActionContext getDatabaseActionContext() {
		return databaseActionContext;
	}
}
