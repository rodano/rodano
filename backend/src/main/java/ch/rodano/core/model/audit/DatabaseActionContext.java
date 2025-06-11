package ch.rodano.core.model.audit;

import java.util.Objects;
import java.util.Optional;

import ch.rodano.core.model.actor.Actor;

public record DatabaseActionContext(
	AuditAction auditAction,
	Optional<Actor> actor
) {

	public DatabaseActionContext {
		Objects.requireNonNull(auditAction);
		Objects.requireNonNull(actor);
	}

	public DatabaseActionContext(final AuditAction auditAction) {
		this(auditAction, Optional.empty());
	}

	public String getActorName() {
		return actor.map(Actor::getName).orElse(Actor.SYSTEM_USERNAME);
	}

	public DatabaseActionContext toSystemAction() {
		return new DatabaseActionContext(auditAction);
	}
}
