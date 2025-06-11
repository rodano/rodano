package ch.rodano.core.services.migration;

public interface MigrationService {
	/**
	 * Run the migration which will be read from a property
	 *
	 * @return True if the migration is a success and false if it is a failure
	 */
	Boolean run();
}
