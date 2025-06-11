package ch.rodano.core.services.bll.role;

import java.util.List;

import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.rights.Attributable;
import ch.rodano.configuration.model.rights.RightAssignable;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;

public interface RoleService {

	/**
	 * Get all the roles that are active for the actor
	 *
	 * @return List of Role
	 */
	List<Role> getRoles(Actor actor);

	List<Role> getRoles(Actor actor, FeatureStatic feature);

	List<Role> getRoles(Scope scope, Profile profile);

	/**
	 * Get all the active roles associated with the actor
	 *
	 * @return A sorted list of all the active roles associated with the actor.
	 */
	List<Role> getActiveRoles(Actor actor);

	List<Role> getActiveRoles(Actor actor, Scope scope);

	List<Role> getActiveRoles(Actor actor, FeatureStatic feature);

	List<Role> getActiveRoles(Actor actor, String feature);

	List<Role> getActiveRoles(Actor actor, RightAssignable<?> rightAssignable, Rights right);

	List<Role> getActiveRoles(Actor actor, Attributable<?> attributable);

	List<Role> getActiveRoles(Scope scope, Profile profile);

	/**
	 * Creates a role without the actor e-mail notification. Mostly used for robot creation and internal use.
	 *
	 * @param actor Actor for whom role will be created
	 * @param profile Profile of the new role
	 * @param scope Scope of the new role
	 * @return The new role
	 */
	Role createRoleWithoutNotification(
		Actor actor,
		Profile profile,
		Scope scope,
		DatabaseActionContext context
	);

	/**
	 * Creates a new role for a user.
	 *
	 * @param user The user for whom the role will be created
	 * @param profile Profile of the new role
	 * @param scope Scope of the new role
	 * @return The new role
	 */
	Role createRole(
		final User user,
		final Profile profile,
		final Scope scope,
		DatabaseActionContext context
	);

	void updateRole(Role role, DatabaseActionContext context, String rationale);

	/**
	 * Enable a role without the actor e-mail notification. Mostly for internal use.
	 *
	 * @param actor Actor of the role
	 * @param role The role to enable
	 * @param context Database context
	 */
	void enableRoleWithoutNotification(
		Actor actor,
		Role role,
		DatabaseActionContext context
	);

	/**
	 * Enable a role
	 *
	 * @param user    Actor of the role
	 * @param role    The role to enable
	 * @param context Database context
	 */
	void enableRole(
		User user,
		Role role,
		DatabaseActionContext context
	);

	/**
	 * Disable a role
	 * @param actor     Actor of the role
	 * @param role      The role to disable
	 * @param context   Database context
	 */
	void disableRole(
		Actor actor,
		Role role,
		DatabaseActionContext context
	);

	/**
	 * Reject a user role. Used when the user rejects the privacy policies of the role.
	 * @param actor     Actor of the role
	 * @param role      The role to reject
	 * @param context   Database context
	 */
	void rejectRole(
		Actor actor,
		Role role,
		DatabaseActionContext context
	);
}
