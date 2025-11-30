package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.reports.Report;
import ch.rodano.core.model.jooq.tables.records.ReportRecord;

import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.FieldModel.FIELD_MODEL;
import static ch.rodano.core.model.jooq.tables.Report.REPORT;
import static ch.rodano.core.model.jooq.tables.ReportField.REPORT_FIELD;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;

@Repository
public class ReportDAO implements BaseProjectDAO<Report> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;

	public ReportDAO(final DSLContext dslContext, final MappingHelper mappingHelper) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
	}

	@Override
	public List<Report> findByProject(final UUID projectId) {
		return dslContext.selectFrom(REPORT)
			.where(REPORT.PROJECT_ID.eq(projectId))
			.fetch(this::mapToModel);
	}

	@Override
	public Report findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(REPORT)
			.where(REPORT.PROJECT_ID.eq(projectId))
			.and(REPORT.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public Report findById(final UUID id) {
		return dslContext.selectFrom(REPORT)
			.where(REPORT.REPORT_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public Report save(final Report entity) {
		return null;
	}

	@Override
	public void delete(final UUID id) {

	}

	private Report mapToModel(final ReportRecord record) {
		if(record == null) {
			return null;
		}

		final Report model = new Report();

		model.setReportId(record.getReportId());
		model.setId(record.getCode());

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

		if(record.getDatasetModelId() != null) {
			model.setDatasetModelId(getDatasetModelCode(record.getDatasetModelId()));
		}

		if(record.getWorkflowId() != null) {
			model.setWorkflowId(getWorkflowCode(record.getWorkflowId()));
		}

		model.setFieldModelIds(loadFieldModelIds(record.getReportId()));

		return model;
	}

	private List<String> loadFieldModelIds(final UUID reportId) {
		return dslContext.select(FIELD_MODEL.CODE)
			.from(REPORT_FIELD)
			.join(FIELD_MODEL).on(FIELD_MODEL.FIELD_MODEL_ID.eq(REPORT_FIELD.FIELD_MODEL_ID))
			.where(REPORT_FIELD.REPORT_ID.eq(reportId))
			.fetch(FIELD_MODEL.CODE);
	}

	private String getDatasetModelCode(final UUID datasetModelId) {
		return dslContext.select(DATASET_MODEL.CODE)
			.from(DATASET_MODEL)
			.where(DATASET_MODEL.DATASET_MODEL_ID.eq(datasetModelId))
			.fetchOne(DATASET_MODEL.CODE);
	}

	private String getWorkflowCode(final UUID workflowId) {
		return dslContext.select(WORKFLOW.CODE)
			.from(WORKFLOW)
			.where(WORKFLOW.WORKFLOW_ID.eq(workflowId))
			.fetchOne(WORKFLOW.CODE);
	}
}
