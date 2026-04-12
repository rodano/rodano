package ch.rodano.core.services.dao.configurator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.config.ScopeModelDTO;
import ch.rodano.core.model.jooq.tables.records.ScopeModelRecord;

import static ch.rodano.core.model.jooq.tables.Event.EVENT;
import static ch.rodano.core.model.jooq.tables.EventGroup.EVENT_GROUP;
import static ch.rodano.core.model.jooq.tables.EventModel.EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelBlockedEvent.EVENT_MODEL_BLOCKED_EVENT;
import static ch.rodano.core.model.jooq.tables.EventModelDatasetModel.EVENT_MODEL_DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelDeadlineReference.EVENT_MODEL_DEADLINE_REFERENCE;
import static ch.rodano.core.model.jooq.tables.EventModelFormModel.EVENT_MODEL_FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelImpliedEvent.EVENT_MODEL_IMPLIED_EVENT;
import static ch.rodano.core.model.jooq.tables.EventModelWorkflow.EVENT_MODEL_WORKFLOW;
import static ch.rodano.core.model.jooq.tables.Scope.SCOPE;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelDatasetModel.SCOPE_MODEL_DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelFormModel.SCOPE_MODEL_FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelParent.SCOPE_MODEL_PARENT;
import static ch.rodano.core.model.jooq.tables.ScopeModelWorkflow.SCOPE_MODEL_WORKFLOW;
import static ch.rodano.core.model.jooq.tables.ScopeModelWorkflowStateSelector.SCOPE_MODEL_WORKFLOW_STATE_SELECTOR;
import static ch.rodano.core.model.jooq.tables.WorkflowState.WORKFLOW_STATE;

@Repository
public class ScopeModelDAOServiceImpl implements ScopeModelDAOService {

