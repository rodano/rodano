package ch.rodano.core.services.migration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import ch.rodano.core.database.migrations.AbstractDatabaseMigration;

@Service
@Profile("migration")
public class MigrationServiceImpl implements MigrationService {
	private final ApplicationContext context;

	private final String script;

	public MigrationServiceImpl(
		final ApplicationContext context,
		@Value("${rodano.migration.script}") final String script
	) {
		this.context = context;
		this.script = script;
	}

	/**
	 * Run the migration which will be read from a property
	 *
	 * @return True if the migration is a success and false if it is a failure
	 */
	@Override
	public Boolean run() {
		if("DBConsistencyCheck".equals(script)) {
			final var dbConsistencyCheck = (AbstractDatabaseMigration) context.getBean(script);
			return dbConsistencyCheck.runConsistencyCheck();
		}
		final var migration = (AbstractDatabaseMigration) context.getBean(script);
		return migration.runMigration();
	}
}
