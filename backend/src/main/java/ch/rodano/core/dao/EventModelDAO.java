package ch.rodano.core.dao;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.event.DateAggregationFunction;
import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.core.model.jooq.enums.RuleEntityType;
import ch.rodano.core.model.jooq.tables.records.EventModelRecord;

import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.EventGroup.EVENT_GROUP;
import static ch.rodano.core.model.jooq.tables.EventModel.EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelBlockedEvent.EVENT_MODEL_BLOCKED_EVENT;
import static ch.rodano.core.model.jooq.tables.EventModelDatasetModel.EVENT_MODEL_DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelDeadlineReference.EVENT_MODEL_DEADLINE_REFERENCE;
import static ch.rodano.core.model.jooq.tables.EventModelFormModel.EVENT_MODEL_FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelImpliedEvent.EVENT_MODEL_IMPLIED_EVENT;
import static ch.rodano.core.model.jooq.tables.EventModelWorkflow.EVENT_MODEL_WORKFLOW;
import static ch.rodano.core.model.jooq.tables.FormModel.FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;

@Repository
public class EventModelDAO implements BaseProjectDAO<EventModel> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final RuleDAO ruleDAO;

	public EventModelDAO(final DSLContext dslContext, final MappingHelper mappingHelper, final RuleDAO ruleDAO) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.ruleDAO = ruleDAO;
	}

	@Override
	public List<EventModel> findByProject(final UUID projectId) {
		return dslContext.selectFrom(EVENT_MODEL)
			.where(EVENT_MODEL.PROJECT_ID.eq(projectId))
			.fetch(this::mapToModel);
	}

	@Override
	public EventModel findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(EVENT_MODEL)
			.where(EVENT_MODEL.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public EventModel findById(final UUID id) {
		return dslContext.selectFrom(EVENT_MODEL)
			.where(EVENT_MODEL.EVENT_MODEL_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public EventModel save(final EventModel entity) {
		return null;
	}

	@Override
	public void delete(final UUID id) {

	}

	public List<EventModel> findByScopeModel(final UUID scopeModelId) {
		return dslContext.selectFrom(EVENT_MODEL)
			.where(EVENT_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetch(this::mapToModel);
	}

	private EventModel mapToModel(final EventModelRecord record) {
		if(record == null) {
			return null;
		}

		final EventModel model = new EventModel();

		model.setEventModelId(record.getEventModelId());
		model.setId(record.getCode());
		model.setInceptive(record.getInceptive() != null ? record.getInceptive() : false);
		model.setNumber(record.getNumber() != null ? record.getNumber() : 0);
		model.setMandatory(record.getMandatory() != null ? record.getMandatory() : false);
		model.setMaxOccurrence(record.getMaxOccurrence() != null ? record.getMaxOccurrence() : 0);
		model.setPreventAdd(record.getPreventAdd() != null ? record.getPreventAdd() : false);
		model.setLabelPattern(record.getLabelPattern());
		model.setIcon(record.getIcon());

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

		if(record.getEventGroupId() != null) {
			model.setEventGroupId(getEventGroupCode(record.getEventGroupId()));
		}

		model.setDeadline(record.getDeadlineValue());
		model.setDeadlineUnit(mappingHelper.parseEnum(ChronoUnit.class, record.getDeadlineUnit(), "deadlineUnit"));
		model.setDeadlineAggregationFunction(mappingHelper.parseEnum(DateAggregationFunction.class, record.getDeadlineAggrFnct(), "deadlineAggrFnct"));

		model.setInterval(record.getIntervalValue());
		model.setIntervalUnit(mappingHelper.parseEnum(ChronoUnit.class, record.getIntervalUnit(), "intervalUnit"));

		model.setDatasetModelIds(loadDatasetModelIds(record.getEventModelId()));
		model.setFormModelIds(loadFormModelIds(record.getEventModelId()));
		model.setWorkflowIds(loadWorkflowIds(record.getEventModelId()));
		model.setImpliedEventModelIds(loadImpliedEventModelIds(record.getEventModelId()));
		model.setBlockedEventModelIds(loadBlockedEventModelIds(record.getEventModelId()));
		model.setDeadlineReferenceEventModelIds(loadDeadlineReferenceEventModelIds(record.getEventModelId()));

		model.setCreateRules(ruleDAO.findByEntityAndType(RuleEntityType.EVENT_MODEL, record.getEventModelId(), "CREATE"));
		model.setRemoveRules(ruleDAO.findByEntityAndType(RuleEntityType.EVENT_MODEL, record.getEventModelId(), "REMOVE"));
		model.setRestoreRules(ruleDAO.findByEntityAndType(RuleEntityType.EVENT_MODEL, record.getEventModelId(), "RESTORE"));

		return model;
	}

	private String getEventGroupCode(final UUID eventGroupId) {
		return dslContext.select(EVENT_GROUP.CODE)
			.from(EVENT_GROUP)
			.where(EVENT_GROUP.EVENT_GROUP_ID.eq(eventGroupId))
			.fetchOne(EVENT_GROUP.CODE);
	}

	private List<String> loadDatasetModelIds(final UUID eventModelId) {
		return dslContext.select(DATASET_MODEL.CODE)
			.from(EVENT_MODEL_DATASET_MODEL)
			.join(DATASET_MODEL).on(DATASET_MODEL.DATASET_MODEL_ID.eq(EVENT_MODEL_DATASET_MODEL.DATASET_MODEL_ID))
			.where(EVENT_MODEL_DATASET_MODEL.EVENT_MODEL_ID.eq(eventModelId))
			.fetch(DATASET_MODEL.CODE);
	}

	private List<String> loadFormModelIds(final UUID eventModelId) {
		return dslContext.select(FORM_MODEL.CODE)
			.from(EVENT_MODEL_FORM_MODEL)
			.join(FORM_MODEL).on(FORM_MODEL.FORM_MODEL_ID.eq(EVENT_MODEL_FORM_MODEL.FORM_MODEL_ID))
			.where(EVENT_MODEL_FORM_MODEL.EVENT_MODEL_ID.eq(eventModelId))
			.fetch(FORM_MODEL.CODE);
	}

	private List<String> loadWorkflowIds(final UUID eventModelId) {
		return dslContext.select(WORKFLOW.CODE)
			.from(EVENT_MODEL_WORKFLOW)
			.join(WORKFLOW).on(WORKFLOW.WORKFLOW_ID.eq(EVENT_MODEL_WORKFLOW.WORKFLOW_ID))
			.where(EVENT_MODEL_WORKFLOW.EVENT_MODEL_ID.eq(eventModelId))
			.fetch(WORKFLOW.CODE);
	}

	private Set<String> loadImpliedEventModelIds(final UUID eventModelId) {
		final var codes = dslContext.select(EVENT_MODEL.CODE)
			.from(EVENT_MODEL_IMPLIED_EVENT)
			.join(EVENT_MODEL).on(EVENT_MODEL.EVENT_MODEL_ID.eq(EVENT_MODEL_IMPLIED_EVENT.IMPLIED_EVENT_MODEL_ID))
			.where(EVENT_MODEL_IMPLIED_EVENT.EVENT_MODEL_ID.eq(eventModelId))
			.fetch(EVENT_MODEL.CODE);

		return new TreeSet<>(codes);
	}

	private Set<String> loadBlockedEventModelIds(final UUID eventModelId) {
		final var codes = dslContext.select(EVENT_MODEL.CODE)
			.from(EVENT_MODEL_BLOCKED_EVENT)
			.join(EVENT_MODEL).on(EVENT_MODEL.EVENT_MODEL_ID.eq(EVENT_MODEL_BLOCKED_EVENT.BLOCKED_EVENT_MODEL_ID))
			.where(EVENT_MODEL_BLOCKED_EVENT.EVENT_MODEL_ID.eq(eventModelId))
			.fetch(EVENT_MODEL.CODE);

		return new TreeSet<>(codes);
	}

	private List<String> loadDeadlineReferenceEventModelIds(final UUID eventModelId) {
		return dslContext.select(EVENT_MODEL.CODE)
			.from(EVENT_MODEL_DEADLINE_REFERENCE)
			.join(EVENT_MODEL).on(EVENT_MODEL.EVENT_MODEL_ID.eq(EVENT_MODEL_DEADLINE_REFERENCE.REFERENCE_EVENT_MODEL_ID))
			.where(EVENT_MODEL_DEADLINE_REFERENCE.EVENT_MODEL_ID.eq(eventModelId))
			.fetch(EVENT_MODEL.CODE);
	}
}
