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

import ch.rodano.api.config.ReportDTO;
import ch.rodano.core.model.jooq.tables.records.ReportRecord;

import static ch.rodano.core.model.jooq.tables.Report.REPORT;
import static ch.rodano.core.model.jooq.tables.ReportField.REPORT_FIELD;

@Repository
public class ReportDAOServiceImpl implements ReportDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public ReportDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "reports", key = "#projectId.toString()")
	public List<ReportDTO> getReports(final UUID projectId) {
		final var records = dslContext.selectFrom(REPORT)
			.where(REPORT.PROJECT_ID.eq(projectId))
			.orderBy(REPORT.CODE)
			.fetch();

		return records.stream()
			.map(record -> mapToDTO(record, getFieldModelIds(record.getReportId())))
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "report", key = "#projectId.toString() + ':' + #reportId.toString()")
	public ReportDTO getReport(final UUID projectId, final UUID reportId) {
		final var record = dslContext.selectFrom(REPORT)
			.where(REPORT.PROJECT_ID.eq(projectId))
			.and(REPORT.REPORT_ID.eq(reportId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		return mapToDTO(record, getFieldModelIds(reportId));
	}

	@Override
	@Transactional
	@CacheEvict(value = "reports", key = "#projectId.toString()")
	public ReportDTO createReport(final UUID projectId, final ReportDTO dto) {
		final var reportId = dto.reportId() != null ? dto.reportId() : UUID.randomUUID();

		dslContext.insertInto(REPORT)
			.set(REPORT.REPORT_ID, reportId)
			.set(REPORT.PROJECT_ID, projectId)
			.set(REPORT.CODE, dto.id())
			.set(REPORT.SHORTNAME, jsonMapperService.toJson(dto.shortname()))
			.set(REPORT.LONGNAME, jsonMapperService.toJson(dto.longname()))
			.set(REPORT.DESCRIPTION, jsonMapperService.toJson(dto.description()))
			.set(REPORT.WORKFLOW_ID, dto.workflowId())
			.set(REPORT.DATASET_MODEL_ID, dto.datasetModelId())
			.execute();

		saveFieldModelIds(projectId, reportId, dto.fieldModelIds());

		return getReport(projectId, reportId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "reports", key = "#projectId.toString()"),
		@CacheEvict(value = "report", key = "#projectId.toString() + ':' + #reportId.toString()")
	})
	public ReportDTO updateReport(final UUID projectId, final UUID reportId, final ReportDTO dto) {
		dslContext.update(REPORT)
			.set(REPORT.CODE, dto.id())
			.set(REPORT.SHORTNAME, jsonMapperService.toJson(dto.shortname()))
			.set(REPORT.LONGNAME, jsonMapperService.toJson(dto.longname()))
			.set(REPORT.DESCRIPTION, jsonMapperService.toJson(dto.description()))
			.set(REPORT.WORKFLOW_ID, dto.workflowId())
			.set(REPORT.DATASET_MODEL_ID, dto.datasetModelId())
			.where(REPORT.PROJECT_ID.eq(projectId))
			.and(REPORT.REPORT_ID.eq(reportId))
			.execute();

		saveFieldModelIds(projectId, reportId, dto.fieldModelIds());

		return getReport(projectId, reportId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "reports", key = "#projectId.toString()"),
		@CacheEvict(value = "report", key = "#projectId.toString() + ':' + #reportId.toString()")
	})
	public void deleteReport(final UUID projectId, final UUID reportId) {
		dslContext.deleteFrom(REPORT_FIELD)
			.where(REPORT_FIELD.PROJECT_ID.eq(projectId))
			.and(REPORT_FIELD.REPORT_ID.eq(reportId))
			.execute();

		dslContext.deleteFrom(REPORT)
			.where(REPORT.PROJECT_ID.eq(projectId))
			.and(REPORT.REPORT_ID.eq(reportId))
			.execute();
	}

	private List<UUID> getFieldModelIds(final UUID reportId) {
		return dslContext.select(REPORT_FIELD.FIELD_MODEL_ID)
			.from(REPORT_FIELD)
			.where(REPORT_FIELD.REPORT_ID.eq(reportId))
			.fetch(REPORT_FIELD.FIELD_MODEL_ID);
	}

	private void saveFieldModelIds(final UUID projectId, final UUID reportId, final List<UUID> fieldModelIds) {
		dslContext.deleteFrom(REPORT_FIELD)
			.where(REPORT_FIELD.PROJECT_ID.eq(projectId))
			.and(REPORT_FIELD.REPORT_ID.eq(reportId))
			.execute();

		if(fieldModelIds == null || fieldModelIds.isEmpty()) {
			return;
		}

		final var insert = dslContext.insertInto(REPORT_FIELD,
			REPORT_FIELD.PROJECT_ID,
			REPORT_FIELD.REPORT_ID,
			REPORT_FIELD.FIELD_MODEL_ID);

		fieldModelIds.forEach(fieldModelId -> insert.values(projectId, reportId, fieldModelId));
		insert.execute();
	}

	private ReportDTO mapToDTO(final ReportRecord record, final List<UUID> fieldModelIds) {
		return new ReportDTO(
			record.getReportId(),
			record.getCode(),
			jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
			}),
			jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
			}),
			jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
			}),
			record.getWorkflowId(),
			record.getDatasetModelId(),
			fieldModelIds
		);
	}
}
