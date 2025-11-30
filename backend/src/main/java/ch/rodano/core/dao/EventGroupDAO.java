package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.event.EventGroup;
import ch.rodano.core.model.jooq.tables.records.EventGroupRecord;

import static ch.rodano.core.model.jooq.tables.EventGroup.EVENT_GROUP;

@Repository
public class EventGroupDAO implements BaseProjectDAO<EventGroup> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;

	public EventGroupDAO(final DSLContext dslContext, final MappingHelper mappingHelper) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
	}

	@Override
	public List<EventGroup> findByProject(final UUID projectId) {
		return dslContext.selectFrom(EVENT_GROUP)
			.where(EVENT_GROUP.PROJECT_ID.eq(projectId))
			.fetch(this::mapToModel);
	}

	@Override
	public EventGroup findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(EVENT_GROUP)
			.where(EVENT_GROUP.PROJECT_ID.eq(projectId))
			.and(EVENT_GROUP.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public EventGroup findById(final UUID id) {
		return dslContext.selectFrom(EVENT_GROUP)
			.where(EVENT_GROUP.EVENT_GROUP_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public EventGroup save(final EventGroup entity) {
		return null;
	}

	@Override
	public void delete(final UUID id) {
	}

	public List<EventGroup> findByScopeModel(final UUID scopeModelId) {
		return dslContext.selectFrom(EVENT_GROUP)
			.where(EVENT_GROUP.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetch(this::mapToModel);
	}

	private EventGroup mapToModel(final EventGroupRecord record) {
		if(record == null) {
			return null;
		}

		final EventGroup model = new EventGroup();

		model.setEventGroupId(record.getEventGroupId());
		model.setId(record.getCode());

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

		return model;
	}
}
