package ch.rodano.core.dao;


import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.jooq.enums.RuleEntityType;
import ch.rodano.core.model.jooq.tables.records.ScopeModelRecord;

import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.FormModel.FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.Profile.PROFILE;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelDatasetModel.SCOPE_MODEL_DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelFormModel.SCOPE_MODEL_FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelParent.SCOPE_MODEL_PARENT;
import static ch.rodano.core.model.jooq.tables.ScopeModelWorkflow.SCOPE_MODEL_WORKFLOW;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;

@Repository
public class ScopeModelDAO implements BaseProjectDAO<ScopeModel> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final EventModelDAO eventModelDAO;
	private final EventGroupDAO eventGroupDAO;
	private final WorkflowStatesSelectorDAO workflowStatesSelectorDAO;
	private final RuleDAO ruleDAO;

	public ScopeModelDAO(final DSLContext dslContext,
						 final MappingHelper mappingHelper,
						 final EventModelDAO eventModelDAO,
						 final EventGroupDAO eventGroupDAO,
						 final WorkflowStatesSelectorDAO workflowStatesSelectorDAO,
						 final RuleDAO ruleDAO) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.eventModelDAO = eventModelDAO;
		this.eventGroupDAO = eventGroupDAO;
		this.workflowStatesSelectorDAO = workflowStatesSelectorDAO;
		this.ruleDAO = ruleDAO;
	}

	@Override
	public List<ScopeModel> findByProject(final UUID projectId) {
		return dslContext.selectFrom(SCOPE_MODEL)
			.where(SCOPE_MODEL.PROJECT_ID.eq(projectId))
			.fetch(this::mapToModel);
	}

	@Override
	public ScopeModel findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(SCOPE_MODEL)
			.where(SCOPE_MODEL.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public ScopeModel findById(final UUID id) {
		return dslContext.selectFrom(SCOPE_MODEL)
			.where(SCOPE_MODEL.SCOPE_MODEL_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public ScopeModel save(final ScopeModel entity) {
		return null;
	}

	@Override
	public void delete(final UUID id) {
	}

	private ScopeModel mapToModel(final ScopeModelRecord record) {
		if(record == null) {
			return null;
		}

		final ScopeModel model = new ScopeModel();

		model.setScopeModelId(record.getScopeModelId());
		model.setId(record.getCode());

		model.setVirtual(record.getVirtual());
		model.setMaxNumber(record.getMaxNumber());
		model.setScopeFormat(record.getScopeFormat());

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));
		model.setPluralShortname(mappingHelper.parseJsonToMap(record.getPluralShortname()));

		if(record.getDefaultProfileId() != null) {
			final String profileCode = getProfileCode(record.getDefaultProfileId());
			model.setDefaultProfileId(profileCode);
		}

		model.setParentIds(loadParentIds(record.getScopeModelId()));
		model.setDatasetModelIds(loadDatasetModelIds(record.getScopeModelId()));
		model.setFormModelIds(loadFormModelIds(record.getScopeModelId()));
		model.setWorkflowIds(loadWorkflowIds(record.getScopeModelId()));

		model.setEventModels(eventModelDAO.findByScopeModel(record.getScopeModelId()));
		model.setEventGroups(eventGroupDAO.findByScopeModel(record.getScopeModelId()));
		model.setWorkflowStatesSelectors(workflowStatesSelectorDAO.findByScopeModel(record.getScopeModelId()));

		model.setCreateRules(ruleDAO.findByEntityAndType(RuleEntityType.SCOPE_MODEL, record.getScopeModelId(), "CREATE"));
		model.setRemoveRules(ruleDAO.findByEntityAndType(RuleEntityType.SCOPE_MODEL, record.getScopeModelId(), "REMOVE"));
		model.setRestoreRules(ruleDAO.findByEntityAndType(RuleEntityType.SCOPE_MODEL, record.getScopeModelId(), "RESTORE"));

		return model;
	}

	private String getProfileCode(final UUID profileId) {
		return dslContext.select(PROFILE.CODE)
			.from(PROFILE)
			.where(PROFILE.PROFILE_ID.eq(profileId))
			.fetchOne(PROFILE.CODE);
	}

	private List<String> loadParentIds(final UUID scopeModelId) {
		return dslContext.select(SCOPE_MODEL.CODE)
			.from(SCOPE_MODEL_PARENT)
			.join(SCOPE_MODEL).on(SCOPE_MODEL.SCOPE_MODEL_ID.eq(SCOPE_MODEL_PARENT.PARENT_SCOPE_MODEL_ID))
			.where(SCOPE_MODEL_PARENT.CHILD_SCOPE_MODEL_ID.eq(scopeModelId))
			.fetch(SCOPE_MODEL.CODE);
	}

	private List<String> loadDatasetModelIds(final UUID scopeModelId) {
		return dslContext.select(DATASET_MODEL.CODE)
			.from(SCOPE_MODEL_DATASET_MODEL)
			.join(DATASET_MODEL).on(DATASET_MODEL.DATASET_MODEL_ID.eq(SCOPE_MODEL_DATASET_MODEL.DATASET_MODEL_ID))
			.where(SCOPE_MODEL_DATASET_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetch(DATASET_MODEL.CODE);
	}

	private List<String> loadFormModelIds(final UUID scopeModelId) {
		return dslContext.select(FORM_MODEL.CODE)
			.from(SCOPE_MODEL_FORM_MODEL)
			.join(FORM_MODEL).on(FORM_MODEL.FORM_MODEL_ID.eq(SCOPE_MODEL_FORM_MODEL.FORM_MODEL_ID))
			.where(SCOPE_MODEL_FORM_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetch(FORM_MODEL.CODE);
	}

	private List<String> loadWorkflowIds(final UUID scopeModelId) {
		return dslContext.select(WORKFLOW.CODE)
			.from(SCOPE_MODEL_WORKFLOW)
			.join(WORKFLOW).on(WORKFLOW.WORKFLOW_ID.eq(SCOPE_MODEL_WORKFLOW.WORKFLOW_ID))
			.where(SCOPE_MODEL_WORKFLOW.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetch(WORKFLOW.CODE);
	}
}
