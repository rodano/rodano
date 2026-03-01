package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.config.ResourceCategoryDTO;
import ch.rodano.core.model.jooq.tables.records.ResourceCategoryRecord;

import static ch.rodano.core.model.jooq.tables.ResourceCategory.RESOURCE_CATEGORY;

@Repository
public class ResourceCategoryDAOServiceImpl implements ResourceCategoryDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public ResourceCategoryDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "resource-categories", key = "#projectId.toString()")
	public List<ResourceCategoryDTO> getResourceCategories(final UUID projectId) {
		final var resourceCategories = dslContext.selectFrom(RESOURCE_CATEGORY)
			.where(RESOURCE_CATEGORY.PROJECT_ID.eq(projectId))
			.orderBy(RESOURCE_CATEGORY.CODE)
			.fetch();

		return resourceCategories.stream()
			.map(this::mapToDTO)
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "resource-category", key = "#projectId.toString() + ':' + #resourceCategoryId.toString()")
	public ResourceCategoryDTO getResourceCategory(final UUID projectId, final UUID resourceCategoryId) {
		final var record = dslContext.selectFrom(RESOURCE_CATEGORY)
			.where(RESOURCE_CATEGORY.PROJECT_ID.eq(projectId))
			.and(RESOURCE_CATEGORY.CATEGORY_ID.eq(resourceCategoryId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		return mapToDTO(record);
	}

	@Override
	@Transactional
	@CacheEvict(value = "resource-categories", key = "#projectId.toString()")
	public ResourceCategoryDTO createResourceCategory(final UUID projectId, final ResourceCategoryDTO dto) {
		final var categoryId = dto.getCategoryId() != null ? dto.getCategoryId() : UUID.randomUUID();

		dslContext.insertInto(RESOURCE_CATEGORY)
			.set(RESOURCE_CATEGORY.CATEGORY_ID, categoryId)
			.set(RESOURCE_CATEGORY.PROJECT_ID, projectId)
			.set(RESOURCE_CATEGORY.CODE, dto.getId())
			.set(RESOURCE_CATEGORY.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(RESOURCE_CATEGORY.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(RESOURCE_CATEGORY.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(RESOURCE_CATEGORY.ICON, dto.getIcon())
			.execute();

		return getResourceCategory(projectId, categoryId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "resource-categories", key = "#projectId.toString()"),
		@CacheEvict(value = "resource-category", key = "#projectId.toString() + ':' + #resourceCategoryId.toString()")
	})
	public ResourceCategoryDTO updateResourceCategory(final UUID projectId, final UUID resourceCategoryId, final ResourceCategoryDTO dto) {
		dslContext.update(RESOURCE_CATEGORY)
			.set(RESOURCE_CATEGORY.CODE, dto.getId())
			.set(RESOURCE_CATEGORY.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(RESOURCE_CATEGORY.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(RESOURCE_CATEGORY.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(RESOURCE_CATEGORY.ICON, dto.getIcon())
			.where(RESOURCE_CATEGORY.PROJECT_ID.eq(projectId))
			.and(RESOURCE_CATEGORY.CATEGORY_ID.eq(resourceCategoryId))
			.execute();

		return getResourceCategory(projectId, resourceCategoryId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "resource-categories", key = "#projectId.toString()"),
		@CacheEvict(value = "resource-category", key = "#projectId.toString() + ':' + #resourceCategoryId.toString()")
	})
	public void deleteResourceCategory(final UUID projectId, final UUID resourceCategoryId) {
		dslContext.deleteFrom(RESOURCE_CATEGORY)
			.where(RESOURCE_CATEGORY.PROJECT_ID.eq(projectId))
			.and(RESOURCE_CATEGORY.CATEGORY_ID.eq(resourceCategoryId))
			.execute();
	}

	private ResourceCategoryDTO mapToDTO(final ResourceCategoryRecord record) {
		final var dto = new ResourceCategoryDTO();

		dto.setCategoryId(record.getCategoryId());
		dto.setId(record.getCode());
		dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setLongname(jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setIcon(record.getIcon());

		return dto;
	}
}
