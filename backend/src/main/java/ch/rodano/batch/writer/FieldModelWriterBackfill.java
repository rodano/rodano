package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.DatasetModel;
import ch.rodano.batch.pojo.FieldModel;

import static ch.rodano.batch.helper.ModelResolvers.resolveDatasetModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveFieldModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveValidatorId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowId;
import static ch.rodano.core.model.jooq.tables.FieldModelValidator.FIELD_MODEL_VALIDATOR;
import static ch.rodano.core.model.jooq.tables.FieldModelWorkflow.FIELD_MODEL_WORKFLOW;

public class FieldModelWriterBackfill extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(FieldModelWriterBackfill.class);

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<DatasetModel> wrapped = (ProjectScoped<DatasetModel>) raw;

				final UUID projectId = wrapped.getProjectId();
				final DatasetModel datasetModel = wrapped.getPayload();
				if(datasetModel == null || datasetModel.getId() == null || datasetModel.getId().isBlank()) {
					LOGGER.warn("Skipping dataset model with blank id");
					continue;
				}

				final UUID datasetModelId = resolveDatasetModelId(tx, projectId, datasetModel.getId());
				final List<FieldModel> fields = datasetModel.getFieldModels();
				if(fields == null || fields.isEmpty()) {
					continue;
				}

				for(FieldModel fieldModel : fields) {
					if(fieldModel == null || fieldModel.getId() == null || fieldModel.getId().isBlank()) {
						LOGGER.warn("Skipping field model with blank id");
						continue;
					}

					final UUID fieldModelId = resolveFieldModelId(tx, projectId, datasetModelId, fieldModel.getId());
					final List<String> validatorCodes = fieldModel.getValidatorIds();
					if(validatorCodes != null && !validatorCodes.isEmpty()) {
						for(String validatorCode : validatorCodes) {
							final UUID validatorId = resolveValidatorId(tx, projectId, validatorCode);
							tx.insertInto(FIELD_MODEL_VALIDATOR)
								.set(FIELD_MODEL_VALIDATOR.PROJECT_ID, projectId)
								.set(FIELD_MODEL_VALIDATOR.FIELD_MODEL_ID, fieldModelId)
								.set(FIELD_MODEL_VALIDATOR.VALIDATOR_ID, validatorId)
								.onDuplicateKeyIgnore()
								.execute();
						}
					}

					final List<String> workflowCodes = fieldModel.getWorkflowIds();
					if(workflowCodes != null && !workflowCodes.isEmpty()) {
						for(String workflowCode : workflowCodes) {
							final UUID workflowId = resolveWorkflowId(tx, projectId, workflowCode);
							tx.insertInto(FIELD_MODEL_WORKFLOW)
								.set(FIELD_MODEL_WORKFLOW.PROJECT_ID, projectId)
								.set(FIELD_MODEL_WORKFLOW.FIELD_MODEL_ID, fieldModelId)
								.set(FIELD_MODEL_WORKFLOW.WORKFLOW_ID, workflowId)
								.onDuplicateKeyIgnore()
								.execute();
						}
					}
				}
			}
		});
	}
}
