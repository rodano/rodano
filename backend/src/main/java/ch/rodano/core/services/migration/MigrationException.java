package ch.rodano.core.services.migration;

/**
 * Thrown when a database migration fails.
 * As migrations are run during application startup, this exception aborts the startup to avoid running on a partially migrated database.
 */
public class MigrationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public MigrationException(final String message) {
		super(message);
	}

	public MigrationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
