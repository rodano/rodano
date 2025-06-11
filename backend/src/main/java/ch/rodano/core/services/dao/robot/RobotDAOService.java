package ch.rodano.core.services.dao.robot;

import java.util.NavigableSet;
import java.util.Optional;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.RobotAuditTrail;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.model.robot.RobotSearch;

public interface RobotDAOService {

	/**
	 * Create or update a robot
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
	 * Get a robot by its pk
	 *
	 * @param pk      The pk of the robot
	 * @return The wanted robot or null if doesn't exist
	 */
	Robot getRobotByPk(Long pk);

	/**
	 * Get a robot by its name
	 *
	 * @param name The name of the robot
	 * @return The wanted robot or null if doesn't exist
	 */
	Robot getRobotByName(String name);

	/**
	 * Get a robot by its key
	 *
	 * @param key The key of the robot
	 * @return The wanted robot or null if doesn't exist
	 */
	Robot getRobotByKey(String key);

	/**
	 * Get a robot by its name and key
	 *
	 * @param name The name of the robot
	 * @param key  The key of the robot
	 * @return The wanted robot or null if doesn't exist
	 */
	Robot getRobotByNameAndKey(String name, String key);

	/**
	 * Search robots based on the given predicate
	 *
	 * @param predicate The predicate
	 * @return A list of robot
	 */
	PagedResult<Robot> search(RobotSearch predicate);

	NavigableSet<RobotAuditTrail> getAuditTrails(Robot robot, Optional<Timeframe> timeframe, Optional<Long> actorPk);
}
