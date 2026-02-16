package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.Map;
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

import ch.rodano.api.config.DatasetModelDTO;
import ch.rodano.api.config.FieldModelDTO;
import ch.rodano.core.model.jooq.tables.records.DatasetModelRecord;

import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;

@Repository
public class DatasetModelDAOServiceImpl implements DatasetModelDAOService {

	private static final String VIEW_SUMMARY = "summary";
	private static final String VIEW_FULL = "full";

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;
	private final FieldModelDAOService fieldModelDAOService;

	public DatasetModelDAOServiceImpl(final DSLContext dslContext,
									  final JsonMapperService jsonMapperService,
									  final FieldModelDAOService fieldModelDAOService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
		this.fieldModelDAOService = fieldModelDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<DatasetModelDTO> getDatasetModels(final UUID projectId, final String view) {
		final var normalized = view == null ? VIEW_SUMMARY : view.trim().toLowerCase();
		return switch(normalized) {
			case VIEW_FULL -> getDatasetModelsFull(projectId);
			case VIEW_SUMMARY -> getDatasetModelsSummary(projectId);
			default -> getDatasetModelsSummary(projectId);
		};
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "datasetModels", key = "#projectId.toString() + ':summary'")
	public List<DatasetModelDTO> getDatasetModelsSummary(final UUID projectId) {
		final var records = dslContext.selectFrom(DATASET_MODEL)
			.where(DATASET_MODEL.PROJECT_ID.eq(projectId))
			.orderBy(DATASET_MODEL.CODE)
			.fetch();

		return records.stream()
			.map(record -> mapToDTO(record, List.of()))
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "datasetModels", key = "#projectId.toString() + ':full'")
	public List<DatasetModelDTO> getDatasetModelsFull(final UUID projectId) {
		final var records = dslContext.selectFrom(DATASET_MODEL)
			.where(DATASET_MODEL.PROJECT_ID.eq(projectId))
			.orderBy(DATASET_MODEL.CODE)
			.fetch();

		if(records.isEmpty()) {
			return List.of();
		}

		final var allFields = fieldModelDAOService.getFieldModels(projectId, VIEW_FULL);

		final Map<UUID, List<FieldModelDTO>> fieldsByDataset = allFields.stream()
			.filter(f -> f.getDatasetModelId() != null)
			.collect(Collectors.groupingBy(FieldModelDTO::getDatasetModelId));

		return records.stream()
			.map(record -> mapToDTO(
				record,
				fieldsByDataset.getOrDefault(record.getDatasetModelId(), List.of())
			))
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "datasetModel", key = "#projectId.toString() + ':' + #datasetModelId.toString()")
	public DatasetModelDTO getDatasetModel(final UUID projectId, final UUID datasetModelId) {
		final var record = dslContext.selectFrom(DATASET_MODEL)
			.where(DATASET_MODEL.PROJECT_ID.eq(projectId))
			.and(DATASET_MODEL.DATASET_MODEL_ID.eq(datasetModelId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		final var fields = fieldModelDAOService.getFieldModels(projectId, VIEW_FULL).stream()
			.filter(f -> datasetModelId.equals(f.getDatasetModelId()))
			.collect(Collectors.toList());
		return mapToDTO(record, fields);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "datasetModels", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "datasetModels", key = "#projectId.toString() + ':full'")
	})
	public DatasetModelDTO createDatasetModel(final UUID projectId, final DatasetModelDTO datasetModel) {
		final var datasetModelId = datasetModel.getDatasetModelId() != null
			? datasetModel.getDatasetModelId()
			: UUID.randomUUID();

		dslContext.insertInto(DATASET_MODEL)
			.set(DATASET_MODEL.DATASET_MODEL_ID, datasetModelId)
			.set(DATASET_MODEL.PROJECT_ID, projectId)
			.set(DATASET_MODEL.CODE, datasetModel.getId())
			.set(DATASET_MODEL.SHORTNAME, jsonMapperService.toJson(datasetModel.getShortname()))
			.set(DATASET_MODEL.LONGNAME, jsonMapperService.toJson(datasetModel.getLongname()))
			.set(DATASET_MODEL.DESCRIPTION, jsonMapperService.toJson(datasetModel.getDescription()))
			.set(DATASET_MODEL.MULTIPLE, datasetModel.isMultiple())
			.set(DATASET_MODEL.MASTER, datasetModel.isMaster())
			.set(DATASET_MODEL.EXPORTABLE, datasetModel.isExportable())
			.set(DATASET_MODEL.EXPORT_ORDER, datasetModel.getExportOrder())
			.set(DATASET_MODEL.FAMILY, datasetModel.getFamily())
			.set(DATASET_MODEL.COLLAPSED_LABEL_PATTERN, datasetModel.getCollapsedLabelPattern())
			.set(DATASET_MODEL.EXPANDED_LABEL_PATTERN, datasetModel.getExpandedLabelPattern())
			.execute();

		return getDatasetModel(projectId, datasetModelId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "datasetModels", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "datasetModels", key = "#projectId.toString() + ':full'"),
		@CacheEvict(value = "datasetModel", key = "#projectId.toString() + ':' + #datasetModelId.toString()")
	})
	public DatasetModelDTO updateDatasetModel(final UUID projectId, final UUID datasetModelId, final DatasetModelDTO datasetModel) {
		dslContext.update(DATASET_MODEL)
			.set(DATASET_MODEL.CODE, datasetModel.getId())
			.set(DATASET_MODEL.SHORTNAME, jsonMapperService.toJson(datasetModel.getShortname()))
			.set(DATASET_MODEL.LONGNAME, jsonMapperService.toJson(datasetModel.getLongname()))
			.set(DATASET_MODEL.DESCRIPTION, jsonMapperService.toJson(datasetModel.getDescription()))
			.set(DATASET_MODEL.MULTIPLE, datasetModel.isMultiple())
			.set(DATASET_MODEL.MASTER, datasetModel.isMaster())
			.set(DATASET_MODEL.EXPORTABLE, datasetModel.isExportable())
			.set(DATASET_MODEL.EXPORT_ORDER, datasetModel.getExportOrder())
			.set(DATASET_MODEL.FAMILY, datasetModel.getFamily())
			.set(DATASET_MODEL.COLLAPSED_LABEL_PATTERN, datasetModel.getCollapsedLabelPattern())
			.set(DATASET_MODEL.EXPANDED_LABEL_PATTERN, datasetModel.getExpandedLabelPattern())
			.where(DATASET_MODEL.PROJECT_ID.eq(projectId))
			.and(DATASET_MODEL.DATASET_MODEL_ID.eq(datasetModelId))
			.execute();

		return getDatasetModel(projectId, datasetModelId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "datasetModels", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "datasetModels", key = "#projectId.toString() + ':full'"),
		@CacheEvict(value = "datasetModel", key = "#projectId.toString() + ':' + #datasetModelId.toString()")
	})
	public void deleteDatasetModel(final UUID projectId, final UUID datasetModelId) {
		dslContext.deleteFrom(DATASET_MODEL)
			.where(DATASET_MODEL.PROJECT_ID.eq(projectId))
			.and(DATASET_MODEL.DATASET_MODEL_ID.eq(datasetModelId))
			.execute();
	}

	private DatasetModelDTO mapToDTO(final DatasetModelRecord record, final List<FieldModelDTO> fieldModels) {
		final var dto = new DatasetModelDTO();
		dto.setDatasetModelId(record.getDatasetModelId());
		dto.setId(record.getCode());
		dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setLongname(jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setMultiple(record.getMultiple());
		dto.setMaster(record.getMaster());
		dto.setExportable(record.getExportable());
		dto.setExportOrder(record.getExportOrder());
		dto.setFamily(record.getFamily());
		dto.setCollapsedLabelPattern(record.getCollapsedLabelPattern());
		dto.setExpandedLabelPattern(record.getExpandedLabelPattern());

		dto.setFieldModels(fieldModels != null ? fieldModels : List.of());

		return dto;
	}
}
