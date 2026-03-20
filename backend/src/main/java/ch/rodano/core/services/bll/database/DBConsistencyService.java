package ch.rodano.core.services.bll.database;

import java.util.List;

public interface DBConsistencyService {
	/**
	 * Check the consistency of the database with the study configuration.
	 *
	 * @return a list of inconsistency messages; empty if the database is consistent
	 */
	List<String> checkConsistency();
}
