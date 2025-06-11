package ch.rodano.core.services.bll.robot;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.model.robot.RobotSearch;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;

public interface RobotService {

	/**
	 * Create a robot
	 *
	 * @param robot     The robot to save
	 * @param profile    Profile of the robot
	 * @param roleScope Scope to which the robot belongs
	 * @param context   The context in which the action takes place
	 * @return The saved robot
	 */
	Robot createRobot(
		Robot robot,
		Profile profile,
		Scope roleScope,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Save a robot
	 *
	 * @param robot   The robot to save
	 * @param context The context in which the action takes place
	 * @param rationale The rationale for the operation
	 */
	void saveRobot(Robot robot, DatabaseActionContext context, String rationale);

	/**
	 * Delete a robot
	 *
	 * @param robot   The robot to delete
	 * @param context The context in which the action takes place
	 * @param rationale The rationale for the operation
	 */
	void deleteRobot(Robot robot, DatabaseActionContext context, String rationale);

	/**
	 * Restore a robot
	 *
	 * @param robot   The robot to restore
	 * @param context The context in which the action takes place
	 * @param rationale The rationale for the operation
	 */
	void restoreRobot(Robot robot, DatabaseActionContext context, String rationale);

	/**
	 * Search robots based on the given predicate
	 *
	 * @param search The predicate
	 * @return A list of robot
	 */
	PagedResult<Robot> search(RobotSearch search);

	Robot getRobot(final Role role);

}
