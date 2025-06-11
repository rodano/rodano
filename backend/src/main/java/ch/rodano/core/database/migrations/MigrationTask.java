package ch.rodano.core.database.migrations;

public interface MigrationTask {
	/**
	 * Execute a migration task
	 *
	 * @return True if the task has been correctly executed
	 */
	boolean run();

	/**
	 * The task description
	 *
	 * @return The task description
	 */
	String description();
}
