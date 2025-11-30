package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.reports.WorkflowWidget;
import ch.rodano.configuration.model.workflow.WorkflowableEntity;
import ch.rodano.core.model.jooq.tables.records.WorkflowWidgetRecord;

import static ch.rodano.core.model.jooq.tables.WorkflowWidget.WORKFLOW_WIDGET;

@Repository
public class WorkflowWidgetDAO implements BaseProjectDAO<WorkflowWidget> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final WorkflowWidgetColumnDAO workflowWidgetColumnDAO;
	private final WorkflowStatesSelectorDAO workflowStatesSelectorDAO;

	public WorkflowWidgetDAO(final DSLContext dslContext,
							 final MappingHelper mappingHelper,
							 final WorkflowWidgetColumnDAO workflowWidgetColumnDAO,
							 final WorkflowStatesSelectorDAO workflowStatesSelectorDAO) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.workflowWidgetColumnDAO = workflowWidgetColumnDAO;
		this.workflowStatesSelectorDAO = workflowStatesSelectorDAO;
	}

	@Override
	public List<WorkflowWidget> findByProject(final UUID projectId) {
		return dslContext.selectFrom(WORKFLOW_WIDGET)
			.where(WORKFLOW_WIDGET.PROJECT_ID.eq(projectId))
			.fetch(this::mapToModel);
	}

	@Override
	public WorkflowWidget findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(WORKFLOW_WIDGET)
			.where(WORKFLOW_WIDGET.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_WIDGET.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public WorkflowWidget findById(final UUID id) {
		return dslContext.selectFrom(WORKFLOW_WIDGET)
			.where(WORKFLOW_WIDGET.WORKFLOW_WIDGET_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public WorkflowWidget save(final WorkflowWidget entity) {
		return null;
	}

	@Override
	public void delete(final UUID id) {

	}

	private WorkflowWidget mapToModel(final WorkflowWidgetRecord record) {
		if(record == null) {
			return null;
		}

		final WorkflowWidget model = new WorkflowWidget();

		model.setWorkflowWidgetId(record.getWorkflowWidgetId());
		model.setId(record.getCode());

		model.setFilterExpectedEvents(record.getFilterExpectedEvents() != null ? record.getFilterExpectedEvents() : false);

		model.setWorkflowEntity(mappingHelper.parseEnum(WorkflowableEntity.class, record.getWorkflowEntity(), "workflowEntity"));

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

		model.setWorkflowStatesSelectors(workflowStatesSelectorDAO.findByWorkflowWidget(record.getWorkflowWidgetId()));

		model.setColumns(workflowWidgetColumnDAO.findByWorkflowWidget(record.getWorkflowWidgetId()));

		return model;
	}
}
