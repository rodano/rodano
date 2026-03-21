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

import static ch.rodano.core.model.jooq.tables.ProfileMenuGrants.PROFILE_MENU_GRANTS;

@Repository
public class MenuGrantsConfigDAOServiceImpl implements MenuGrantsConfigDAOService {

	private final DSLContext dslContext;

	public MenuGrantsConfigDAOServiceImpl(final DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "menuGrants", key = "#projectId.toString()")
	public Map<UUID, List<UUID>> getMenuGrants(final UUID projectId) {
		final var rows = dslContext
			.select(
				PROFILE_MENU_GRANTS.PROFILE_ID,
				PROFILE_MENU_GRANTS.MENU_ID
			)
			.from(PROFILE_MENU_GRANTS)
			.where(PROFILE_MENU_GRANTS.PROJECT_ID.eq(projectId))
			.fetch();

		final Map<UUID, List<UUID>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2());
		}
		return result;
	}

	@Override
	@Transactional
	@CacheEvict(value = "menuGrants", key = "#projectId.toString()")
	public void saveMenuGrants(final UUID projectId, final Map<UUID, List<UUID>> profileMenuMap) {
		dslContext.deleteFrom(PROFILE_MENU_GRANTS)
			.where(PROFILE_MENU_GRANTS.PROJECT_ID.eq(projectId))
			.execute();

		for(final var entry : profileMenuMap.entrySet()) {
			final var profileId = entry.getKey();
			for(final var menuId : entry.getValue()) {
				dslContext.insertInto(PROFILE_MENU_GRANTS)
					.set(PROFILE_MENU_GRANTS.PROJECT_ID, projectId)
					.set(PROFILE_MENU_GRANTS.PROFILE_ID, profileId)
					.set(PROFILE_MENU_GRANTS.MENU_ID, menuId)
					.execute();
			}
		}
	}
}
