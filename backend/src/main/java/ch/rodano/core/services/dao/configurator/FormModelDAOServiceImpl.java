package ch.rodano.core.services.dao.configurator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.config.FormModelDTO;
import ch.rodano.core.model.jooq.tables.records.FormModelRecord;

import static ch.rodano.core.model.jooq.tables.FormModel.FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.FormModelWorkflow.FORM_MODEL_WORKFLOW;

@Repository
public class FormModelDAOServiceImpl implements FormModelDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public FormModelDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "formModels", key = "#projectId.toString()")
	public List<FormModelDTO> getFormModels(final UUID projectId) {
		final var records = dslContext
			.selectFrom(FORM_MODEL)
			.where(FORM_MODEL.PROJECT_ID.eq(projectId))
			.orderBy(FORM_MODEL.CODE)
			.fetch();

		if(records.isEmpty()) {
			return List.of();
		}

		final var formModelIds = records.map(FormModelRecord::getFormModelId);
		final var workflowMap = loadWorkflowIds(projectId, formModelIds);

		return records.map(record -> mapToDTO(record, workflowMap));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "formModel", key = "#projectId.toString() + ':' + #formModelId.toString()")
	public FormModelDTO getFormModel(final UUID projectId, final UUID formModelId) {
		final var record = dslContext
			.selectFrom(FORM_MODEL)
			.where(FORM_MODEL.PROJECT_ID.eq(projectId))
			.and(FORM_MODEL.FORM_MODEL_ID.eq(formModelId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		final var workflowMap = loadWorkflowIds(projectId, List.of(formModelId));

		return mapToDTO(record, workflowMap);
	}

	@Override
	@Transactional
	@CacheEvict(value = "formModels", key = "#projectId.toString()")
	public FormModelDTO createFormModel(final UUID projectId, final FormModelDTO dto) {
		final var formModelId = dto.getFormModelId() != null ? dto.getFormModelId() : UUID.randomUUID();

		dslContext.insertInto(FORM_MODEL)
			.set(FORM_MODEL.FORM_MODEL_ID, formModelId)
			.set(FORM_MODEL.PROJECT_ID, projectId)
			.set(FORM_MODEL.CODE, dto.getId())
			.set(FORM_MODEL.OPTIONAL, dto.isOptional())
			.set(FORM_MODEL.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(FORM_MODEL.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(FORM_MODEL.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(FORM_MODEL.PRINT_BUTTON_LABEL, jsonMapperService.toJson(dto.getPrintButtonLabel()))
			.execute();

		replaceWorkflows(projectId, formModelId, dto);

		return getFormModel(projectId, formModelId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "formModels", key = "#projectId.toString()"),
		@CacheEvict(value = "formModel", key = "#projectId.toString() + ':' + #formModelId.toString()")
	})
	public FormModelDTO updateFormModel(final UUID projectId, final UUID formModelId, final FormModelDTO dto) {
		dslContext.update(FORM_MODEL)
			.set(FORM_MODEL.CODE, dto.getId())
			.set(FORM_MODEL.OPTIONAL, dto.isOptional())
			.set(FORM_MODEL.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(FORM_MODEL.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(FORM_MODEL.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(FORM_MODEL.PRINT_BUTTON_LABEL, jsonMapperService.toJson(dto.getPrintButtonLabel()))
			.where(FORM_MODEL.PROJECT_ID.eq(projectId))
			.and(FORM_MODEL.FORM_MODEL_ID.eq(formModelId))
			.execute();

		replaceWorkflows(projectId, formModelId, dto);

		return getFormModel(projectId, formModelId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "formModels", key = "#projectId.toString()"),
		@CacheEvict(value = "formModel", key = "#projectId.toString() + ':' + #formModelId.toString()")
	})
	public void deleteFormModel(final UUID projectId, final UUID formModelId) {
		dslContext.deleteFrom(FORM_MODEL_WORKFLOW)
			.where(FORM_MODEL_WORKFLOW.PROJECT_ID.eq(projectId))
			.and(FORM_MODEL_WORKFLOW.FORM_MODEL_ID.eq(formModelId))
			.execute();

		dslContext.deleteFrom(FORM_MODEL)
			.where(FORM_MODEL.PROJECT_ID.eq(projectId))
			.and(FORM_MODEL.FORM_MODEL_ID.eq(formModelId))
			.execute();
	}

	private void replaceWorkflows(final UUID projectId, final UUID formModelId, final FormModelDTO dto) {
		dslContext.deleteFrom(FORM_MODEL_WORKFLOW)
			.where(FORM_MODEL_WORKFLOW.PROJECT_ID.eq(projectId))
			.and(FORM_MODEL_WORKFLOW.FORM_MODEL_ID.eq(formModelId))
			.execute();

		if(dto.getWorkflowIds() != null) {
			for(final var workflowId : dto.getWorkflowIds()) {
				dslContext.insertInto(FORM_MODEL_WORKFLOW)
					.set(FORM_MODEL_WORKFLOW.PROJECT_ID, projectId)
					.set(FORM_MODEL_WORKFLOW.FORM_MODEL_ID, formModelId)
					.set(FORM_MODEL_WORKFLOW.WORKFLOW_ID, workflowId)
					.execute();
			}
		}
	}

	private FormModelDTO mapToDTO(final FormModelRecord record, final Map<UUID, List<UUID>> workflowMap) {
		final var dto = new FormModelDTO();
		dto.setFormModelId(record.getFormModelId());
		dto.setId(record.getCode());
		dto.setOptional(record.getOptional());
		dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setLongname(jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setPrintButtonLabel(jsonMapperService.fromJson(record.getPrintButtonLabel(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setWorkflowIds(workflowMap.getOrDefault(record.getFormModelId(), List.of()));
		return dto;
	}

	private Map<UUID, List<UUID>> loadWorkflowIds(final UUID projectId, final List<UUID> formModelIds) {
		if(formModelIds == null || formModelIds.isEmpty()) {
			return Map.of();
		}

		final var rows = dslContext
			.select(FORM_MODEL_WORKFLOW.FORM_MODEL_ID, FORM_MODEL_WORKFLOW.WORKFLOW_ID)
			.from(FORM_MODEL_WORKFLOW)
			.where(FORM_MODEL_WORKFLOW.PROJECT_ID.eq(projectId))
			.and(FORM_MODEL_WORKFLOW.FORM_MODEL_ID.in(formModelIds))
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
