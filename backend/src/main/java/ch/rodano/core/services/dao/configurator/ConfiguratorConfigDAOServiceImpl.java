package ch.rodano.core.services.dao.configurator;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.rodano.api.config.ScopeModelDTO;
import ch.rodano.core.model.jooq.tables.records.ScopeModelRecord;

import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelDatasetModel.SCOPE_MODEL_DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelFormModel.SCOPE_MODEL_FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelParent.SCOPE_MODEL_PARENT;
import static ch.rodano.core.model.jooq.tables.ScopeModelWorkflow.SCOPE_MODEL_WORKFLOW;

@Repository
public class ConfiguratorConfigDAOServiceImpl implements ConfiguratorConfigDAOService {

	private final DSLContext dslContext;
	private final ObjectMapper objectMapper;

	public ConfiguratorConfigDAOServiceImpl(final DSLContext dslContext, final ObjectMapper objectMapper) {
		this.dslContext = dslContext;
		this.objectMapper = objectMapper;
	}

	@Override
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
	public ScopeModelDTO createScopeModel(final UUID projectId, final ScopeModelDTO scopeModel) {
		final var scopeModelId = scopeModel.getScopeModelId() != null
			? scopeModel.getScopeModelId()
			: UUID.randomUUID();

		dslContext.insertInto(SCOPE_MODEL)
			.set(SCOPE_MODEL.SCOPE_MODEL_ID, scopeModelId)
			.set(SCOPE_MODEL.PROJECT_ID, projectId)
			.set(SCOPE_MODEL.CODE, scopeModel.getId())
			.set(SCOPE_MODEL.SHORTNAME, toJson(scopeModel.getShortname()))
			.set(SCOPE_MODEL.LONGNAME, toJson(scopeModel.getLongname()))
			.set(SCOPE_MODEL.DESCRIPTION, toJson(scopeModel.getDescription()))
			.set(SCOPE_MODEL.PLURAL_SHORTNAME, toJson(scopeModel.getPluralShortname()))
			.set(SCOPE_MODEL.VIRTUAL, scopeModel.isVirtual())
			.set(SCOPE_MODEL.DEFAULT_PARENT_ID, scopeModel.getDefaultParentId())
			.set(SCOPE_MODEL.DEFAULT_PROFILE_ID, scopeModel.getDefaultProfileId())
			.execute();

		insertParents(projectId, scopeModelId, scopeModel.getParentIds(), scopeModel.getDefaultParentId());
		insertDatasetModels(projectId, scopeModelId, scopeModel.getDatasetModelIds());
		insertFormModels(projectId, scopeModelId, scopeModel.getFormModelIds());
		insertWorkflows(projectId, scopeModelId, scopeModel.getWorkflowIds());

		return getScopeModel(projectId, scopeModelId);
	}

	@Override
	public ScopeModelDTO updateScopeModel(final UUID projectId, final UUID scopeModelId, final ScopeModelDTO scopeModel) {
		dslContext.update(SCOPE_MODEL)
			.set(SCOPE_MODEL.CODE, scopeModel.getId())
			.set(SCOPE_MODEL.SHORTNAME, toJson(scopeModel.getShortname()))
			.set(SCOPE_MODEL.LONGNAME, toJson(scopeModel.getLongname()))
			.set(SCOPE_MODEL.DESCRIPTION, toJson(scopeModel.getDescription()))
			.set(SCOPE_MODEL.PLURAL_SHORTNAME, toJson(scopeModel.getPluralShortname()))
			.set(SCOPE_MODEL.VIRTUAL, scopeModel.isVirtual())
			.set(SCOPE_MODEL.DEFAULT_PARENT_ID, scopeModel.getDefaultParentId())
			.set(SCOPE_MODEL.DEFAULT_PROFILE_ID, scopeModel.getDefaultProfileId())
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
		dto.setShortname(fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {}));
		dto.setLongname(fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {}));
		dto.setDescription(fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {}));
		dto.setPluralShortname(fromJson(record.getPluralShortname(), new TypeReference<TreeMap<String, String>>() {}));
		dto.setVirtual(record.getVirtual());
		dto.setDefaultParentId(record.getDefaultParentId());
		dto.setDefaultProfileId(record.getDefaultProfileId());

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

		// TODO: Load event groups and event models if needed
		dto.setEventGroups(new ArrayList<>());
		dto.setEventModels(new ArrayList<>());

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

	private String toJson(final Object obj) {
		if(obj == null) {
			return null;
		}
		try {
			return objectMapper.writeValueAsString(obj);
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to serialize to JSON", e);
		}
	}

	private <T> T fromJson(final String json, final TypeReference<T> typeRef) {
		if(json == null || json.isEmpty()) {
			return null;
		}
		try {
			return objectMapper.readValue(json, typeRef);
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to deserialize JSON", e);
		}
	}
}
