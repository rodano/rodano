package ch.rodano.core.services.dao.configurator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static ch.rodano.core.model.jooq.tables.ProfileReportGrants.PROFILE_REPORT_GRANTS;

@Repository
public class ReportGrantsDAOServiceImpl implements ReportGrantsDAOService {

	private final DSLContext dslContext;

	public ReportGrantsDAOServiceImpl(final DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "reportGrants", key = "#projectId.toString()")
	public Map<UUID, List<UUID>> getReportGrants(final UUID projectId) {
		final var rows = dslContext
			.select(
				PROFILE_REPORT_GRANTS.PROFILE_ID,
				PROFILE_REPORT_GRANTS.REPORT_ID
			)
			.from(PROFILE_REPORT_GRANTS)
			.where(PROFILE_REPORT_GRANTS.PROJECT_ID.eq(projectId))
			.fetch();

		final Map<UUID, List<UUID>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2());
		}
		return result;
	}

	@Override
	@Transactional
	@CacheEvict(value = "reportGrants", key = "#projectId.toString()")
	public void saveReportGrants(final UUID projectId, final Map<UUID, List<UUID>> profileReportMap) {
		dslContext.deleteFrom(PROFILE_REPORT_GRANTS)
			.where(PROFILE_REPORT_GRANTS.PROJECT_ID.eq(projectId))
			.execute();

		for(final var entry : profileReportMap.entrySet()) {
			final var profileId = entry.getKey();
			for(final var reportId : entry.getValue()) {
				dslContext.insertInto(PROFILE_REPORT_GRANTS)
					.set(PROFILE_REPORT_GRANTS.PROJECT_ID, projectId)
					.set(PROFILE_REPORT_GRANTS.PROFILE_ID, profileId)
					.set(PROFILE_REPORT_GRANTS.REPORT_ID, reportId)
					.execute();
			}
		}
	}
}
