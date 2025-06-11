package ch.rodano.core.utils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.feature.Feature;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.rights.Assignable;
import ch.rodano.configuration.model.rights.Attributable;
import ch.rodano.configuration.model.rights.ProfileRightAssignable;
import ch.rodano.configuration.model.rights.RightAssignable;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.resource.Resource;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;

public interface RightsService {

	/**
	 * @param actor
	 * @return The ACL for the provided user with no consideration of a scope
	 */
	ACL getACL(Actor actor);

	/**
	 * @param actor
	 * @param scope
	 * @return The ACL for the provided user over the specified scope
	 */
	ACL getACL(Actor actor, Scope scope);

	List<Role> filterEnabledRoles(Collection<Role> roles);

	List<Role> filterRoles(Collection<Role> roles, Scope scope);

	List<Role> filterRoles(Collection<Role> roles, Feature feature);

	List<Role> filterRoles(Collection<Role> roles, RightAssignable<?> assignableRight, Rights right);

	void checkRight(Actor actor, List<Role> roles, Scope scope);

	void checkRightAdmin(Actor actor, List<Role> roles);

	void checkRight(Actor actor, Collection<Role> roles, Feature feature);

	void checkRight(Actor actor, Collection<Role> roles, FeatureStatic staticFeature);

	void checkRight(Actor actor, Collection<Role> roles, Assignable<?> node);

	void checkRight(Actor actor, Collection<Role> roles, Attributable<?> node);

	//right assignable
	Role checkRight(Actor actor, Collection<Role> roles, RightAssignable<?> rightAssignable, Rights right);

	Role checkRight(Actor actor, Collection<Role> roles, Entity entity, Rights right);

	void checkRight(Actor actor, List<Role> roles, User targetUser, Rights right);

	void checkRight(Actor actor, Collection<Role> roles, ProfileRightAssignable<?> profileRightAssignable, Optional<Profile> creatorProfile);

	void checkRightToRead(Actor actor, List<Role> roles, Resource resource);

	boolean hasRight(Scope scope, Rights right, Collection<Role> roles);

	boolean hasRight(Collection<Role> roles, Entity entity, Rights right);

	boolean hasRight(Collection<Role> roles, Entity entity, String nodeId, Rights right);

	// TODO this shouldn't be here, maybe move it to the Role Service?
	Optional<Role> getRole(Collection<Role> roles, RightAssignable<?> rightAssignable, Rights right);

	boolean hasRightAdmin(Collection<Role> roles);

	boolean hasRight(Collection<Role> roles, FeatureStatic feature);

	boolean hasRight(Collection<Role> roles, Assignable<?> assignable);

	boolean hasRight(Collection<Role> roles, RightAssignable<?> rightAssignable, Rights right);

	boolean hasRight(Collection<Role> roles, Attributable<?> attributable);

	boolean hasRight(Collection<Role> roles, ProfileRightAssignable<?> profileRightAssignable);

	//TODO delete this when the advanced workflow matrix has been updated
	boolean hasRight(Collection<Role> roles, ProfileRightAssignable<?> profileRightAssignable, Optional<Profile> creatorProfile);

	boolean hasRightToRead(Collection<Role> roles, Resource resource);

	boolean hasRightOnActorRole(Actor actor, Collection<Role> roles, Actor targetActor, Role targetRole, Rights right);

	boolean hasRight(Actor actor, Collection<Role> roles, Actor targetActor, Rights right);

	/**
	 * Returns true if the actor has the right on the target actor, providing the roles of the target roles
	 * @param actor the actor asking for the right
	 * @param roles the roles of the actor asking for the right
	 * @param targetActor the target actor
	 * @param targetRoles the roles of the target actor
	 * @param right the right
	 * @return True if the actor has the right on the target actor
	 */
	boolean hasRight(Actor actor, Collection<Role> roles, Actor targetActor, Collection<Role> targetRoles, Rights right);

}