	private static final String VIEW_SUMMARY = "summary";
	private static final String VIEW_FULL = "full";

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public ScopeModelDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ScopeModelDTO> getScopeModels(final UUID projectId, final String view) {
		final var normalized = view == null ? VIEW_SUMMARY : view.trim().toLowerCase();
		return switch(normalized) {
			case VIEW_FULL -> getScopeModelsFull(projectId);
			case VIEW_SUMMARY -> getScopeModelsSummary(projectId);
			default -> getScopeModelsSummary(projectId);
		};
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "scopeModels", key = "#projectId.toString() + ':summary'")
	public List<ScopeModelDTO> getScopeModelsSummary(final UUID projectId) {
		final var scopeModelRecords = dslContext
			.selectFrom(SCOPE_MODEL)
			.where(SCOPE_MODEL.PROJECT_ID.eq(projectId))
			.orderBy(SCOPE_MODEL.CODE)
			.fetch();

		if(scopeModelRecords.isEmpty()) {
			return List.of();
		}

		return scopeModelRecords.map(record -> mapToDTO(
			record, Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of()
		));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "scopeModels", key = "#projectId.toString() + ':full'")
	public List<ScopeModelDTO> getScopeModelsFull(final UUID projectId) {
		final var scopeModelRecords = dslContext
			.selectFrom(SCOPE_MODEL)
			.where(SCOPE_MODEL.PROJECT_ID.eq(projectId))
			.orderBy(SCOPE_MODEL.CODE)
			.fetch();

		if(scopeModelRecords.isEmpty()) {
			return List.of();
		}

		final var scopeModelIds = scopeModelRecords.map(ScopeModelRecord::getScopeModelId);
		final var parentMap = loadParentIds(projectId, scopeModelIds);
		final var childMap = loadChildIds(projectId, scopeModelIds);
		final var datasetMap = loadDatasetModelIds(projectId, scopeModelIds);
		final var formMap = loadFormModelIds(projectId, scopeModelIds);
		final var workflowMap = loadWorkflowIds(projectId, scopeModelIds);
		final var workflowStateMap = loadWorkflowStateIds(projectId, scopeModelIds);

		return scopeModelRecords.map(record -> mapToDTO(
			record, parentMap, childMap, datasetMap, formMap, workflowMap, workflowStateMap
		));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "scopeModel", key = "#projectId.toString() + ':' + #scopeModelId.toString()")
	public ScopeModelDTO getScopeModel(final UUID projectId, final UUID scopeModelId) {
		final var record = dslContext
			.selectFrom(SCOPE_MODEL)
			.where(SCOPE_MODEL.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		final var scopeModelIds = List.of(scopeModelId);
		final var parentMap = loadParentIds(projectId, scopeModelIds);
		final var childMap = loadChildIds(projectId, scopeModelIds);
		final var datasetMap = loadDatasetModelIds(projectId, scopeModelIds);
		final var formMap = loadFormModelIds(projectId, scopeModelIds);
		final var workflowMap = loadWorkflowIds(projectId, scopeModelIds);
		final var workflowStateMap = loadWorkflowStateIds(projectId, scopeModelIds);

		return mapToDTO(record, parentMap, childMap, datasetMap, formMap, workflowMap, workflowStateMap);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "scopeModels", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "scopeModels", key = "#projectId.toString() + ':full'")
	})
	public ScopeModelDTO createScopeModel(final UUID projectId, final ScopeModelDTO dto) {
		final var scopeModelId = dto.getScopeModelId() != null ? dto.getScopeModelId() : UUID.randomUUID();

		dslContext.insertInto(SCOPE_MODEL)
			.set(SCOPE_MODEL.SCOPE_MODEL_ID, scopeModelId)
			.set(SCOPE_MODEL.PROJECT_ID, projectId)
			.set(SCOPE_MODEL.CODE, dto.getId())
			.set(SCOPE_MODEL.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(SCOPE_MODEL.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(SCOPE_MODEL.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(SCOPE_MODEL.PLURAL_SHORTNAME, jsonMapperService.toJson(dto.getPluralShortname()))
			.set(SCOPE_MODEL.VIRTUAL, dto.isVirtual())
			.set(SCOPE_MODEL.DEFAULT_PARENT_ID, dto.getDefaultParentId())
			.set(SCOPE_MODEL.DEFAULT_PROFILE_ID, dto.getDefaultProfileId())
			.set(SCOPE_MODEL.EXPECTED_NUMBER, dto.getExpectedNumber())
			.set(SCOPE_MODEL.MAX_NUMBER, dto.getMaxNumber())
			.set(SCOPE_MODEL.SCOPE_FORMAT, dto.getScopeFormat())
			.execute();

		replaceRelations(projectId, scopeModelId, dto);

		return getScopeModel(projectId, scopeModelId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "scopeModels", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "scopeModels", key = "#projectId.toString() + ':full'"),
		@CacheEvict(value = "scopeModel", key = "#projectId.toString() + ':' + #scopeModelId.toString()")
	})
	public ScopeModelDTO updateScopeModel(final UUID projectId, final UUID scopeModelId, final ScopeModelDTO dto) {
		dslContext.update(SCOPE_MODEL)
			.set(SCOPE_MODEL.CODE, dto.getId())
			.set(SCOPE_MODEL.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(SCOPE_MODEL.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(SCOPE_MODEL.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(SCOPE_MODEL.PLURAL_SHORTNAME, jsonMapperService.toJson(dto.getPluralShortname()))
			.set(SCOPE_MODEL.VIRTUAL, dto.isVirtual())
			.set(SCOPE_MODEL.DEFAULT_PARENT_ID, dto.getDefaultParentId())
			.set(SCOPE_MODEL.DEFAULT_PROFILE_ID, dto.getDefaultProfileId())
			.set(SCOPE_MODEL.EXPECTED_NUMBER, dto.getExpectedNumber())
			.set(SCOPE_MODEL.MAX_NUMBER, dto.getMaxNumber())
			.set(SCOPE_MODEL.SCOPE_FORMAT, dto.getScopeFormat())
			.where(SCOPE_MODEL.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.execute();

		replaceRelations(projectId, scopeModelId, dto);

		return getScopeModel(projectId, scopeModelId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "scopeModels", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "scopeModels", key = "#projectId.toString() + ':full'"),
		@CacheEvict(value = "scopeModel", key = "#projectId.toString() + ':' + #scopeModelId.toString()")
	})
	public void deleteScopeModel(final UUID projectId, final UUID scopeModelId) {
		final var eventModelIds = dslContext
			.select(EVENT_MODEL.EVENT_MODEL_ID).from(EVENT_MODEL)
			.where(EVENT_MODEL.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL.SCOPE_MODEL_ID.eq(scopeModelId));

		dslContext.deleteFrom(EVENT_MODEL_DATASET_MODEL).where(EVENT_MODEL_DATASET_MODEL.PROJECT_ID.eq(projectId)).and(EVENT_MODEL_DATASET_MODEL.EVENT_MODEL_ID.in(eventModelIds)).execute();
		dslContext.deleteFrom(EVENT_MODEL_FORM_MODEL).where(EVENT_MODEL_FORM_MODEL.PROJECT_ID.eq(projectId)).and(EVENT_MODEL_FORM_MODEL.EVENT_MODEL_ID.in(eventModelIds)).execute();
		dslContext.deleteFrom(EVENT_MODEL_WORKFLOW).where(EVENT_MODEL_WORKFLOW.PROJECT_ID.eq(projectId)).and(EVENT_MODEL_WORKFLOW.EVENT_MODEL_ID.in(eventModelIds)).execute();
		dslContext.deleteFrom(EVENT_MODEL_DEADLINE_REFERENCE).where(EVENT_MODEL_DEADLINE_REFERENCE.PROJECT_ID.eq(projectId)).and(EVENT_MODEL_DEADLINE_REFERENCE.EVENT_MODEL_ID.in(eventModelIds)).execute();
		dslContext.deleteFrom(EVENT_MODEL_BLOCKED_EVENT).where(EVENT_MODEL_BLOCKED_EVENT.PROJECT_ID.eq(projectId)).and(EVENT_MODEL_BLOCKED_EVENT.EVENT_MODEL_ID.in(eventModelIds)).execute();
		dslContext.deleteFrom(EVENT_MODEL_IMPLIED_EVENT).where(EVENT_MODEL_IMPLIED_EVENT.PROJECT_ID.eq(projectId)).and(EVENT_MODEL_IMPLIED_EVENT.EVENT_MODEL_ID.in(eventModelIds)).execute();
		dslContext.deleteFrom(EVENT_MODEL).where(EVENT_MODEL.PROJECT_ID.eq(projectId)).and(EVENT_MODEL.SCOPE_MODEL_ID.eq(scopeModelId)).execute();

		dslContext.deleteFrom(EVENT_GROUP).where(EVENT_GROUP.PROJECT_ID.eq(projectId)).and(EVENT_GROUP.SCOPE_MODEL_ID.eq(scopeModelId)).execute();

		dslContext.deleteFrom(SCOPE_MODEL_PARENT).where(SCOPE_MODEL_PARENT.PROJECT_ID.eq(projectId)).and(SCOPE_MODEL_PARENT.CHILD_SCOPE_MODEL_ID.eq(scopeModelId)).execute();
		dslContext.deleteFrom(SCOPE_MODEL_PARENT).where(SCOPE_MODEL_PARENT.PROJECT_ID.eq(projectId)).and(SCOPE_MODEL_PARENT.PARENT_SCOPE_MODEL_ID.eq(scopeModelId)).execute();
		dslContext.deleteFrom(SCOPE_MODEL_DATASET_MODEL).where(SCOPE_MODEL_DATASET_MODEL.PROJECT_ID.eq(projectId)).and(SCOPE_MODEL_DATASET_MODEL.SCOPE_MODEL_ID.eq(scopeModelId)).execute();
		dslContext.deleteFrom(SCOPE_MODEL_FORM_MODEL).where(SCOPE_MODEL_FORM_MODEL.PROJECT_ID.eq(projectId)).and(SCOPE_MODEL_FORM_MODEL.SCOPE_MODEL_ID.eq(scopeModelId)).execute();
		dslContext.deleteFrom(SCOPE_MODEL_WORKFLOW).where(SCOPE_MODEL_WORKFLOW.PROJECT_ID.eq(projectId)).and(SCOPE_MODEL_WORKFLOW.SCOPE_MODEL_ID.eq(scopeModelId)).execute();
		dslContext.deleteFrom(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR).where(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.PROJECT_ID.eq(projectId)).and(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.SCOPE_MODEL_ID.eq(scopeModelId)).execute();

		dslContext.deleteFrom(SCOPE_MODEL).where(SCOPE_MODEL.PROJECT_ID.eq(projectId)).and(SCOPE_MODEL.SCOPE_MODEL_ID.eq(scopeModelId)).execute();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasPatientData(final UUID projectId, final UUID scopeModelId) {
		if(dslContext.fetchExists(
			dslContext.selectOne()
				.from(SCOPE)
				.where(SCOPE.PROJECT_ID.eq(projectId))
				.and(SCOPE.SCOPE_MODEL_ID.eq(scopeModelId))
		)) {
			return true;
		}
		return dslContext.fetchExists(
			dslContext.selectOne()
				.from(EVENT)
				.where(EVENT.PROJECT_ID.eq(projectId))
				.and(EVENT.SCOPE_MODEL_ID.eq(scopeModelId))
		);
	}

	private void replaceRelations(final UUID projectId, final UUID scopeModelId, final ScopeModelDTO dto) {
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

		dslContext.deleteFrom(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR)
			.where(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.SCOPE_MODEL_ID.eq(scopeModelId))
			.execute();

		batchInsert(projectId, scopeModelId, dto);
	}

	private void batchInsert(final UUID projectId, final UUID scopeModelId, final ScopeModelDTO dto) {
		if(dto.getParentIds() != null) {
			for(final var parentId : dto.getParentIds()) {
				dslContext.insertInto(SCOPE_MODEL_PARENT)
					.set(SCOPE_MODEL_PARENT.PROJECT_ID, projectId)
					.set(SCOPE_MODEL_PARENT.PARENT_SCOPE_MODEL_ID, parentId)
					.set(SCOPE_MODEL_PARENT.CHILD_SCOPE_MODEL_ID, scopeModelId)
					.execute();
			}
		}

		if(dto.getChildScopeModelIds() != null) {
			for(final var childId : dto.getChildScopeModelIds()) {
				dslContext.insertInto(SCOPE_MODEL_PARENT)
					.set(SCOPE_MODEL_PARENT.PROJECT_ID, projectId)
					.set(SCOPE_MODEL_PARENT.CHILD_SCOPE_MODEL_ID, childId)
					.set(SCOPE_MODEL_PARENT.PARENT_SCOPE_MODEL_ID, scopeModelId)
					.execute();
			}
		}

		if(dto.getDatasetModelIds() != null) {
			for(final var datasetId : dto.getDatasetModelIds()) {
				dslContext.insertInto(SCOPE_MODEL_DATASET_MODEL)
					.set(SCOPE_MODEL_DATASET_MODEL.PROJECT_ID, projectId)
					.set(SCOPE_MODEL_DATASET_MODEL.SCOPE_MODEL_ID, scopeModelId)
					.set(SCOPE_MODEL_DATASET_MODEL.DATASET_MODEL_ID, datasetId)
					.execute();
			}
		}

		if(dto.getFormModelIds() != null) {
			for(final var formId : dto.getFormModelIds()) {
				dslContext.insertInto(SCOPE_MODEL_FORM_MODEL)
					.set(SCOPE_MODEL_FORM_MODEL.PROJECT_ID, projectId)
					.set(SCOPE_MODEL_FORM_MODEL.SCOPE_MODEL_ID, scopeModelId)
					.set(SCOPE_MODEL_FORM_MODEL.FORM_MODEL_ID, formId)
					.execute();
			}
		}

		if(dto.getWorkflowIds() != null) {
			for(final var workflowId : dto.getWorkflowIds()) {
				dslContext.insertInto(SCOPE_MODEL_WORKFLOW)
					.set(SCOPE_MODEL_WORKFLOW.PROJECT_ID, projectId)
					.set(SCOPE_MODEL_WORKFLOW.SCOPE_MODEL_ID, scopeModelId)
					.set(SCOPE_MODEL_WORKFLOW.WORKFLOW_ID, workflowId)
					.execute();
			}
		}

		if(dto.getWorkflowStateIds() != null) {
			for(final var workflowStateId : dto.getWorkflowStateIds()) {
				final var workflowId = dslContext.select(WORKFLOW_STATE.WORKFLOW_ID)
					.from(WORKFLOW_STATE)
					.where(WORKFLOW_STATE.WORKFLOW_STATE_ID.eq(workflowStateId))
					.fetchOneInto(UUID.class);
				dslContext.insertInto(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR)
					.set(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.PROJECT_ID, projectId)
					.set(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.SCOPE_MODEL_ID, scopeModelId)
					.set(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.WORKFLOW_ID, workflowId)
					.set(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.WORKFLOW_STATE_ID, workflowStateId)
					.execute();
			}
		}
	}

	private ScopeModelDTO mapToDTO(
		final ScopeModelRecord record,
		final Map<UUID, List<UUID>> parentMap,
		final Map<UUID, List<UUID>> childMap,
		final Map<UUID, List<UUID>> datasetMap,
		final Map<UUID, List<UUID>> formMap,
		final Map<UUID, List<UUID>> workflowMap,
		final Map<UUID, List<UUID>> workflowStateMap
	) {
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
		dto.setExpectedNumber(record.getExpectedNumber());
		dto.setMaxNumber(record.getMaxNumber());
		dto.setScopeFormat(record.getScopeFormat());
		dto.setDefaultParentId(record.getDefaultParentId());
		dto.setDefaultProfileId(record.getDefaultProfileId());

		final var scopeModelId = record.getScopeModelId();
		dto.setParentIds(parentMap.getOrDefault(scopeModelId, List.of()));
		dto.setChildScopeModelIds(childMap.getOrDefault(scopeModelId, List.of()));
		dto.setDatasetModelIds(datasetMap.getOrDefault(scopeModelId, List.of()));
		dto.setFormModelIds(formMap.getOrDefault(scopeModelId, List.of()));
		dto.setWorkflowIds(workflowMap.getOrDefault(scopeModelId, List.of()));
		dto.setWorkflowStateIds(workflowStateMap.getOrDefault(scopeModelId, List.of()));

		return dto;
	}

	private Map<UUID, List<UUID>> loadParentIds(final UUID projectId, final List<UUID> scopeModelIds) {
		return fetchGroupedIds(
			projectId,
			scopeModelIds,
			SCOPE_MODEL_PARENT,
			SCOPE_MODEL_PARENT.PROJECT_ID,
			SCOPE_MODEL_PARENT.CHILD_SCOPE_MODEL_ID,
			SCOPE_MODEL_PARENT.PARENT_SCOPE_MODEL_ID
		);
	}

	private Map<UUID, List<UUID>> loadChildIds(final UUID projectId, final List<UUID> scopeModelIds) {
		return fetchGroupedIds(
			projectId,
			scopeModelIds,
			SCOPE_MODEL_PARENT,
			SCOPE_MODEL_PARENT.PROJECT_ID,
			SCOPE_MODEL_PARENT.PARENT_SCOPE_MODEL_ID,
			SCOPE_MODEL_PARENT.CHILD_SCOPE_MODEL_ID
		);
	}

	private Map<UUID, List<UUID>> loadDatasetModelIds(final UUID projectId, final List<UUID> scopeModelIds) {
		return fetchGroupedIds(
			projectId,
			scopeModelIds,
			SCOPE_MODEL_DATASET_MODEL,
			SCOPE_MODEL_DATASET_MODEL.PROJECT_ID,
			SCOPE_MODEL_DATASET_MODEL.SCOPE_MODEL_ID,
			SCOPE_MODEL_DATASET_MODEL.DATASET_MODEL_ID
		);
	}

	private Map<UUID, List<UUID>> loadFormModelIds(final UUID projectId, final List<UUID> scopeModelIds) {
		return fetchGroupedIds(
			projectId,
			scopeModelIds,
			SCOPE_MODEL_FORM_MODEL,
			SCOPE_MODEL_FORM_MODEL.PROJECT_ID,
			SCOPE_MODEL_FORM_MODEL.SCOPE_MODEL_ID,
			SCOPE_MODEL_FORM_MODEL.FORM_MODEL_ID
		);
	}

	private Map<UUID, List<UUID>> loadWorkflowIds(final UUID projectId, final List<UUID> scopeModelIds) {
		return fetchGroupedIds(
			projectId,
			scopeModelIds,
			SCOPE_MODEL_WORKFLOW,
			SCOPE_MODEL_WORKFLOW.PROJECT_ID,
			SCOPE_MODEL_WORKFLOW.SCOPE_MODEL_ID,
			SCOPE_MODEL_WORKFLOW.WORKFLOW_ID
		);
	}

	private Map<UUID, List<UUID>> loadWorkflowStateIds(final UUID projectId, final List<UUID> scopeModelIds) {
		return fetchGroupedIds(
			projectId, scopeModelIds,
			SCOPE_MODEL_WORKFLOW_STATE_SELECTOR,
			SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.PROJECT_ID,
			SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.SCOPE_MODEL_ID,
			SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.WORKFLOW_STATE_ID
		);
	}

	private <R extends Record, T extends Table<R>> Map<UUID, List<UUID>> fetchGroupedIds(
		final UUID projectId,
		final List<UUID> scopeModelIds,
		final T table,
		final TableField<R, UUID> projectIdField,
		final TableField<R, UUID> scopeModelIdField,
		final TableField<R, UUID> valueField
	) {
		if(scopeModelIds == null || scopeModelIds.isEmpty()) {
			return Map.of();
		}

		final var rows = dslContext
			.select(scopeModelIdField, valueField)
			.from(table)
			.where(projectIdField.eq(projectId))
			.and(scopeModelIdField.in(scopeModelIds))
			.fetch();

		final Map<UUID, List<UUID>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2());
		}

		result.replaceAll((_, v) -> {
			final var copy = new ArrayList<>(v);
			Collections.sort(copy);
			return copy;
		});

		return result;
	}
}
