package ch.rodano.core.services.bll.logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.robot.Robot;

@Service
public class LoggerServiceImpl implements LoggerService {
	@Value("${rodano.robot.logger.id}")
	private String robotLoggerId;

	/**
	 * Log the given message using logger
	 *
	 * @param robot The robot logging the message
	 * @param log   The message to log
	 * @param level The level of the log
	 */
	@Override
	public void log(final Robot robot, final String log, final LogLevel level) {
		final var logger = LoggerFactory.getLogger(robotLoggerId + robot.getName());
		switch(level) {
			case ERROR, FATAL -> logger.error(log);
			case WARN -> logger.warn(log);
			case INFO -> logger.info(log);
			case DEBUG -> logger.debug(log);
			default -> logger.trace(log);
		}
	}
}
