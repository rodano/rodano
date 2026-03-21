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

import static ch.rodano.core.model.jooq.tables.ProfileCategoryGrants.PROFILE_CATEGORY_GRANTS;

@Repository
public class ResourceCategoryGrantsDAOServiceImpl implements ResourceCategoryGrantsDAOService {

	private final DSLContext dslContext;

	public ResourceCategoryGrantsDAOServiceImpl(final DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "resourceCategoryGrants", key = "#projectId.toString()")
	public Map<UUID, List<UUID>> getResourceCategoryGrants(final UUID projectId) {
		final var rows = dslContext
			.select(
				PROFILE_CATEGORY_GRANTS.PROFILE_ID,
				PROFILE_CATEGORY_GRANTS.CATEGORY_ID
			)
			.from(PROFILE_CATEGORY_GRANTS)
			.where(PROFILE_CATEGORY_GRANTS.PROJECT_ID.eq(projectId))
			.fetch();

		final Map<UUID, List<UUID>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2());
		}
		return result;
	}

	@Override
	@Transactional
	@CacheEvict(value = "resourceCategoryGrants", key = "#projectId.toString()")
	public void saveResourceCategoryGrants(final UUID projectId, final Map<UUID, List<UUID>> profileResourceCategoryMap) {
		dslContext.deleteFrom(PROFILE_CATEGORY_GRANTS)
			.where(PROFILE_CATEGORY_GRANTS.PROJECT_ID.eq(projectId))
			.execute();

		for(final var entry : profileResourceCategoryMap.entrySet()) {
			final var profileId = entry.getKey();
			for(final var categoryId : entry.getValue()) {
				dslContext.insertInto(PROFILE_CATEGORY_GRANTS)
					.set(PROFILE_CATEGORY_GRANTS.PROJECT_ID, projectId)
					.set(PROFILE_CATEGORY_GRANTS.PROFILE_ID, profileId)
					.set(PROFILE_CATEGORY_GRANTS.CATEGORY_ID, categoryId)
					.execute();
			}
		}
	}
}
