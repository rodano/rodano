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

import ch.rodano.api.workflow.WorkflowActionDTO;
import ch.rodano.core.model.jooq.tables.records.WorkflowActionRecord;

import static ch.rodano.core.model.jooq.tables.WorkflowAction.WORKFLOW_ACTION;

@Repository
public class WorkflowActionDAOServiceImpl implements WorkflowActionDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public WorkflowActionDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "workflowActions", key = "#projectId.toString()")
	public List<WorkflowActionDTO> getWorkflowActions(final UUID projectId) {
		final var workflowActions = dslContext.selectFrom(WORKFLOW_ACTION)
			.where(WORKFLOW_ACTION.PROJECT_ID.eq(projectId))
			.orderBy(WORKFLOW_ACTION.CODE)
			.fetch();

		return workflowActions.stream()
			.map(this::mapToDTO)
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "workflowAction", key = "#projectId.toString() + ':' + #workflowActionId.toString()")
	public WorkflowActionDTO getWorkflowAction(final UUID projectId, final UUID workflowActionId) {
		final var record = dslContext.selectFrom(WORKFLOW_ACTION)
			.where(WORKFLOW_ACTION.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_ACTION.WORKFLOW_ACTION_ID.eq(workflowActionId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		return mapToDTO(record);
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkflowActionDTO> getWorkflowActionsByIds(final UUID projectId, final List<UUID> workflowActionIds) {
		if(workflowActionIds == null || workflowActionIds.isEmpty()) {
			return List.of();
		}

		final var records = dslContext.selectFrom(WORKFLOW_ACTION)
			.where(WORKFLOW_ACTION.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_ACTION.WORKFLOW_ACTION_ID.in(workflowActionIds))
			.orderBy(WORKFLOW_ACTION.CODE)
			.fetch();

		return records.stream()
			.map(this::mapToDTO)
			.collect(Collectors.toList());
	}

	@Override
	@Transactional
	@Caching(evict = { @CacheEvict(value = "workflowActions", key = "#projectId.toString()") })
	public WorkflowActionDTO createWorkflowAction(final UUID projectId, final WorkflowActionDTO workflowAction) {
		final var workflowActionId = workflowAction.getWorkflowActionId()
			!= null ? workflowAction.getWorkflowActionId()
			: UUID.randomUUID();

		dslContext.insertInto(WORKFLOW_ACTION)
			.set(WORKFLOW_ACTION.WORKFLOW_ACTION_ID, workflowActionId)
			.set(WORKFLOW_ACTION.PROJECT_ID, projectId)
			.set(WORKFLOW_ACTION.CODE, workflowAction.getId())
			.set(WORKFLOW_ACTION.WORKFLOW_ID, workflowAction.getWorkflowId())
			.set(WORKFLOW_ACTION.SHORTNAME, jsonMapperService.toJson(workflowAction.getShortname()))
			.set(WORKFLOW_ACTION.LONGNAME, jsonMapperService.toJson(workflowAction.getLongname()))
			.set(WORKFLOW_ACTION.DESCRIPTION, jsonMapperService.toJson(workflowAction.getDescription()))
			.set(WORKFLOW_ACTION.DOCUMENTABLE, workflowAction.isDocumentable())
			.set(WORKFLOW_ACTION.DOCUMENTABLE_OPTIONS, jsonMapperService.toJson(workflowAction.getDocumentableOptions()))
			.set(WORKFLOW_ACTION.REQUIRE_SIGNATURE, workflowAction.isRequireSignature())
			.set(WORKFLOW_ACTION.REQUIRED_SIGNATURE_TEXT, jsonMapperService.toJson(workflowAction.getRequireSignatureText()))
			.execute();

		return getWorkflowAction(projectId, workflowActionId);

	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "workflowActions", key = "#projectId.toString()"),
		@CacheEvict(value = "workflowAction", key = "#projectId.toString() + ':' + #workflowActionId.toString()")
	})
	public WorkflowActionDTO updateWorkflowAction(final UUID projectId, final UUID workflowActionId, final WorkflowActionDTO workflowAction) {
		dslContext.update(WORKFLOW_ACTION)
			.set(WORKFLOW_ACTION.CODE, workflowAction.getId())
			.set(WORKFLOW_ACTION.WORKFLOW_ID, workflowAction.getWorkflowId())
			.set(WORKFLOW_ACTION.SHORTNAME, jsonMapperService.toJson(workflowAction.getShortname()))
			.set(WORKFLOW_ACTION.LONGNAME, jsonMapperService.toJson(workflowAction.getLongname()))
			.set(WORKFLOW_ACTION.DESCRIPTION, jsonMapperService.toJson(workflowAction.getDescription()))
			.set(WORKFLOW_ACTION.DOCUMENTABLE, workflowAction.isDocumentable())
			.set(WORKFLOW_ACTION.DOCUMENTABLE_OPTIONS, jsonMapperService.toJson(workflowAction.getDocumentableOptions()))
			.set(WORKFLOW_ACTION.REQUIRE_SIGNATURE, workflowAction.isRequireSignature())
			.set(WORKFLOW_ACTION.REQUIRED_SIGNATURE_TEXT, jsonMapperService.toJson(workflowAction.getRequireSignatureText()))
			.where(WORKFLOW_ACTION.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_ACTION.WORKFLOW_ACTION_ID.eq(workflowActionId))
			.execute();

		return getWorkflowAction(projectId, workflowActionId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "workflowActions", key = "#projectId.toString()"),
		@CacheEvict(value = "workflowAction", key = "#projectId.toString() + ':' + #workflowActionId.toString()")
	})
	public void deleteWorkflowAction(final UUID projectId, final UUID workflowActionId) {
		dslContext.deleteFrom(WORKFLOW_ACTION)
			.where(WORKFLOW_ACTION.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_ACTION.WORKFLOW_ACTION_ID.eq(workflowActionId))
			.execute();
	}

	private WorkflowActionDTO mapToDTO(final WorkflowActionRecord record) {
		final var dto = new WorkflowActionDTO();
		dto.setWorkflowActionId(record.getWorkflowActionId());
		dto.setId(record.getCode());
		dto.setWorkflowId(record.getWorkflowId());
		dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setLongname(jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setDocumentable(record.getDocumentable());
		dto.setDocumentableOptions(List.of(jsonMapperService.fromJson(record.getDocumentableOptions(), new TypeReference<TreeMap<String, String>>() {
		})));
		dto.setRequireSignature(record.getRequireSignature());
		dto.setRequireSignatureText(jsonMapperService.fromJson(record.getRequiredSignatureText(), new TypeReference<TreeMap<String, String>>() {
		}));

		return dto;
	}
}
