package ch.rodano.core.database.migrations;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

public abstract class AbstractDatabaseMigration {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${rodano.migration.internal-patch-table}")
	protected String internalPatchTable;

	@Autowired
	private DSLContext create;

	/**
	 * Execute the database update
	 *
	 * @return True if everything went well and false otherwise
	 */
	@Transactional
	public boolean runMigration() {
		final var watch = new StopWatch();
		watch.start();

		// Init
		logger.info("Init the database update");

		// Check if the database update has already been run
		if(checkDatabaseVersion()) {
			logger.error("The database migration " + migrationTaskNumber() + " has already been run");
			return false;
		}

		// Execute
		logger.info("Start migration");
		final var success = runMigrationTasks();

		// Save the database update into the internal patches table
		updateDatabaseVersionTable();

		logger.info("Database migration " + migrationTaskNumber() + " has been executed");

		watch.stop();
		logger.info("Migration took " + watch.getTotalTimeSeconds() + " seconds (" + TimeUnit.MILLISECONDS.toMinutes(watch.getTotalTimeMillis()) + " minutes)");

		return success;
	}

	/**
	 * Create all tasks
	 *
	 * @return True if everything went well and false otherwise
	 */
	protected abstract List<MigrationTask> tasks();

	/**
	 * Get the database update number
	 *
	 * @return The database update number
	 */
	protected abstract Double migrationTaskNumber();

	/**
	 * Get the execution context of the database update
	 *
	 * @return The context of the database update
	 */
	protected abstract String context();

	/**
	 * Get the name of the database update
	 *
	 * @return The name of the database update
	 */
	protected String javaFileName() {
		return this.getClass().getSimpleName() + ".java";
	}

	/**
	 * Execute all tasks
	 *
	 * @return True if all tasks executed correctly and false otherwise
	 */
	public boolean runMigrationTasks() {
		final var tasks = tasks();
		logger.info("Executing " + tasks.size() + " task" + (tasks.size() > 1 ? "s" : ""));

		for(final var task : tasks) {
			logger.info("Running task : " + task.description());

			final var taskResult = task.run();

			if(!taskResult) {
				logger.error("Failed task : " + task.description());
				return false;
			}
		}

		logger.info("All tasks have been executed correctly");
		return true;
	}

	/**
	 * Check the patch state into the internal patch table
	 *
	 * @return True if the patch can be run and false otherwise (error or already within the database)
	 */
	private boolean checkDatabaseVersion() {
		logger.info("Checking the database version...");

		final var query = "select script from " + internalPatchTable + " where script = " + migrationTaskNumber();

		final var jooqQuery = create.resultQuery(query);
		final var result = jooqQuery.fetch();
		return result.size() == 1;
	}

	/**
	 * Update the database version table
	 */
	public void updateDatabaseVersionTable() {
		logger.info("Save the script into the internal patch table");

		final var query = create.insertInto(DSL.table(internalPatchTable))
			.columns(DSL.field("script"), DSL.field("date"), DSL.field("context"), DSL.field("name"))
			.values(migrationTaskNumber(), ZonedDateTime.now(), context(), javaFileName());

		query.execute();
	}

	@Transactional
	public boolean runConsistencyCheck() {
		return runMigrationTasks();
	}
}
