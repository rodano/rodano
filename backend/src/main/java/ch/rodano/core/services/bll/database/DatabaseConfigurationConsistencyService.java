package ch.rodano.core.services.bll.database;

import java.util.List;

import ch.rodano.core.model.audit.DatabaseActionContext;

public interface DatabaseConfigurationConsistencyService {

	/**
	 * Detect and optionally fix inconsistencies between the database and the current configuration
	 *
	 * @param dryRun    When true, only detects issues without fixing them
	 * @param context   Action context for auditing
	 * @param rationale Rationale for the operation
	 * @return a list of grouped database issues; empty if the database is consistent
	 */
	List<ConfigurationInconsistencyGroup> fixInconsistencies(boolean dryRun, DatabaseActionContext context, String rationale);
}
