package ch.rodano.core.services.dao.configurator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.config.EventGroupDTO;
import ch.rodano.api.config.EventModelDTO;
import ch.rodano.api.config.ScopeModelDTO;
import ch.rodano.core.model.jooq.tables.records.EventModelRecord;
import ch.rodano.core.model.jooq.tables.records.ScopeModelRecord;

import static ch.rodano.core.model.jooq.tables.EventModel.EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelDatasetModel.EVENT_MODEL_DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelFormModel.EVENT_MODEL_FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelWorkflow.EVENT_MODEL_WORKFLOW;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelDatasetModel.SCOPE_MODEL_DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelFormModel.SCOPE_MODEL_FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelParent.SCOPE_MODEL_PARENT;
import static ch.rodano.core.model.jooq.tables.ScopeModelWorkflow.SCOPE_MODEL_WORKFLOW;

@Repository
public class ScopeModelDAOServiceImpl implements ScopeModelDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;
	private final EventGroupDAOService eventGroupDAOService;

	public ScopeModelDAOServiceImpl(final DSLContext dslContext,
									final JsonMapperService jsonMapperService,
									final EventGroupDAOService eventGroupDAOService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
		this.eventGroupDAOService = eventGroupDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "scopeModels", key = "#projectId")
	public List<ScopeModelDTO> getScopeModels(final UUID projectId) {
		final var scopeModels = dslContext.selectFrom(SCOPE_MODEL)
			.where(SCOPE_MODEL.PROJECT_ID.eq(projectId))
			.orderBy(SCOPE_MODEL.CODE)
			.fetch();

		return scopeModels.stream()
			.map(record -> mapToDTO(record, projectId))
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "scopeModel", key = "#projectId + '-' + #scopeModelId")
	public ScopeModelDTO getScopeModel(final UUID projectId, final UUID scopeModelId) {
		final var record = dslContext.selectFrom(SCOPE_MODEL)
			.where(SCOPE_MODEL.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		return mapToDTO(record, projectId);
	}

	@Override
	@Transactional
	@CacheEvict(value = { "scopeModels", "scopeModel" }, allEntries = true)
	public ScopeModelDTO createScopeModel(final UUID projectId, final ScopeModelDTO scopeModel) {
		final var scopeModelId = scopeModel.getScopeModelId() != null
			? scopeModel.getScopeModelId()
			: UUID.randomUUID();

		dslContext.insertInto(SCOPE_MODEL)
			.set(SCOPE_MODEL.SCOPE_MODEL_ID, scopeModelId)
			.set(SCOPE_MODEL.PROJECT_ID, projectId)
			.set(SCOPE_MODEL.CODE, scopeModel.getId())
			.set(SCOPE_MODEL.SHORTNAME, jsonMapperService.toJson(scopeModel.getShortname()))
			.set(SCOPE_MODEL.LONGNAME, jsonMapperService.toJson(scopeModel.getLongname()))
			.set(SCOPE_MODEL.DESCRIPTION, jsonMapperService.toJson(scopeModel.getDescription()))
			.set(SCOPE_MODEL.PLURAL_SHORTNAME, jsonMapperService.toJson(scopeModel.getPluralShortname()))
			.set(SCOPE_MODEL.VIRTUAL, scopeModel.isVirtual())
			.set(SCOPE_MODEL.DEFAULT_PARENT_ID, scopeModel.getDefaultParentId())
			.set(SCOPE_MODEL.DEFAULT_PROFILE_ID, scopeModel.getDefaultProfileId())
			.set(SCOPE_MODEL.MAX_NUMBER, scopeModel.getMaxNumber())
			.set(SCOPE_MODEL.SCOPE_FORMAT, scopeModel.getScopeFormat())
			.set(SCOPE_MODEL.LAYOUT, scopeModel.getLayout())
			.execute();

		insertParents(projectId, scopeModelId, scopeModel.getParentIds(), scopeModel.getDefaultParentId());
		insertDatasetModels(projectId, scopeModelId, scopeModel.getDatasetModelIds());
		insertFormModels(projectId, scopeModelId, scopeModel.getFormModelIds());
		insertWorkflows(projectId, scopeModelId, scopeModel.getWorkflowIds());

		return getScopeModel(projectId, scopeModelId);
	}

	@Override
	@Transactional
	@CacheEvict(value = { "scopeModels", "scopeModel" }, allEntries = true)
	public ScopeModelDTO updateScopeModel(final UUID projectId, final UUID scopeModelId, final ScopeModelDTO scopeModel) {
		dslContext.update(SCOPE_MODEL)
			.set(SCOPE_MODEL.CODE, scopeModel.getId())
			.set(SCOPE_MODEL.SHORTNAME, jsonMapperService.toJson(scopeModel.getShortname()))
			.set(SCOPE_MODEL.LONGNAME, jsonMapperService.toJson(scopeModel.getLongname()))
			.set(SCOPE_MODEL.DESCRIPTION, jsonMapperService.toJson(scopeModel.getDescription()))
			.set(SCOPE_MODEL.PLURAL_SHORTNAME, jsonMapperService.toJson(scopeModel.getPluralShortname()))
			.set(SCOPE_MODEL.VIRTUAL, scopeModel.isVirtual())
			.set(SCOPE_MODEL.DEFAULT_PARENT_ID, scopeModel.getDefaultParentId())
			.set(SCOPE_MODEL.DEFAULT_PROFILE_ID, scopeModel.getDefaultProfileId())
			.set(SCOPE_MODEL.MAX_NUMBER, scopeModel.getMaxNumber())
			.set(SCOPE_MODEL.SCOPE_FORMAT, scopeModel.getScopeFormat())
			.set(SCOPE_MODEL.LAYOUT, scopeModel.getLayout())
			.where(SCOPE_MODEL.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.execute();

		deleteRelationships(projectId, scopeModelId);

		insertParents(projectId, scopeModelId, scopeModel.getParentIds(), scopeModel.getDefaultParentId());
		insertDatasetModels(projectId, scopeModelId, scopeModel.getDatasetModelIds());
		insertFormModels(projectId, scopeModelId, scopeModel.getFormModelIds());
		insertWorkflows(projectId, scopeModelId, scopeModel.getWorkflowIds());

		return getScopeModel(projectId, scopeModelId);
	}

	@Override
	@Transactional
	@CacheEvict(value = { "scopeModels", "scopeModel" }, allEntries = true)
	public void deleteScopeModel(final UUID projectId, final UUID scopeModelId) {
		deleteRelationships(projectId, scopeModelId);

		dslContext.deleteFrom(SCOPE_MODEL)
			.where(SCOPE_MODEL.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.execute();
	}

	private void deleteRelationships(final UUID projectId, final UUID scopeModelId) {
		dslContext.deleteFrom(SCOPE_MODEL_PARENT)
			.where(SCOPE_MODEL_PARENT.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL_PARENT.CHILD_SCOPE_MODEL_ID.eq(scopeModelId))
			.execute();

		dslContext.deleteFrom(SCOPE_MODEL_PARENT)
			.where(SCOPE_MODEL_PARENT.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL_PARENT.PARENT_SCOPE_MODEL_ID.eq(scopeModelId))
			.execute();

		dslContext.deleteFrom(SCOPE_MODEL_DATASET_MODEL)
			.where(SCOPE_MODEL_DATASET_MODEL.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL_DATASET_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.execute();

		dslContext.deleteFrom(SCOPE_MODEL_FORM_MODEL)
			.where(SCOPE_MODEL_FORM_MODEL.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL_FORM_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.execute();

		dslContext.deleteFrom(SCOPE_MODEL_WORKFLOW)
			.where(SCOPE_MODEL_WORKFLOW.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL_WORKFLOW.SCOPE_MODEL_ID.eq(scopeModelId))
			.execute();
	}

	private void insertParents(final UUID projectId, final UUID scopeModelId, final List<UUID> parentIds, final UUID defaultParentId) {
		if(parentIds == null || parentIds.isEmpty()) {
			return;
		}

		for(int i = 0; i < parentIds.size(); i++) {
			final var parentId = parentIds.get(i);
			dslContext.insertInto(SCOPE_MODEL_PARENT)
				.set(SCOPE_MODEL_PARENT.PROJECT_ID, projectId)
				.set(SCOPE_MODEL_PARENT.CHILD_SCOPE_MODEL_ID, scopeModelId)
				.set(SCOPE_MODEL_PARENT.PARENT_SCOPE_MODEL_ID, parentId)
				.set(SCOPE_MODEL_PARENT.IS_DEFAULT, parentId.equals(defaultParentId))
				.set(SCOPE_MODEL_PARENT.PARENT_ORDER, i)
				.execute();
		}
	}

	private void insertDatasetModels(final UUID projectId, final UUID scopeModelId, final List<UUID> datasetModelIds) {
		if(datasetModelIds == null || datasetModelIds.isEmpty()) {
			return;
		}

		for(final var datasetModelId : datasetModelIds) {
			dslContext.insertInto(SCOPE_MODEL_DATASET_MODEL)
				.set(SCOPE_MODEL_DATASET_MODEL.PROJECT_ID, projectId)
				.set(SCOPE_MODEL_DATASET_MODEL.SCOPE_MODEL_ID, scopeModelId)
				.set(SCOPE_MODEL_DATASET_MODEL.DATASET_MODEL_ID, datasetModelId)
				.execute();
		}
	}

	private void insertFormModels(final UUID projectId, final UUID scopeModelId, final List<UUID> formModelIds) {
		if(formModelIds == null || formModelIds.isEmpty()) {
			return;
		}

		for(final var formModelId : formModelIds) {
			dslContext.insertInto(SCOPE_MODEL_FORM_MODEL)
				.set(SCOPE_MODEL_FORM_MODEL.PROJECT_ID, projectId)
				.set(SCOPE_MODEL_FORM_MODEL.SCOPE_MODEL_ID, scopeModelId)
				.set(SCOPE_MODEL_FORM_MODEL.FORM_MODEL_ID, formModelId)
				.execute();
		}
	}

	private void insertWorkflows(final UUID projectId, final UUID scopeModelId, final List<UUID> workflowIds) {
		if(workflowIds == null || workflowIds.isEmpty()) {
			return;
		}

		for(final var workflowId : workflowIds) {
			dslContext.insertInto(SCOPE_MODEL_WORKFLOW)
				.set(SCOPE_MODEL_WORKFLOW.PROJECT_ID, projectId)
				.set(SCOPE_MODEL_WORKFLOW.SCOPE_MODEL_ID, scopeModelId)
				.set(SCOPE_MODEL_WORKFLOW.WORKFLOW_ID, workflowId)
				.execute();
		}
	}

	private ScopeModelDTO mapToDTO(final ScopeModelRecord record, final UUID projectId) {
		final var dto = new ScopeModelDTO();
		dto.setScopeModelId(record.getScopeModelId());
		dto.setId(record.getCode());
		dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setLongname(jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setPluralShortname(jsonMapperService.fromJson(record.getPluralShortname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setVirtual(record.getVirtual());
		dto.setDefaultParentId(record.getDefaultParentId());
		dto.setDefaultProfileId(record.getDefaultProfileId());
		dto.setMaxNumber(record.getMaxNumber());
		dto.setScopeFormat(record.getScopeFormat());
		dto.setLayout(record.getLayout());

		final var parentIds = loadParentIds(projectId, record.getScopeModelId());
		dto.setParentIds(parentIds);

		final var childIds = loadChildIds(projectId, record.getScopeModelId());
		dto.setChildScopeModelIds(childIds);

		final var datasetModelIds = loadDatasetModelIds(projectId, record.getScopeModelId());
		dto.setDatasetModelIds(datasetModelIds);

		final var formModelIds = loadFormModelIds(projectId, record.getScopeModelId());
		dto.setFormModelIds(formModelIds);

		final var workflowIds = loadWorkflowIds(projectId, record.getScopeModelId());
		dto.setWorkflowIds(workflowIds);

		dto.setRoot(parentIds.isEmpty());
		dto.setLeaf(childIds.isEmpty());

		final var eventModels = loadEventModels(projectId, record.getScopeModelId());
		dto.setEventModels(eventModels);

		final var eventGroups = loadEventGroups(projectId, record.getScopeModelId());
		dto.setEventGroups(eventGroups);

		return dto;
	}

	private List<UUID> loadParentIds(final UUID projectId, final UUID scopeModelId) {
		return dslContext.select(SCOPE_MODEL_PARENT.PARENT_SCOPE_MODEL_ID)
			.from(SCOPE_MODEL_PARENT)
			.where(SCOPE_MODEL_PARENT.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL_PARENT.CHILD_SCOPE_MODEL_ID.eq(scopeModelId))
			.orderBy(SCOPE_MODEL_PARENT.PARENT_ORDER)
			.fetch(SCOPE_MODEL_PARENT.PARENT_SCOPE_MODEL_ID);
	}

	private List<UUID> loadChildIds(final UUID projectId, final UUID scopeModelId) {
		return dslContext.select(SCOPE_MODEL_PARENT.CHILD_SCOPE_MODEL_ID)
			.from(SCOPE_MODEL_PARENT)
			.where(SCOPE_MODEL_PARENT.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL_PARENT.PARENT_SCOPE_MODEL_ID.eq(scopeModelId))
			.fetch(SCOPE_MODEL_PARENT.CHILD_SCOPE_MODEL_ID);
	}

	private List<UUID> loadDatasetModelIds(final UUID projectId, final UUID scopeModelId) {
		return dslContext.select(SCOPE_MODEL_DATASET_MODEL.DATASET_MODEL_ID)
			.from(SCOPE_MODEL_DATASET_MODEL)
			.where(SCOPE_MODEL_DATASET_MODEL.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL_DATASET_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetch(SCOPE_MODEL_DATASET_MODEL.DATASET_MODEL_ID);
	}

	private List<UUID> loadFormModelIds(final UUID projectId, final UUID scopeModelId) {
		return dslContext.select(SCOPE_MODEL_FORM_MODEL.FORM_MODEL_ID)
			.from(SCOPE_MODEL_FORM_MODEL)
			.where(SCOPE_MODEL_FORM_MODEL.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL_FORM_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetch(SCOPE_MODEL_FORM_MODEL.FORM_MODEL_ID);
	}

	private List<UUID> loadWorkflowIds(final UUID projectId, final UUID scopeModelId) {
		return dslContext.select(SCOPE_MODEL_WORKFLOW.WORKFLOW_ID)
			.from(SCOPE_MODEL_WORKFLOW)
			.where(SCOPE_MODEL_WORKFLOW.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL_WORKFLOW.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetch(SCOPE_MODEL_WORKFLOW.WORKFLOW_ID);
	}

	private List<EventModelDTO> loadEventModels(final UUID projectId, final UUID scopeModelId) {
		final var eventModelRecords = dslContext.selectFrom(EVENT_MODEL)
			.where(EVENT_MODEL.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.orderBy(EVENT_MODEL.CODE)
			.fetch();

		if(eventModelRecords.isEmpty()) {
			return new ArrayList<>();
		}

		final var eventModelIds = eventModelRecords.stream()
			.map(EventModelRecord::getEventModelId)
			.collect(Collectors.toList());

		final var datasetsByEvent = loadAllEventModelDatasets(projectId, eventModelIds);
		final var formsByEvent = loadAllEventModelForms(projectId, eventModelIds);
		final var workflowsByEvent = loadAllEventModelWorkflows(projectId, eventModelIds);

		return eventModelRecords.stream()
			.map(record -> {
				final var dto = new EventModelDTO();
				dto.setEventModelId(record.getEventModelId());
				dto.setId(record.getCode());
				dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
				}));
				dto.setDatasetModelIds(datasetsByEvent.getOrDefault(record.getEventModelId(), new ArrayList<>()));
				dto.setFormModelIds(formsByEvent.getOrDefault(record.getEventModelId(), new ArrayList<>()));
				dto.setWorkflowIds(workflowsByEvent.getOrDefault(record.getEventModelId(), new ArrayList<>()));

				return dto;
			})
			.collect(Collectors.toList());
	}

	private List<EventGroupDTO> loadEventGroups(final UUID projectId, final UUID scopeModelId) {
		return eventGroupDAOService.getEventGroupsByScopeModel(projectId, scopeModelId);
	}

	private Map<UUID, List<UUID>> loadAllEventModelDatasets(final UUID projectId, final List<UUID> eventModelIds) {
		return dslContext.select(EVENT_MODEL_DATASET_MODEL.EVENT_MODEL_ID, EVENT_MODEL_DATASET_MODEL.DATASET_MODEL_ID)
			.from(EVENT_MODEL_DATASET_MODEL)
			.where(EVENT_MODEL_DATASET_MODEL.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_DATASET_MODEL.EVENT_MODEL_ID.in(eventModelIds))
			.fetch()
			.stream()
			.collect(Collectors.groupingBy(
				Record2::value1,
				Collectors.mapping(Record2::value2, Collectors.toList())
			));
	}

	private Map<UUID, List<UUID>> loadAllEventModelForms(final UUID projectId, final List<UUID> eventModelIds) {
		return dslContext.select(EVENT_MODEL_FORM_MODEL.EVENT_MODEL_ID, EVENT_MODEL_FORM_MODEL.FORM_MODEL_ID)
			.from(EVENT_MODEL_FORM_MODEL)
			.where(EVENT_MODEL_FORM_MODEL.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_FORM_MODEL.EVENT_MODEL_ID.in(eventModelIds))
			.fetch()
			.stream()
			.collect(Collectors.groupingBy(
				Record2::value1,
				Collectors.mapping(Record2::value2, Collectors.toList())
			));
	}

	private Map<UUID, List<UUID>> loadAllEventModelWorkflows(final UUID projectId, final List<UUID> eventModelIds) {
		return dslContext.select(EVENT_MODEL_WORKFLOW.EVENT_MODEL_ID, EVENT_MODEL_WORKFLOW.WORKFLOW_ID)
			.from(EVENT_MODEL_WORKFLOW)
			.where(EVENT_MODEL_WORKFLOW.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_WORKFLOW.EVENT_MODEL_ID.in(eventModelIds))
			.fetch()
			.stream()
			.collect(Collectors.groupingBy(
				Record2::value1,
				Collectors.mapping(Record2::value2, Collectors.toList())
			));
	}
}
