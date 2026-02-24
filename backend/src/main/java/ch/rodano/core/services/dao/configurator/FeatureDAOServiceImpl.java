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

import ch.rodano.api.config.FeatureDTO;
import ch.rodano.core.model.jooq.tables.records.FeatureRecord;

import static ch.rodano.core.model.jooq.tables.Feature.FEATURE;


@Repository
public class FeatureDAOServiceImpl implements FeatureDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public FeatureDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "features", key = "#projectId.toString()")
	public List<FeatureDTO> getFeatures(final UUID projectId) {
		final var features = dslContext.selectFrom(FEATURE)
			.where(FEATURE.PROJECT_ID.eq(projectId))
			.orderBy(FEATURE.CODE)
			.fetch();

		return features.stream()
			.map(this::mapToDTO)
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "feature", key = "#projectId.toString() + ':' + #featureId.toString()")
	public FeatureDTO getFeature(final UUID projectId, final UUID featureId) {
		final var record = dslContext.selectFrom(FEATURE)
			.where(FEATURE.PROJECT_ID.eq(projectId))
			.and(FEATURE.FEATURE_ID.eq(featureId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		return mapToDTO(record);
	}

	@Override
	@Transactional
	@CacheEvict(value = "features", key = "#projectId.toString()")
	public FeatureDTO createFeature(final UUID projectId, final FeatureDTO dto) {
		final var featureId = dto.featureId() != null ? dto.featureId() : UUID.randomUUID();

		dslContext.insertInto(FEATURE)
			.set(FEATURE.FEATURE_ID, featureId)
			.set(FEATURE.PROJECT_ID, projectId)
			.set(FEATURE.CODE, dto.id())
			.set(FEATURE.SHORTNAME, jsonMapperService.toJson(dto.shortname()))
			.set(FEATURE.LONGNAME, jsonMapperService.toJson(dto.longname()))
			.set(FEATURE.DESCRIPTION, jsonMapperService.toJson(dto.description()))
			.set(FEATURE.OPTIONAL, dto.optional() != null ? dto.optional() : Boolean.FALSE)
			.execute();

		return getFeature(projectId, featureId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "features", key = "#projectId.toString()"),
		@CacheEvict(value = "feature", key = "#projectId.toString() + ':' + #featureId.toString()")
	})
	public FeatureDTO updateFeature(final UUID projectId, final UUID featureId, final FeatureDTO dto) {
		dslContext.update(FEATURE)
			.set(FEATURE.CODE, dto.id())
			.set(FEATURE.SHORTNAME, jsonMapperService.toJson(dto.shortname()))
			.set(FEATURE.LONGNAME, jsonMapperService.toJson(dto.longname()))
			.set(FEATURE.DESCRIPTION, jsonMapperService.toJson(dto.description()))
			.set(FEATURE.OPTIONAL, dto.optional() != null ? dto.optional() : Boolean.FALSE)
			.where(FEATURE.PROJECT_ID.eq(projectId))
			.and(FEATURE.FEATURE_ID.eq(featureId))
			.execute();

		return getFeature(projectId, featureId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "features", key = "#projectId.toString()"),
		@CacheEvict(value = "feature", key = "#projectId.toString() + ':' + #featureId.toString()")
	})
	public void deleteFeature(final UUID projectId, final UUID featureId) {
		dslContext.deleteFrom(FEATURE)
			.where(FEATURE.PROJECT_ID.eq(projectId))
			.and(FEATURE.FEATURE_ID.eq(featureId))
			.execute();
	}

	private FeatureDTO mapToDTO(final FeatureRecord record) {
		return new FeatureDTO(
			record.getFeatureId(),
			record.getCode(),
			jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
			}),
			jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
			}),
			jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
			}),
			record.getOptional()
		);
	}
}
