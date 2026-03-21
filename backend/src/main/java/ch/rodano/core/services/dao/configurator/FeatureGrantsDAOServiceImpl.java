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

import static ch.rodano.core.model.jooq.tables.ProfileFeatureGrants.PROFILE_FEATURE_GRANTS;

@Repository
public class FeatureGrantsDAOServiceImpl implements FeatureGrantsDAOService {

	private final DSLContext dslContext;

	public FeatureGrantsDAOServiceImpl(final DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "featureGrants", key = "#projectId.toString()")
	public Map<UUID, List<UUID>> getFeatureGrants(final UUID projectId) {
		final var rows = dslContext
			.select(
				PROFILE_FEATURE_GRANTS.PROFILE_ID,
				PROFILE_FEATURE_GRANTS.FEATURE_ID
			)
			.from(PROFILE_FEATURE_GRANTS)
			.where(PROFILE_FEATURE_GRANTS.PROJECT_ID.eq(projectId))
			.fetch();

		final Map<UUID, List<UUID>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2());
		}
		return result;
	}

	@Override
	@Transactional
	@CacheEvict(value = "featureGrants", key = "#projectId.toString()")
	public void saveFeatureGrants(final UUID projectId, final Map<UUID, List<UUID>> profileFeatureMap) {
		dslContext.deleteFrom(PROFILE_FEATURE_GRANTS)
			.where(PROFILE_FEATURE_GRANTS.PROJECT_ID.eq(projectId))
			.execute();

		for(final var entry : profileFeatureMap.entrySet()) {
			final var profileId = entry.getKey();
			for(final var featureId : entry.getValue()) {
				dslContext.insertInto(PROFILE_FEATURE_GRANTS)
					.set(PROFILE_FEATURE_GRANTS.PROJECT_ID, projectId)
					.set(PROFILE_FEATURE_GRANTS.PROFILE_ID, profileId)
					.set(PROFILE_FEATURE_GRANTS.FEATURE_ID, featureId)
					.execute();
			}
		}
	}
}
