package ch.rodano.api.actor;

import java.util.Collection;
import java.util.List;

import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.user.User;

public interface ActorDTOService {

	RobotDTO createRobotDTO(Robot robot, Actor actor, List<Role> roles);

	Robot generateRobot(RobotCreationDTO robotCreationDTO);

	void updateRobot(Robot robot, RobotUpdateDTO robotDTO);

	List<RobotDTO> createRobotDTOs(Collection<Robot> robots, Actor actor, List<Role> roles);

	UserDTO createUserDTO(User user, Actor actor, List<Role> roles);

	List<UserDTO> createUserDTOs(Collection<User> users, Actor actor, List<Role> roles);

	User generateUser(UserCreationDTO userCreationDTO);

	void updateUser(User user, UserUpdateDTO userUpdateDTO);
}
