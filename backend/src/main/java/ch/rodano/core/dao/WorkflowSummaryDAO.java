package ch.rodano.core.dao;

import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.reports.WorkflowSummary;
import ch.rodano.configuration.model.workflow.WorkflowableEntity;
import ch.rodano.core.model.jooq.tables.records.WorkflowSummaryRecord;

import static ch.rodano.core.model.jooq.tables.EventModel.EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;
import static ch.rodano.core.model.jooq.tables.WorkflowSummary.WORKFLOW_SUMMARY;
import static ch.rodano.core.model.jooq.tables.WorkflowSummaryFilterEventModel.WORKFLOW_SUMMARY_FILTER_EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.WorkflowSummaryWorkflow.WORKFLOW_SUMMARY_WORKFLOW;

@Repository
public class WorkflowSummaryDAO implements BaseProjectDAO<WorkflowSummary> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final WorkflowSummaryColumnDAO workflowSummaryColumnDAO;

	public WorkflowSummaryDAO(final DSLContext dslContext,
							  final MappingHelper mappingHelper,
							  final WorkflowSummaryColumnDAO workflowSummaryColumnDAO) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.workflowSummaryColumnDAO = workflowSummaryColumnDAO;
	}

	@Override
	public List<WorkflowSummary> findByProject(final UUID projectId) {
		return dslContext.selectFrom(WORKFLOW_SUMMARY)
			.where(WORKFLOW_SUMMARY.PROJECT_ID.eq(projectId))
			.fetch(this::mapToModel);
	}

	@Override
	public WorkflowSummary findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(WORKFLOW_SUMMARY)
			.where(WORKFLOW_SUMMARY.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_SUMMARY.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public WorkflowSummary findById(final UUID id) {
		return dslContext.selectFrom(WORKFLOW_SUMMARY)
			.where(WORKFLOW_SUMMARY.WORKFLOW_SUMMARY_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public WorkflowSummary save(final WorkflowSummary entity) {
		return null;
	}

	@Override
	public void delete(final UUID id) {

	}

	private WorkflowSummary mapToModel(final WorkflowSummaryRecord record) {
		if(record == null) {
			return null;
		}

		final WorkflowSummary model = new WorkflowSummary();

		model.setId(record.getCode());
		model.setWorkflowSummaryId(record.getWorkflowSummaryId());

		model.setWorkflowEntity(mappingHelper.parseEnum(WorkflowableEntity.class, record.getWorkflowEntity(), "workflowEntity"));

		model.setFilterExpectedEvents(record.getFilterExpectedEvents() != null ? record.getFilterExpectedEvents() : false);
		model.setDisplayLegend(record.getDisplayLegend() != null ? record.getDisplayLegend() : false);
		model.setDisplayColumnExport(record.getDisplayColumnExport() != null ? record.getDisplayColumnExport() : false);

		model.setTitle(mappingHelper.parseJsonToMap(record.getTitle()));

		if(record.getLeafScopeModelId() != null) {
			model.setLeafScopeModelId(getScopeModelCode(record.getLeafScopeModelId()));
		}

		model.setWorkflowIds(loadWorkflowIds(record.getWorkflowSummaryId()));

		model.setFilterEventModelIds(new TreeSet<>(loadFilterEventModelIds(record.getWorkflowSummaryId())));

		model.setColumns(workflowSummaryColumnDAO.findByWorkflowSummary(record.getWorkflowSummaryId()));

		return model;
	}

	private List<String> loadWorkflowIds(final UUID summaryId) {
		return dslContext.select(WORKFLOW.CODE)
			.from(WORKFLOW_SUMMARY_WORKFLOW)
			.join(WORKFLOW).on(WORKFLOW.WORKFLOW_ID.eq(WORKFLOW_SUMMARY_WORKFLOW.WORKFLOW_ID))
			.where(WORKFLOW_SUMMARY_WORKFLOW.WORKFLOW_SUMMARY_ID.eq(summaryId))
			.fetch(WORKFLOW.CODE);
	}

	private List<String> loadFilterEventModelIds(final UUID summaryId) {
		return dslContext.select(EVENT_MODEL.CODE)
			.from(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL)
			.join(EVENT_MODEL).on(EVENT_MODEL.EVENT_MODEL_ID.eq(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL.EVENT_MODEL_ID))
			.where(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL.WORKFLOW_SUMMARY_ID.eq(summaryId))
			.fetch(EVENT_MODEL.CODE);
	}

	private String getScopeModelCode(final UUID scopeModelId) {
		return dslContext.select(SCOPE_MODEL.CODE)
			.from(SCOPE_MODEL)
			.where(SCOPE_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetchOne(SCOPE_MODEL.CODE);
	}
}
