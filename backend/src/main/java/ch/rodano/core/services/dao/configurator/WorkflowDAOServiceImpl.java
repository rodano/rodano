package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.workflow.WorkflowDTO;
import ch.rodano.core.model.jooq.tables.records.WorkflowRecord;

import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;
import static ch.rodano.core.model.jooq.tables.WorkflowAction.WORKFLOW_ACTION;
import static ch.rodano.core.model.jooq.tables.WorkflowState.WORKFLOW_STATE;
import static ch.rodano.core.model.jooq.tables.WorkflowStatePossibleAction.WORKFLOW_STATE_POSSIBLE_ACTION;
import static ch.rodano.core.model.jooq.tables.WorkflowStatus.WORKFLOW_STATUS;

@Repository
public class WorkflowDAOServiceImpl implements WorkflowDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public WorkflowDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "workflows", key = "#projectId.toString()")
	public List<WorkflowDTO> getWorkflows(final UUID projectId) {
		final var workflowRecords = dslContext
			.selectFrom(WORKFLOW)
			.where(WORKFLOW.PROJECT_ID.eq(projectId))
			.orderBy(WORKFLOW.ORDER_BY.asc(), WORKFLOW.CODE.asc())
			.fetch();

		if(workflowRecords.isEmpty()) {
			return List.of();
		}

		return workflowRecords.map(this::mapToDTO);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "workflow", key = "#projectId.toString() + ':' + #workflowId.toString()")
	public WorkflowDTO getWorkflow(final UUID projectId, final UUID workflowId) {
		final var record = dslContext
			.selectFrom(WORKFLOW)
			.where(WORKFLOW.PROJECT_ID.eq(projectId))
			.and(WORKFLOW.WORKFLOW_ID.eq(workflowId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		return mapToDTO(record);
	}

	@Override
	@Transactional
	@Caching(evict = { @CacheEvict(value = "workflows", key = "#projectId.toString()") })
	public WorkflowDTO createWorkflow(final UUID projectId, final WorkflowDTO dto) {
		final var workflowId = dto.getWorkflowId() != null ? dto.getWorkflowId() : UUID.randomUUID();

		dslContext.insertInto(WORKFLOW)
			.set(WORKFLOW.WORKFLOW_ID, workflowId)
			.set(WORKFLOW.PROJECT_ID, projectId)
			.set(WORKFLOW.CODE, dto.getId())
			.set(WORKFLOW.AGGREGATE_WORKFLOW_ID, dto.getAggregatedWorkflowId())
			.set(WORKFLOW.INITIAL_STATE_ID, dto.getInitialStateId())
			.set(WORKFLOW.CREATION_ACTION_ID, dto.getActionId())
			.set(WORKFLOW.ORDER_BY, dto.getOrder())
			.set(WORKFLOW.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(WORKFLOW.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(WORKFLOW.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(WORKFLOW.MESSAGE, jsonMapperService.toJson(dto.getMessage()))
			.set(WORKFLOW.MANDATORY, dto.isMandatory())
			.set(WORKFLOW.IS_UNIQUE, dto.isUnique())
			.set(WORKFLOW.ICON, dto.getIcon())
			.execute();

		return getWorkflow(projectId, workflowId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "workflows", key = "#projectId.toString()"),
		@CacheEvict(value = "workflow", key = "#projectId.toString() + ':' + #workflowId.toString()")
	})
	public WorkflowDTO updateWorkflow(final UUID projectId, final UUID workflowId, final WorkflowDTO dto) {
		dslContext.update(WORKFLOW)
			.set(WORKFLOW.CODE, dto.getId())
			.set(WORKFLOW.AGGREGATE_WORKFLOW_ID, dto.getAggregatedWorkflowId())
			.set(WORKFLOW.INITIAL_STATE_ID, dto.getInitialStateId())
			.set(WORKFLOW.CREATION_ACTION_ID, dto.getActionId())
			.set(WORKFLOW.ORDER_BY, dto.getOrder())
			.set(WORKFLOW.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(WORKFLOW.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(WORKFLOW.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(WORKFLOW.MESSAGE, jsonMapperService.toJson(dto.getMessage()))
			.set(WORKFLOW.MANDATORY, dto.isMandatory())
			.set(WORKFLOW.IS_UNIQUE, dto.isUnique())
			.set(WORKFLOW.ICON, dto.getIcon())
			.where(WORKFLOW.PROJECT_ID.eq(projectId))
			.and(WORKFLOW.WORKFLOW_ID.eq(workflowId))
			.execute();

		return getWorkflow(projectId, workflowId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "workflows", key = "#projectId.toString()"),
		@CacheEvict(value = "workflow", key = "#projectId.toString() + ':' + #workflowId.toString()")
	})
	public void deleteWorkflow(final UUID projectId, final UUID workflowId) {
		final var workflowStateIds = dslContext
			.select(WORKFLOW_STATE.WORKFLOW_STATE_ID).from(WORKFLOW_STATE)
			.where(WORKFLOW_STATE.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId));

		dslContext.deleteFrom(WORKFLOW_STATE_POSSIBLE_ACTION).where(WORKFLOW_STATE_POSSIBLE_ACTION.PROJECT_ID.eq(projectId)).and(WORKFLOW_STATE_POSSIBLE_ACTION.WORKFLOW_STATE_ID.in(workflowStateIds)).execute();
		dslContext.deleteFrom(WORKFLOW_STATE).where(WORKFLOW_STATE.PROJECT_ID.eq(projectId)).and(WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId)).execute();
		dslContext.deleteFrom(WORKFLOW_ACTION).where(WORKFLOW_ACTION.PROJECT_ID.eq(projectId)).and(WORKFLOW_ACTION.WORKFLOW_ID.eq(workflowId)).execute();

		dslContext.deleteFrom(WORKFLOW).where(WORKFLOW.PROJECT_ID.eq(projectId)).and(WORKFLOW.WORKFLOW_ID.eq(workflowId)).execute();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasPatientData(final UUID projectId, final UUID workflowId) {
		return dslContext.fetchExists(
			dslContext.selectOne()
				.from(WORKFLOW_STATUS)
				.where(WORKFLOW_STATUS.PROJECT_ID.eq(projectId))
				.and(WORKFLOW_STATUS.WORKFLOW_ID.eq(workflowId))
		);
	}

	private WorkflowDTO mapToDTO(final WorkflowRecord record) {
		final var dto = new WorkflowDTO();
		dto.setWorkflowId(record.getWorkflowId());
		dto.setId(record.getCode());
		dto.setOrder(record.getOrderBy());
		dto.setAggregatedWorkflowId(record.getAggregateWorkflowId());
		dto.setInitialStateId(record.getInitialStateId());
		dto.setActionId(record.getCreationActionId());
		dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setLongname(jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setMessage(jsonMapperService.fromJson(record.getMessage(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setMandatory(record.getMandatory());
		dto.setUnique(record.getIsUnique());
		dto.setIcon(record.getIcon());

		return dto;
	}
}
