package ch.rodano.core.services.bll.study;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.study.Study;

import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModel.EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.FieldModel.FIELD_MODEL;
import static ch.rodano.core.model.jooq.tables.FormModel.FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;

@Service
public class ModelCatalogSyncService {

	private final DSLContext create;

	public ModelCatalogSyncService(final DSLContext create) {
		this.create = create;
	}

	public void syncModelUuidsFromDatabase(final Study study) {
		final var projectId = study.getProjectId();

		final var scopeModelUuids = create.select(SCOPE_MODEL.CODE, SCOPE_MODEL.SCOPE_MODEL_ID)
			.from(SCOPE_MODEL)
			.where(SCOPE_MODEL.PROJECT_ID.eq(projectId))
			.fetchMap(SCOPE_MODEL.CODE, SCOPE_MODEL.SCOPE_MODEL_ID);

		study.getScopeModels().forEach(sm -> {
			final var uuid = scopeModelUuids.get(sm.getId());
			if(uuid != null) {
				sm.setScopeModelId(uuid);
			}
		});

		final var datasetModelUuids = create.select(DATASET_MODEL.CODE, DATASET_MODEL.DATASET_MODEL_ID)
			.from(DATASET_MODEL)
			.where(DATASET_MODEL.PROJECT_ID.eq(projectId))
			.fetchMap(DATASET_MODEL.CODE, DATASET_MODEL.DATASET_MODEL_ID);

		study.getDatasetModels().forEach(dm -> {
			final var uuid = datasetModelUuids.get(dm.getId());
			if(uuid != null) {
				dm.setDatasetModelId(uuid);
			}
		});

		final var eventModelUuids = create.select(EVENT_MODEL.CODE, EVENT_MODEL.EVENT_MODEL_ID)
			.from(EVENT_MODEL)
			.where(EVENT_MODEL.PROJECT_ID.eq(projectId))
			.fetchMap(EVENT_MODEL.CODE, EVENT_MODEL.EVENT_MODEL_ID);

		study.getEventModels().forEach(em -> {
			final var uuid = eventModelUuids.get(em.getId());
			if(uuid != null) {
				em.setEventModelId(uuid);
			}
		});

		final var formModelUuids = create.select(FORM_MODEL.CODE, FORM_MODEL.FORM_MODEL_ID)
			.from(FORM_MODEL)
			.where(FORM_MODEL.PROJECT_ID.eq(projectId))
			.fetchMap(FORM_MODEL.CODE, FORM_MODEL.FORM_MODEL_ID);

		study.getFormModels().forEach(fm -> {
			final var uuid = formModelUuids.get(fm.getId());
			if(uuid != null) {
				fm.setFormModelId(uuid);
			}
		});

		final var fieldModelRecords = create.select(
				FIELD_MODEL.DATASET_MODEL_ID,
				FIELD_MODEL.CODE,
				FIELD_MODEL.FIELD_MODEL_ID
			)
			.from(FIELD_MODEL)
			.where(FIELD_MODEL.PROJECT_ID.eq(projectId))
			.fetch();

		study.getFieldModels().forEach(fm -> {
			final var datasetModelId = fm.getDatasetModel().getDatasetModelId();
			final var code = fm.getId();

			final var matchingRecord = fieldModelRecords.stream()
				.filter(r -> r.value1().equals(datasetModelId) && r.value2().equals(code))
				.findFirst();

			matchingRecord.ifPresent(uuidStringUUIDRecord3 -> fm.setFieldModelId(uuidStringUUIDRecord3.value3()));
		});
	}
}
