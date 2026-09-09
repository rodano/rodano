package ch.rodano.core.services.migration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;

import ch.rodano.core.database.migrations.AbstractDatabaseMigration;

@Service
@Profile("api")
public class MigrationServiceImpl implements MigrationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MigrationServiceImpl.class);

	private static final String SQL_MIGRATIONS_LOCATION = "classpath:/database_scripts/migrations/db_update_*.sql";
	private static final Pattern SQL_MIGRATION_NUMBER = Pattern.compile("db_update_(\\d+)\\.sql");

	private final DSLContext create;
	private final DataSource dataSource;
	private final String internalPatchTable;

	/**
	 * All the Java migration beans present in the context.
	 * May be empty when no Java migration is registered.
	 */
	private final List<AbstractDatabaseMigration> javaMigrations;

	public MigrationServiceImpl(
		final DSLContext create,
		final DataSource dataSource,
		final List<AbstractDatabaseMigration> javaMigrations,
		@Value("${rodano.migration.internal-patch-table}") final String internalPatchTable
	) {
		this.create = create;
		this.dataSource = dataSource;
		this.javaMigrations = javaMigrations;
		this.internalPatchTable = internalPatchTable;
	}

	@Override
	public void migrateToLatest() {
		final var currentVersion = getCurrentVersion();
		LOGGER.info("Current database version is {}", currentVersion);

		// Gather all migrations (SQL and Java) that are more recent than the current version, sorted by ascending number
		final var pendingMigrations = new TreeMap<Double, Migration>();

		for(final var sqlMigration : discoverSqlMigrations()) {
			if(sqlMigration.number() > currentVersion) {
				pendingMigrations.put(sqlMigration.number(), sqlMigration);
			}
		}

		for(final var javaMigration : javaMigrations) {
			final var number = javaMigration.migrationTaskNumber();
			if(number > currentVersion) {
				pendingMigrations.put(number, new JavaMigration(javaMigration));
			}
		}

		if(pendingMigrations.isEmpty()) {
			LOGGER.info("Database is already up to date");
			return;
		}

		LOGGER.info("Applying {} pending migration(s): {}", pendingMigrations.size(), pendingMigrations.keySet());
		for(final var migration : pendingMigrations.values()) {
			LOGGER.info("Running migration {}", migration.name());
			migration.run();
			LOGGER.info("Migration {} applied", migration.name());
		}
		LOGGER.info("Database migrated to version {}", pendingMigrations.lastKey());
	}

	/**
	 * Get the highest version recorded in the internal patch table.
	 *
	 * @return The current database version, or 0 if no migration has been applied yet
	 */
	private double getCurrentVersion() {
		return Optional.ofNullable(
			create.select(DSL.max(DSL.field("script", Double.class)))
				.from(DSL.table(DSL.name(internalPatchTable)))
				.fetchOne(0, Double.class)
		).orElse(0D);
	}

	private List<SqlMigration> discoverSqlMigrations() {
		final var resolver = new PathMatchingResourcePatternResolver();
		try {
			final var resources = resolver.getResources(SQL_MIGRATIONS_LOCATION);
			final var migrations = new ArrayList<SqlMigration>();
			for(final var resource : resources) {
				final var filename = resource.getFilename();
				if(filename == null) {
					continue;
				}
				final var matcher = SQL_MIGRATION_NUMBER.matcher(filename);
				if(matcher.matches()) {
					migrations.add(new SqlMigration(Double.parseDouble(matcher.group(1)), resource));
				}
			}
			return migrations;
		}
		catch(final IOException e) {
			throw new MigrationException("Unable to list SQL migration scripts", e);
		}
	}

	/**
	 * A migration that can be applied to the database.
	 */
	private interface Migration {
		void run();

		String name();
	}

	/**
	 * A SQL migration script. The script is responsible for inserting its own row into the internal patch table.
	 */
	private final class SqlMigration implements Migration {
		private final double number;
		private final Resource resource;

		private SqlMigration(final double number, final Resource resource) {
			this.number = number;
			this.resource = resource;
		}

		private double number() {
			return number;
		}

		@Override
		public void run() {
			final var populator = new ResourceDatabasePopulator(resource);
			try {
				populator.execute(dataSource);
			}
			catch(final RuntimeException e) {
				throw new MigrationException("SQL migration " + name() + " failed", e);
			}
		}

		@Override
		public String name() {
			return resource.getFilename();
		}
	}

	/**
	 * A Java migration bean. The bean is responsible for recording its version in the internal patch table.
	 */
	private static final class JavaMigration implements Migration {
		private final AbstractDatabaseMigration migration;

		private JavaMigration(final AbstractDatabaseMigration migration) {
			this.migration = migration;
		}

		@Override
		public void run() {
			if(!migration.runMigration()) {
				throw new MigrationException("Java migration " + name() + " failed");
			}
		}

		@Override
		public String name() {
			return migration.getClass().getSimpleName();
		}
	}
}
