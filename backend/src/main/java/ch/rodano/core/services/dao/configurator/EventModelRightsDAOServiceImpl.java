package ch.rodano.core.services.dao.configurator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.EntityRightDTO;

import static ch.rodano.core.model.jooq.tables.ProfileEventModelRights.PROFILE_EVENT_MODEL_RIGHTS;


@Repository
public class EventModelRightsDAOServiceImpl implements EventModelRightsDAOService {

	private final DSLContext dslContext;

	public EventModelRightsDAOServiceImpl(final DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "eventModelRights", key = "#projectId.toString()")
	public Map<UUID, Map<UUID, EntityRightDTO>> getEventModelRights(final UUID projectId) {
		final var rows = dslContext
			.selectFrom(PROFILE_EVENT_MODEL_RIGHTS)
			.where(PROFILE_EVENT_MODEL_RIGHTS.PROJECT_ID.eq(projectId))
			.fetch();

		final Map<UUID, Map<UUID, EntityRightDTO>> result = new HashMap<>();
		for(final var row : rows) {
			result
				.computeIfAbsent(row.getProfileId(), _ -> new HashMap<>())
				.put(row.getEventModelId(), new EntityRightDTO(row.getCanRead(), row.getCanWrite()));
		}
		return result;
	}

	@Override
	@Transactional
	@CacheEvict(value = "eventModelRights", key = "#projectId.toString()")
	public void saveEventModelRights(final UUID projectId, final Map<UUID, Map<UUID, EntityRightDTO>> rights) {
		dslContext.deleteFrom(PROFILE_EVENT_MODEL_RIGHTS)
			.where(PROFILE_EVENT_MODEL_RIGHTS.PROJECT_ID.eq(projectId))
			.execute();

		for(final var profileEntry : rights.entrySet()) {
			final var profileId = profileEntry.getKey();
			for(final var entityEntry : profileEntry.getValue().entrySet()) {
				final var right = entityEntry.getValue();
				if(!right.canRead() && !right.canWrite()) {
					continue;
				}
				dslContext.insertInto(PROFILE_EVENT_MODEL_RIGHTS)
					.set(PROFILE_EVENT_MODEL_RIGHTS.PROJECT_ID, projectId)
					.set(PROFILE_EVENT_MODEL_RIGHTS.PROFILE_ID, profileId)
					.set(PROFILE_EVENT_MODEL_RIGHTS.EVENT_MODEL_ID, entityEntry.getKey())
					.set(PROFILE_EVENT_MODEL_RIGHTS.CAN_READ, right.canRead())
					.set(PROFILE_EVENT_MODEL_RIGHTS.CAN_WRITE, right.canWrite())
					.execute();
			}
		}
	}
}
