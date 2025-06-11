package ch.rodano.core.model.actor;

import java.security.Principal;
import java.util.Optional;

import ch.rodano.core.model.common.IdentifiableObject;

public interface Actor extends IdentifiableObject, Principal {

	Optional<Actor> SYSTEM = Optional.<Actor> empty();
	// By convention, the SYSTEM role is called "SYSTEM"
	String SYSTEM_USERNAME = "SYSTEM";

	ActorType getType();

	String getLanguageId();

	boolean isActivated();
}
