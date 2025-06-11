package ch.rodano.core.services.dao.role;

import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.RoleAuditTrail;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.role.Role;

public interface RoleDAOService {

	Role getRoleByPk(Long pk);

	/**
	 * Get all roles of a user
	 *
	 * @param userPk The user pk
	 * @return The roles of a user
	 */
	List<Role> getRolesByUserPk(Long userPk);

	/**
	 * Get all roles for a collection of user pks
	 *
	 * @param userPks The user pks
	 * @return The roles of a user
	 */
	List<Role> getRolesByUserPks(Collection<Long> userPks);

	/**
	 * Get all roles of a robot
	 *
	 * @param robotPk The user pk
	 * @return The roles of a robot
	 */
	List<Role> getRolesByRobotPk(Long robotPk);

	/**
	 * Get all roles for a collection of robot pks
	 *
	 * @param robotPks The robot pks
	 * @return The roles of a user
	 */
	List<Role> getRolesByRobotPks(Collection<Long> robotPks);

	/**
	 * Get all roles for a given profile id
	 *
	 * @param profileId The given profile id
	 * @return The roles of a profile
	 */
	List<Role> getRolesByProfile(String profileId);

	/**
	 * Get roles for the corresponding scope pk and profile ids
	 *
	 * @param scopePk   The scope pk
	 * @param profileIds The collection of profile ids
	 * @return All roles corresponding to the given scope pk and profile ids
	 */
	List<Role> getRolesByScopePkAndProfiles(Long scopePk, Collection<String> profileIds);

	/**
	 * Get the active roles on or over the given scope pk
	 *
	 * @param userPk The user pk
	 * @param scopePk   The scope pk
	 * @return The active roles of the user on or over the given scope pk
	 */
	List<Role> getActiveRolesByUserPkOverScopePk(Long userPk, Long scopePk);

	/**
	 * Get the active roles on or over the given scope pk
	 *
	 * @param robotPk The robot pk
	 * @param scopePk   The scope pk
	 * @return The active roles of the robot on or over the given scope pk
	 */
	List<Role> getActiveRolesByRobotPkOverScopePk(Long robotPk, Long scopePk);

	/**
	 * Create or update a role
	 *
	 * @param role    The role to create or update
	 * @param context The context in which the action takes place
	 * @param rationale The rationale for the operation
	 */
	void saveRole(Role role, DatabaseActionContext context, String rationale);

	NavigableSet<RoleAuditTrail> getAuditTrails(Role role, Optional<Timeframe> timeframe, Optional<Long> actorPk);

}
