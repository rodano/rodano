package ch.rodano.core.plugins.dashboard;

import java.util.List;

import org.jooq.DSLContext;

import ch.rodano.core.model.user.User;

public interface DashboardPlugin {
	/**
	 * Get the general information of the dashboard
	 *
	 * @param user       A user
	 * @return A list of information
	 */
	List<DashboardData> getGeneralInformation(DSLContext create, final User user);
}
