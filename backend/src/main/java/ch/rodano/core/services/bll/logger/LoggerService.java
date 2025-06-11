package ch.rodano.core.services.bll.logger;

import org.springframework.boot.logging.LogLevel;

import ch.rodano.core.model.robot.Robot;

public interface LoggerService {
	/**
	 * Log the given message using logger
	 *
	 * @param robot The robot logging the message
	 * @param log   The message to log
	 * @param level The level of the log
	 */
	void log(Robot robot, String log, LogLevel level);
}
