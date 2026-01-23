package ch.rodano.core.services.bll.project;

import java.util.UUID;

import ch.rodano.core.model.user.User;

public interface ProjectAccessService {

	/**
	 * Check if user can read data in this project
	 *
	 * @param user      the user to check
	 * @param projectId the project ID
	 * @return true if user can read
	 */
	boolean canRead(User user, UUID projectId);

	/**
	 * Check if user can write data in this project
	 *
	 * @param user      the user to check
	 * @param projectId the project ID
	 * @return true if user can write
	 */
	boolean canWrite(User user, UUID projectId);

	/**
	 * Check if user can change project status (ACTIVE/CLOSED/ARCHIVED)
	 * Only superusers are allowed to change project status
	 *
	 * @param user      the user to check
	 * @param projectId the project ID
	 * @return true if user is superuser
	 */
	boolean canChangeProjectStatus(User user, UUID projectId);
}
