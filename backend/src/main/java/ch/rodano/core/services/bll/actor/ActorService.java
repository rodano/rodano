package ch.rodano.core.services.bll.actor;

import java.util.List;
import java.util.Optional;

import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.rights.Attributable;
import ch.rodano.configuration.model.rights.RightAssignable;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;

public interface ActorService {

	Actor getActor(Role role);

	String[] getLanguages(Actor actor);

	Optional<Timeframe> getTimeframeForScope(Actor actor, Scope scope, List<Role> roles);

	List<Profile> getActiveProfiles(Actor actor);

	List<Profile> getActiveProfiles(Actor actor, Scope scope);

	List<Scope> getRootScopes(Actor actor);

	List<Scope> getRootScopes(Actor actor, Profile profile);

	List<Scope> getRootScopes(Actor actor, FeatureStatic feature);

	List<Scope> getRootScopes(Actor actor, RightAssignable<?> rightAssignable, Rights right);

	List<Scope> getRootScopes(Actor actor, Attributable<?> attributable);

	/**
	 * Retrieve the highest scopes on which actor has an active role
	 */
	Optional<Scope> getRootScope(Actor actor);

	Optional<Scope> getRootScope(Actor actor, Profile profile);

	Optional<Scope> getRootScope(Actor actor, FeatureStatic feature);

	Optional<Scope> getRootScope(Actor actor, RightAssignable<?> rightAssignable, Rights right);

	Optional<Scope> getRootScope(Actor actor, Attributable<?> attributable);

}
