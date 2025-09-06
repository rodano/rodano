package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Report;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveDatasetModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveFieldModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowId;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.Report.REPORT;
import static ch.rodano.core.model.jooq.tables.ReportField.REPORT_FIELD;

public class ReportWriter extends BaseWriter {

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<Report> wrapped = (ProjectScoped<Report>) raw;
				final UUID projectId = wrapped.getProjectId();
				final Report report = wrapped.getPayload();

				final String reportCode = report.getId();
				final UUID reportId = deterministic(projectId, "REPORT", reportCode);

				final UUID datasetModelId = resolveDatasetModelId(tx, projectId, report.getDatasetModelId());
				final UUID workflowId = resolveWorkflowId(tx, projectId, report.getWorkflowId());

				tx.insertInto(REPORT)
					.set(REPORT.PROJECT_ID, projectId)
					.set(REPORT.REPORT_ID, reportId)
					.set(REPORT.CODE, reportCode)
					.set(REPORT.DATASET_MODEL_ID, datasetModelId)
					.set(REPORT.WORKFLOW_ID, workflowId)
					.set(REPORT.SHORTNAME, toJson(report.getShortname()))
					.set(REPORT.LONGNAME, toJson(report.getLongname()))
					.set(REPORT.DESCRIPTION, toJson(report.getDescription()))
					.onDuplicateKeyUpdate()
					.set(REPORT.DATASET_MODEL_ID, datasetModelId)
					.set(REPORT.WORKFLOW_ID, workflowId)
					.set(REPORT.SHORTNAME, toJson(report.getShortname()))
					.set(REPORT.LONGNAME, toJson(report.getLongname()))
					.set(REPORT.DESCRIPTION, toJson(report.getDescription()))
					.execute();

				if(report.getFieldModelIds() != null && !report.getFieldModelIds().isEmpty()) {
					for(String fieldCode : report.getFieldModelIds()) {

						final UUID fieldModelId = resolveFieldModelId(tx, projectId, datasetModelId, fieldCode);

						tx.insertInto(REPORT_FIELD)
							.set(REPORT_FIELD.PROJECT_ID, projectId)
							.set(REPORT_FIELD.REPORT_ID, reportId)
							.set(REPORT_FIELD.FIELD_MODEL_ID, fieldModelId)
							.onDuplicateKeyIgnore()
							.execute();
					}
				}
			}
		});
	}
}
