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

import static ch.rodano.core.model.jooq.tables.ProfileTimelineGraphGrants.PROFILE_TIMELINE_GRAPH_GRANTS;

@Repository
public class TimelineGraphGrantsDAOServiceImpl implements TimelineGraphGrantsDAOService {

	private final DSLContext dslContext;

	public TimelineGraphGrantsDAOServiceImpl(final DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timelineGraphGrants", key = "#projectId.toString()")
	public Map<UUID, List<UUID>> getTimelineGraphGrants(final UUID projectId) {
		final var rows = dslContext
			.select(
				PROFILE_TIMELINE_GRAPH_GRANTS.PROFILE_ID,
				PROFILE_TIMELINE_GRAPH_GRANTS.TIMELINE_GRAPH_ID
			)
			.from(PROFILE_TIMELINE_GRAPH_GRANTS)
			.where(PROFILE_TIMELINE_GRAPH_GRANTS.PROJECT_ID.eq(projectId))
			.fetch();

		final Map<UUID, List<UUID>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2());
		}
		return result;
	}

	@Override
	@Transactional
	@CacheEvict(value = "timelineGraphGrants", key = "#projectId.toString()")
	public void saveTimelineGraphGrants(final UUID projectId, final Map<UUID, List<UUID>> profileTimelineGraphMap) {
		dslContext.deleteFrom(PROFILE_TIMELINE_GRAPH_GRANTS)
			.where(PROFILE_TIMELINE_GRAPH_GRANTS.PROJECT_ID.eq(projectId))
			.execute();

		for(final var entry : profileTimelineGraphMap.entrySet()) {
			final var profileId = entry.getKey();
			for(final var timelineGraphId : entry.getValue()) {
				dslContext.insertInto(PROFILE_TIMELINE_GRAPH_GRANTS)
					.set(PROFILE_TIMELINE_GRAPH_GRANTS.PROJECT_ID, projectId)
					.set(PROFILE_TIMELINE_GRAPH_GRANTS.PROFILE_ID, profileId)
					.set(PROFILE_TIMELINE_GRAPH_GRANTS.TIMELINE_GRAPH_ID, timelineGraphId)
					.execute();
			}
		}
	}
}
