package ch.rodano.core.services.migration;

public interface MigrationService {
	/**
	 * Run all pending migrations (SQL and Java) in ascending order until the database reaches the latest version.
	 * A migration is considered pending when its number is greater than the highest version recorded in the internal patch table.
	 *
	 * @throws MigrationException if a migration fails, so that the application startup is aborted
	 */
	void migrateToLatest();
}
