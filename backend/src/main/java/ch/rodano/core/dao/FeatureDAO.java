package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.feature.Feature;
import ch.rodano.core.model.jooq.tables.records.FeatureRecord;

import static ch.rodano.core.model.jooq.tables.Feature.FEATURE;

@Repository
public class FeatureDAO implements BaseProjectDAO<Feature> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;

	public FeatureDAO(final DSLContext dslContext, final MappingHelper mappingHelper) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
	}

	@Override
	public List<Feature> findByProject(final UUID projectId) {
		return dslContext.selectFrom(FEATURE)
			.where(FEATURE.PROJECT_ID.eq(projectId))
			.fetch(this::mapToModel);
	}

	@Override
	public Feature findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(FEATURE)
			.where(FEATURE.PROJECT_ID.eq(projectId))
			.and(FEATURE.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public Feature findById(final UUID id) {
		return dslContext.selectFrom(FEATURE)
			.where(FEATURE.FEATURE_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public Feature save(final Feature entity) {
		return null;
	}

	@Override
	public void delete(final UUID id) {

	}

	private Feature mapToModel(final FeatureRecord record) {
		if(record == null) {
			return null;
		}

		final Feature model = new Feature();

		model.setId(record.getCode());
		model.setFeatureId(record.getFeatureId());
		model.setOptional(record.getOptional() != null ? record.getOptional() : false);

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

		return model;
	}
}
