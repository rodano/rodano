package ch.rodano.api.request.context;

import java.util.Optional;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.services.dao.audit.AuditActionService;

/**
 * Stores the HTTP request related info.
 */
@Service
@Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
public class RequestContextServiceImpl implements RequestContextService {

	private final AuditActionService auditActionService;

	// The actor and rationale of the request, set once by the interceptor for audited requests, null otherwise
	private Optional<Actor> actor;
	private String rationale;

	public RequestContextServiceImpl(final AuditActionService auditActionService) {
		this.auditActionService = auditActionService;
	}

	@Override
	public void setAuditContext(final Optional<Actor> actor, final String rationale) {
		this.actor = actor;
		this.rationale = rationale;
	}

	@Override
	public DatabaseActionContext getDatabaseActionContext() {
		//a non-audited request never sets the actor, so a null actor means the request is not audited
		//the context is built lazily on first use within the current transaction, rather than eagerly when the
		//transaction begins, so it can be attributed to the actor resolved by then (see AuditActionService.getContext)
		if(actor == null) {
			return null;
		}
		return auditActionService.getContext(actor, rationale);
	}
}
