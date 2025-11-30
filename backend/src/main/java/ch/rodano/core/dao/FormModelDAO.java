package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.form.FormModel;
import ch.rodano.core.model.jooq.enums.RuleEntityType;
import ch.rodano.core.model.jooq.tables.records.FormModelRecord;

import static ch.rodano.core.model.jooq.tables.FormModel.FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.FormModelWorkflow.FORM_MODEL_WORKFLOW;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;

@Repository
public class FormModelDAO implements BaseProjectDAO<FormModel> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final LayoutDAO layoutDAO;
	private final RuleDAO ruleDAO;

	public FormModelDAO(final DSLContext dslContext, final MappingHelper mappingHelper, final LayoutDAO layoutDAO, final RuleDAO ruleDAO) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.layoutDAO = layoutDAO;
		this.ruleDAO = ruleDAO;
	}

	@Override
	public List<FormModel> findByProject(final UUID projectId) {
		return dslContext.selectFrom(FORM_MODEL)
			.where(FORM_MODEL.PROJECT_ID.eq(projectId))
			.fetch(this::mapToModel);
	}

	@Override
	public FormModel findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(FORM_MODEL)
			.where(FORM_MODEL.PROJECT_ID.eq(projectId))
			.and(FORM_MODEL.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public FormModel findById(final UUID id) {
		return dslContext.selectFrom(FORM_MODEL)
			.where(FORM_MODEL.FORM_MODEL_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public FormModel save(final FormModel entity) {
		return null;
	}

	@Override
	public void delete(final UUID id) {

	}

	private FormModel mapToModel(final FormModelRecord record) {
		if(record == null) {
			return null;
		}

		final FormModel model = new FormModel();

		model.setFormModelId(record.getFormModelId());
		model.setId(record.getCode());
		model.setOptional(record.getOptional() != null ? record.getOptional() : false);

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));
		model.setPrintButtonLabel(mappingHelper.parseJsonToMap(record.getPrintButtonLabel()));

		final var layouts = layoutDAO.findByFormModel(record.getFormModelId());

		for(final var layout : layouts) {
			layout.setFormModel(model);
		}

		model.setLayouts(layouts);

		model.setWorkflowIds(loadWorkflowIds(record.getFormModelId()));

		model.setRules(ruleDAO.findByEntity(RuleEntityType.FORM_MODEL, record.getFormModelId()));

		return model;
	}

	private List<String> loadWorkflowIds(final UUID formModelId) {
		return dslContext.select(WORKFLOW.CODE)
			.from(FORM_MODEL_WORKFLOW)
			.join(WORKFLOW).on(WORKFLOW.WORKFLOW_ID.eq(FORM_MODEL_WORKFLOW.WORKFLOW_ID))
			.where(FORM_MODEL_WORKFLOW.FORM_MODEL_ID.eq(formModelId))
			.fetch(WORKFLOW.CODE);
	}
}
