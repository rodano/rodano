package ch.rodano.core.services.bll.robot;

import org.springframework.stereotype.Service;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.exception.http.BadArgumentException;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.model.robot.RobotSearch;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.dao.robot.RobotDAOService;

@Service
public class RobotServiceImpl implements RobotService {
	private final RobotDAOService robotDAOService;
	private final RoleService roleService;

	public RobotServiceImpl(
		final RobotDAOService robotDAOService,
		final RoleService roleService
	) {
		this.robotDAOService = robotDAOService;
		this.roleService = roleService;
	}

	@Override
	public Robot createRobot(
		final Robot robot,
		final Profile profile,
		final Scope roleScope,
		final DatabaseActionContext context,
		final String rationale
	) {
		// check if a robot with the same name exists already, if it exists throw
		if(robotDAOService.getRobotByName(robot.getName()) != null) {
			throw new BadArgumentException("A robot with the same name already exists");
		}
		checkKeyUniqueness(robot);

		robotDAOService.saveRobot(robot, context, rationale);

		// save and enable the role
		final var role = roleService.createRoleWithoutNotification(
			robot,
			profile,
			roleScope,
			context
		);
		roleService.enableRoleWithoutNotification(
			robot,
			role,
			context
		);

		return robot;
	}

	@Override
	public void saveRobot(final Robot robot, final DatabaseActionContext context, final String rationale) {
		// check if a robot with the same name exists already
		final var foundRobot = robotDAOService.getRobotByName(robot.getName());
		// if it exists, and it's not the already existing robot...
		// do not use the Robot::equals method to check if both robots are the same because this method only checks the name
		// what we want here is to check that there is no other robot with the same name and a different pk
		if(foundRobot != null && !robot.getPk().equals(foundRobot.getPk())) {
			// then reject it
			throw new BadArgumentException("A robot with the same name already exists");
		}
		checkKeyUniqueness(robot);

		robotDAOService.saveRobot(robot, context, rationale);
	}

	@Override
	public void deleteRobot(final Robot robot, final DatabaseActionContext context, final String rationale) {
		robotDAOService.deleteRobot(robot, context, rationale);
	}

	@Override
	public void restoreRobot(final Robot robot, final DatabaseActionContext context, final String rationale) {
		robotDAOService.restoreRobot(robot, context, rationale);
	}

	@Override
	public PagedResult<Robot> search(final RobotSearch search) {
		return robotDAOService.search(search);
	}

	@Override
	public Robot getRobot(final Role role) {
		return robotDAOService.getRobotByPk(role.getRobotFk());
	}

	/**
	 * Check if the provided robot key has already been used
	 */
	private void checkKeyUniqueness(final Robot updatedRobot) {
		// check if a robot with the same key exists already
		final var foundRobot = robotDAOService.getRobotByKey(updatedRobot.getKey());
		// if it exists, and it's not the already existing robot...
		if(foundRobot != null && !updatedRobot.equals(foundRobot)) {
			// then reject it
			throw new BadArgumentException("A robot with the same key already exists");
		}
	}
}
