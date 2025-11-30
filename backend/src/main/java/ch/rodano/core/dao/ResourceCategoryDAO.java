package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.resource.ResourceCategory;
import ch.rodano.core.model.jooq.tables.records.ResourceCategoryRecord;

import static ch.rodano.core.model.jooq.tables.ResourceCategory.RESOURCE_CATEGORY;

@Repository
public class ResourceCategoryDAO implements BaseProjectDAO<ResourceCategory> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;

	public ResourceCategoryDAO(final DSLContext dslContext, final MappingHelper mappingHelper) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
	}

	@Override
	public List<ResourceCategory> findByProject(final UUID projectId) {
		return dslContext.selectFrom(RESOURCE_CATEGORY)
			.where(RESOURCE_CATEGORY.PROJECT_ID.eq(projectId))
			.fetch(this::mapToModel);
	}

	@Override
	public ResourceCategory findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(RESOURCE_CATEGORY)
			.where(RESOURCE_CATEGORY.PROJECT_ID.eq(projectId))
			.and(RESOURCE_CATEGORY.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public ResourceCategory findById(final UUID id) {
		return dslContext.selectFrom(RESOURCE_CATEGORY)
			.where(RESOURCE_CATEGORY.CATEGORY_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public ResourceCategory save(final ResourceCategory entity) {
		return null;
	}

	@Override
	public void delete(final UUID id) {

	}

	private ResourceCategory mapToModel(final ResourceCategoryRecord record) {
		if(record == null) {
			return null;
		}

		final ResourceCategory model = new ResourceCategory();

		model.setResourceCategoryId(record.getCategoryId());
		model.setId(record.getCode());

		model.setIcon(record.getIcon());

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

		return model;
	}
}
