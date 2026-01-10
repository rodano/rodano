package ch.rodano.core.services.bll.user.management;

import java.util.List;

import ch.rodano.api.user.management.CreateUserRequest;
import ch.rodano.api.user.management.UpdateUserRequest;
import ch.rodano.api.user.management.UserManagementDTO;
import ch.rodano.core.model.audit.DatabaseActionContext;

public interface UserManagementService {

	/**
	 * Get all users (for superuser view)
	 */
	List<UserManagementDTO> getAllUsers();

	/**
	 * Get a single user by pk
	 */
	UserManagementDTO getUser(Long userPk);

	/**
	 * Create a new user
	 */
	UserManagementDTO createUser(CreateUserRequest request, String contextUrl, DatabaseActionContext context);

	/**
	 * Update an existing user
	 */
	UserManagementDTO updateUser(Long userPk, UpdateUserRequest request, DatabaseActionContext context);

	/**
	 * Delete a user (soft delete)
	 */
	void deleteUser(Long userPk, DatabaseActionContext context);

	/**
	 * Restore a deleted user
	 */
	void restoreUser(Long userPk, DatabaseActionContext context);

	/**
	 * Toggle superuser status
	 */
	void toggleSuperuser(Long userPk, boolean isSuperuser, DatabaseActionContext context);

	/**
	 * Unblock a blocked user
	 */
	void unblockUser(Long userPk, DatabaseActionContext context);
}
