package ch.rodano.core.services.bll.widget.workflow;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SortOrder;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.opencsv.CSVWriter;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.scope.ScopeTinyDTO;
import ch.rodano.configuration.model.reports.WorkflowWidget;
import ch.rodano.configuration.model.reports.WorkflowWidgetColumnType;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowableEntity;
import ch.rodano.configuration.model.workflow.WorkflowableModel;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatusInfo;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.workflowStatus.AggregateWorkflowDAOService;
import ch.rodano.core.utils.UtilsService;

import static ch.rodano.core.model.jooq.Tables.DATASET;
import static ch.rodano.core.model.jooq.Tables.EVENT;
import static ch.rodano.core.model.jooq.Tables.FIELD;
import static ch.rodano.core.model.jooq.Tables.FORM;
import static ch.rodano.core.model.jooq.Tables.SCOPE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;
import static ch.rodano.core.model.jooq.Tables.SCOPE_RELATION;
import static ch.rodano.core.model.jooq.Tables.WORKFLOW_STATUS;
import static org.jooq.impl.DSL.field;

@Service
public class WorkflowWidgetServiceImpl implements WorkflowWidgetService {
	private static final ch.rodano.core.model.jooq.tables.Scope PARENT_SCOPE_TABLE = SCOPE.as("parent");

	private static final Field<?> DEFAULT_SORT_COLUMN = SCOPE.CODE;
	private static final Map<String, Field<?>> DB_COLUMNS = Map.ofEntries(
		Map.entry("scopeCode", SCOPE.CODE),
		Map.entry("parentScopeCode", PARENT_SCOPE_TABLE.CODE),
		Map.entry("eventLabel", EVENT.EVENT_MODEL_ID),
		Map.entry("eventDate", EVENT.DATE),
		Map.entry("formLabel", FORM.FORM_MODEL_ID),
		Map.entry("formDate", FORM.LAST_UPDATE_TIME),
		Map.entry("fieldLabel", FIELD.FIELD_MODEL_ID),
		Map.entry("fieldDate", FIELD.LAST_UPDATE_TIME),
		Map.entry("workflow", WORKFLOW_STATUS.WORKFLOW_ID),
		Map.entry("triggerMessage", WORKFLOW_STATUS.WORKFLOW_ID),
		Map.entry("status", WORKFLOW_STATUS.STATE_ID),
		Map.entry("statusDate", WORKFLOW_STATUS.LAST_UPDATE_TIME)
	);

	private static final Map<WorkflowableEntity, TableField<?, ?>> ENTITY_SEARCH_COLUMNS = Map.of(
		WorkflowableEntity.SCOPE, SCOPE.SCOPE_MODEL_ID,
		WorkflowableEntity.EVENT, EVENT.EVENT_MODEL_ID,
		WorkflowableEntity.FORM, FORM.FORM_MODEL_ID,
		WorkflowableEntity.FIELD, FIELD.FIELD_MODEL_ID
	);

	private static final Map<WorkflowableEntity, TableField<?, ?>> WORKFLOW_ENTITY_COLMUN = Map.of(
		WorkflowableEntity.FIELD, WORKFLOW_STATUS.FIELD_FK,
		WorkflowableEntity.FORM, WORKFLOW_STATUS.FORM_FK,
		WorkflowableEntity.EVENT, WORKFLOW_STATUS.EVENT_FK,
		WorkflowableEntity.SCOPE, WORKFLOW_STATUS.SCOPE_FK
	);

	private final StudyService studyService;
	private final DSLContext create;
	private final AggregateWorkflowDAOService aggregateWorkflowDAOService;

	public WorkflowWidgetServiceImpl(
		final StudyService studyService,
		final DSLContext create,
		final AggregateWorkflowDAOService aggregateWorkflowDAOService
	) {
		this.studyService = studyService;
		this.create = create;
		this.aggregateWorkflowDAOService = aggregateWorkflowDAOService;
	}

	@Override
	public PagedResult<WorkflowStatusInfo> getData(
		final WorkflowWidget widget,
		final Collection<Scope> scopes,
		final String[] languages,
		final Optional<String> fullText,
		final Optional<String> sortBy,
		final Optional<Boolean> orderAscending,
		final Optional<Integer> pageSize,
		final Optional<Integer> pageIndex
	) {
		Assert.notEmpty(scopes, "A non empty list of scopes must be provided");

		final var study = studyService.getStudy();

		//check validity of widget
		if(!widget.isValid()) {
			throw new UnsupportedOperationException("Workflow widget configuration is invalid");
		}
		final var workflows = widget.getWorkflows();
		final var isAggregator = workflows.stream().allMatch(Workflow::isAggregator);
		final Table<?> wsTable;
		if(isAggregator) {
			//a widget displaying an aggregation workflow is either on scopes or events
			if(WorkflowableEntity.SCOPE == widget.getWorkflowEntity()) {
				wsTable = aggregateWorkflowDAOService.generateScopeQuery(Optional.of(workflows.get(0)), Optional.empty()).asTable(WORKFLOW_STATUS.getName());
			}
			else {
				wsTable = aggregateWorkflowDAOService.generateEventQuery(Optional.of(workflows.get(0)), Optional.empty()).asTable(WORKFLOW_STATUS.getName());
			}
		}
		else {
			wsTable = WORKFLOW_STATUS;
		}

		final var totalField = DSL.count().over().as("total");
		final var ancestorRelationsTable = SCOPE_ANCESTOR.as("ancestor_relations");
		final var ancestorTable = SCOPE.as("ancestor");
		final var ancestorsField = DSL.multisetAgg(
			ancestorTable.PK,
			ancestorTable.SCOPE_MODEL_ID,
			ancestorTable.CODE,
			ancestorTable.SHORTNAME,
			ancestorTable.LONGNAME
		).as("ancestors").convertFrom(r -> r.map(ra -> new ScopeTinyDTO(ra.value1(), ra.value2(), ra.value3(), ra.value4(), ra.value5())));

		//fields that are always collected
		final List<SelectFieldOrAsterisk> fields = new ArrayList<>(
			Arrays.asList(
				ancestorsField,
				totalField,
				WORKFLOW_STATUS.PK, WORKFLOW_STATUS.SCOPE_FK, WORKFLOW_STATUS.EVENT_FK, WORKFLOW_STATUS.FORM_FK, WORKFLOW_STATUS.FIELD_FK,
				WORKFLOW_STATUS.TRIGGER_MESSAGE, WORKFLOW_STATUS.LAST_UPDATE_TIME, WORKFLOW_STATUS.WORKFLOW_ID, WORKFLOW_STATUS.STATE_ID,
				SCOPE.PK, SCOPE.SCOPE_MODEL_ID, SCOPE.CODE, SCOPE.SHORTNAME, SCOPE.LONGNAME,
				PARENT_SCOPE_TABLE.CODE
			)
		);

		//no need to fetch additional information when workflow is not related to these elements
		if(WorkflowableEntity.SCOPE != widget.getWorkflowEntity()) {
			fields.addAll(
				Arrays.asList(
					EVENT.EVENT_GROUP_NUMBER, EVENT.SCOPE_MODEL_ID, EVENT.EVENT_MODEL_ID, EVENT.EXPECTED_DATE, EVENT.DATE, EVENT.END_DATE, EVENT.BLOCKING
				)
			);
		}
		if(WorkflowableEntity.FORM == widget.getWorkflowEntity()) {
			fields.addAll(Arrays.asList(FORM.FORM_MODEL_ID, FORM.LAST_UPDATE_TIME));
		}
		else if(WorkflowableEntity.FIELD == widget.getWorkflowEntity()) {
			fields.addAll(Arrays.asList(FIELD.DATASET_MODEL_ID, FIELD.FIELD_MODEL_ID, FIELD.LAST_UPDATE_TIME));
		}

		//join on different tables
		final var query = create.select(fields)
			.from(wsTable)
			.innerJoin(SCOPE).on(WORKFLOW_STATUS.SCOPE_FK.eq(SCOPE.PK))
			.leftJoin(EVENT).on(WORKFLOW_STATUS.EVENT_FK.eq(EVENT.PK))
			.leftJoin(FORM).on(WORKFLOW_STATUS.FORM_FK.eq(FORM.PK))
			//retrieve parent
			.leftJoin(SCOPE_RELATION).on(WORKFLOW_STATUS.SCOPE_FK.eq(SCOPE_RELATION.SCOPE_FK))
			.leftJoin(PARENT_SCOPE_TABLE).on(SCOPE_RELATION.PARENT_FK.eq(PARENT_SCOPE_TABLE.PK))
			//join first on scope ancestor to be able to check rights
			.innerJoin(SCOPE_ANCESTOR).on(WORKFLOW_STATUS.SCOPE_FK.eq(SCOPE_ANCESTOR.SCOPE_FK))
			//join twice on scope ancestor to retrieve all scope ancestors
			.innerJoin(ancestorRelationsTable).on(SCOPE.PK.equal(ancestorRelationsTable.SCOPE_FK))
			.innerJoin(ancestorTable).on(ancestorTable.PK.equal(ancestorRelationsTable.ANCESTOR_FK));

		//these tables must be joined to be able to filter the fields in deleted datasets and to retrieve information on the form or the field
		//aggregated workflows are by definition on a scope or a event (they aggregate a workflow on forms or fields)
		//deleted datasets are already filtered in the view, there is no need to filter them again and there is no need to retrieve information on the form nor the field
		//this is just an optimization and could be deleted
		if(!isAggregator) {
			query.leftJoin(FIELD).on(WORKFLOW_STATUS.FIELD_FK.eq(FIELD.PK))
				.leftJoin(DATASET).on(FIELD.DATASET_FK.eq(DATASET.PK));
		}

		//filter workflow states by entity chosen in the configuration
		//the same workflow can be attached to different element
		//think about the query_summary workflow that could be attached to some events and some scopes
		query.where(WORKFLOW_ENTITY_COLMUN.get(widget.getWorkflowEntity()).isNotNull())
			.and(ancestorTable.VIRTUAL.isFalse());

		//filter on workflows and states
		final List<Condition> wssConditions = new ArrayList<>();
		for(final var filter : widget.getWorkflowStatesSelectors()) {
			wssConditions.add(WORKFLOW_STATUS.WORKFLOW_ID.eq(filter.getWorkflowId()).and(WORKFLOW_STATUS.STATE_ID.in(filter.getStateIds())));
		}
		query.and(DSL.or(wssConditions));

		//add scope conditions
		final var now = ZonedDateTime.now();
		final List<Condition> scopeConditions = new ArrayList<>();
		for(final var scope : scopes) {
			final var saCondition = SCOPE_ANCESTOR.ANCESTOR_FK.eq(scope.getPk())
				.and(SCOPE_ANCESTOR.START_DATE.lessThan(now))
				.and(SCOPE_ANCESTOR.END_DATE.isNull().or(SCOPE_ANCESTOR.END_DATE.greaterThan(now)));
			scopeConditions.add(WORKFLOW_STATUS.SCOPE_FK.eq(scope.getPk()).or(saCondition));
		}
		query.and(DSL.or(scopeConditions));

		//add parent condition
		query.and(SCOPE_RELATION.DEFAULT.eq(true));

		//add ancestor condition
		query.and(ancestorRelationsTable.DEFAULT.eq(true));

		//do not consider workflows that are deleted
		query.and(WORKFLOW_STATUS.DELETED.isFalse());
		//aggregated workflow on scopes already filter the contained deleted events, forms and fields in the view, but not the scope that carries the aggregate workflow
		query.and(SCOPE.DELETED.isFalse());
		if(WorkflowableEntity.SCOPE != widget.getWorkflowEntity()) {
			//aggregated workflow on events already filter the contained deleted forms and fields in the view, but not the event that carries the aggregate workflow
			//deleted column may be null if workflow is not related to a event
			query.and(EVENT.DELETED.isNull().or(EVENT.DELETED.isFalse()));
			if(widget.isFilterExpectedEvents()) {
				//date column may be null if workflow is not related to a event
				query.and(EVENT.PK.isNull().or(EVENT.DATE.isNotNull()));
			}
			if(WorkflowableEntity.EVENT != widget.getWorkflowEntity()) {
				//aggregated workflow already filter the deleted datasets in the view, there is no need to filter them again
				if(!isAggregator) {
					//deleted column may be null if workflow is not related to a field
					query.and(DATASET.DELETED.isNull().or(DATASET.DELETED.isFalse()));
				}
			}
		}

		//do not consider workflows on field that are empty
		if(WorkflowableEntity.FIELD == widget.getWorkflowEntity()) {
			query.and(FIELD.VALUE.isNotNull());
		}

		//search
		fullText.ifPresent(search -> {
			final var searchValue = StringUtils.join("%", search.toLowerCase(), "%");
			var searchCondition = WORKFLOW_STATUS.TRIGGER_MESSAGE.likeIgnoreCase(searchValue)
				.or(PARENT_SCOPE_TABLE.CODE.like(searchValue))
				.or(SCOPE.CODE.like(searchValue));
			//add search on workflowable models
			//@formatter:off
			final Predicate<WorkflowableModel> containsSearch = w -> StringUtils.containsIgnoreCase(w.getId(), search)
				|| StringUtils.containsIgnoreCase(w.getLocalizedShortname(languages), search)
				|| StringUtils.containsIgnoreCase(w.getLocalizedLongname(languages), search)
				|| StringUtils.containsIgnoreCase(w.getLocalizedDescription(languages), search);
			//@formatter:on
			final var workflowables = new ArrayList<WorkflowableModel>();
			for(final Workflow workflow : workflows) {
				workflowables.addAll(workflow.getWorkflowableModelsByEntity().get(widget.getWorkflowEntity().getConfigurationEntity()));
			}
			final var workflowablesIds = workflowables.stream()
				.filter(containsSearch)
				.map(WorkflowableModel::getId)
				.toList();
			if(!workflowablesIds.isEmpty()) {
				searchCondition = searchCondition.or(field(ENTITY_SEARCH_COLUMNS.get(widget.getWorkflowEntity())).in(workflowablesIds));
			}
			query.and(searchCondition);
		});

		query.groupBy(WORKFLOW_STATUS.PK);

		//order
		final Field<?> field = sortBy.isPresent() ? DB_COLUMNS.getOrDefault(sortBy.get(), DEFAULT_SORT_COLUMN) : DEFAULT_SORT_COLUMN;
		final var sortedField = field.sort(orderAscending.orElse(true) ? SortOrder.ASC : SortOrder.DESC);
		query.orderBy(sortedField);

		//pagination
		if(pageSize.isPresent() && pageIndex.isPresent()) {
			query.limit(pageSize.get()).offset(pageSize.get() * pageIndex.get());
		}

		final List<WorkflowStatusInfo> infos = new ArrayList<>();
		var total = 0;

		final var results = query.fetch();
		for(final var record : results) {

			total = record.getValue(totalField);
			final var wsi = new WorkflowStatusInfo();

			wsi.setPk(record.getValue(WORKFLOW_STATUS.PK));
			wsi.setScopePk(record.getValue(SCOPE.PK));
			wsi.setScopeModelId(record.getValue(SCOPE.SCOPE_MODEL_ID));
			wsi.setScopeCode(record.getValue(SCOPE.CODE));
			wsi.setScopeShortname(record.getValue(SCOPE.SHORTNAME));
			wsi.setParentScopeCode(record.getValue(PARENT_SCOPE_TABLE.CODE));

			//find scope lineage
			final List<ScopeTinyDTO> ancestors = record.get(ancestorsField);
			final var currentScope = new ScopeTinyDTO(
				record.getValue(SCOPE.PK),
				record.getValue(SCOPE.SCOPE_MODEL_ID),
				record.getValue(SCOPE.CODE),
				record.getValue(SCOPE.SHORTNAME),
				record.getValue(SCOPE.LONGNAME)
			);
			ancestors.add(currentScope);
			wsi.setAncestors(ancestors);

			//workflowable
			if(WorkflowableEntity.SCOPE != widget.getWorkflowEntity()) {
				final var scopeModelId = record.getValue(EVENT.SCOPE_MODEL_ID);
				final var eventModelId = record.getValue(EVENT.EVENT_MODEL_ID);
				if(StringUtils.isNoneBlank(scopeModelId, eventModelId)) {
					final var eventModel = study.getScopeModel(scopeModelId).getEventModel(eventModelId);
					final var eventExpectedDate = record.getValue(EVENT.EXPECTED_DATE);
					final var eventDate = record.getValue(EVENT.DATE);
					wsi.setEventPk(record.getValue(WORKFLOW_STATUS.EVENT_FK));
					wsi.setEventId(eventModel.getId());
					wsi.setEventLabel(eventModel.getDefaultLocalizedShortname());
					wsi.setEventDate(eventDate != null ? eventDate : eventExpectedDate);
				}
			}

			if(WorkflowableEntity.FORM == widget.getWorkflowEntity()) {
				final var formModelId = record.getValue(FORM.FORM_MODEL_ID);
				if(StringUtils.isNotBlank(formModelId)) {
					final var formModel = study.getFormModel(formModelId);
					wsi.setFormPk(record.getValue(WORKFLOW_STATUS.FORM_FK));
					wsi.setFormModelId(formModel.getId());
					wsi.setFormLabel(formModel.getLocalizedShortname(languages));
					wsi.setFormDate(record.getValue(FORM.LAST_UPDATE_TIME));
				}
			}
			else if(WorkflowableEntity.FIELD == widget.getWorkflowEntity()) {
				final var datasetModelId = record.getValue(FIELD.DATASET_MODEL_ID);
				final var fieldModelId = record.getValue(FIELD.FIELD_MODEL_ID);
				if(StringUtils.isNoneBlank(datasetModelId, fieldModelId)) {
					final var fieldModel = study.getDatasetModel(datasetModelId).getFieldModel(fieldModelId);
					wsi.setFieldPk(record.getValue(WORKFLOW_STATUS.FIELD_FK));
					wsi.setDatasetModelId(datasetModelId);
					wsi.setFieldModelId(fieldModelId);
					wsi.setFieldLabel(fieldModel.getLocalizedLabel(languages));
					wsi.setFieldDate(record.getValue(FIELD.LAST_UPDATE_TIME));
				}
			}

			wsi.setTriggerMessage(record.getValue(WORKFLOW_STATUS.TRIGGER_MESSAGE));
			wsi.setStatusDate(record.getValue(WORKFLOW_STATUS.LAST_UPDATE_TIME));

			final var workflow = study.getWorkflow(record.getValue(WORKFLOW_STATUS.WORKFLOW_ID));
			wsi.setWorkflow(workflow.getLocalizedShortname(languages));
			final var state = workflow.getState(record.getValue(WORKFLOW_STATUS.STATE_ID));
			wsi.setStatus(state.getLocalizedShortname(languages));
			wsi.setStatusIcon(state.getIcon());
			wsi.setStatusColor(state.getColor());

			infos.add(wsi);
		}
		return new PagedResult<>(infos, pageSize, pageIndex, total);
	}

	@Override
	public void getExport(final OutputStream out, final WorkflowWidget widget, final Collection<Scope> scopes, final String[] languages) throws IOException {
		final var data = getData(widget, scopes, languages, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()).getObjects();

		final List<ScopeModel> scopeModels = studyService.getStudy().getLineageOfScopeModels();

		try(final var writer = new CSVWriter(new OutputStreamWriter(out))) {

			//header
			final var header = new ArrayList<>();

			//header
			//scope columns
			for(final var scopeModel : scopeModels) {
				final var scopeModelLabel = scopeModel.getLocalizedShortname(languages);
				header.add(String.format("%s ID", scopeModelLabel));
				header.add(scopeModelLabel);
			}

			//custom columns
			for(final var column : widget.getColumns()) {
				if(!column.getType().equals(WorkflowWidgetColumnType.SCOPE_CODE) && !column.getType().equals(WorkflowWidgetColumnType.PARENT_SCOPE_CODE)) {
					header.add(column.getLocalizedShortname(languages));
				}
			}

			// We don't want to duplicate status columns if they have already been inserted
			final var hasWorkflowCustomColumn = widget.getColumns().stream().anyMatch(c -> c.getType().equals(WorkflowWidgetColumnType.WORKFLOW_LABEL));
			final var hasStatusCustomColumn = widget.getColumns().stream().anyMatch(c -> c.getType().equals(WorkflowWidgetColumnType.STATUS_LABEL));
			final var hasStatusDateCustomColumn = widget.getColumns().stream().anyMatch(c -> c.getType().equals(WorkflowWidgetColumnType.STATUS_DATE));

			// Insert status columns if they are not part of the custom columns
			if(!hasWorkflowCustomColumn) {
				header.add("Workflow");
			}
			if(!hasStatusCustomColumn) {
				header.add("Status");
			}
			if(!hasStatusDateCustomColumn) {
				header.add("Status date");
			}

			writer.writeNext(header.toArray(new String[0]));

			//content
			for(final var info : data) {
				final var line = new ArrayList<String>(header.size());

				//column of ancestors' scopes
				for(final var scopeModel : scopeModels) {
					final var ancestor = info.getAncestors().stream().filter(a -> a.modelId().equals(scopeModel.getId())).findAny();
					line.add(ancestor.map(ScopeTinyDTO::pk).map(p -> Long.toString(p)).orElse(""));
					line.add(ancestor.map(ScopeTinyDTO::code).orElse(""));
				}

				//custom columns
				for(final var column : widget.getColumns()) {
					if(!column.getType().equals(WorkflowWidgetColumnType.SCOPE_CODE) && !column.getType().equals(WorkflowWidgetColumnType.PARENT_SCOPE_CODE)) {
						final var value = info.getValue(column.getType());
						if(value != null) {
							if(OperandType.DATE.equals(column.getType().getType())) {
								line.add(((ZonedDateTime) value).format(UtilsService.HUMAN_READABLE_DATE_TIME));
							}
							else {
								line.add(value.toString());
							}
						}
						else {
							line.add("");
						}
					}
				}

				// Insert status columns if they are not part of the custom columns
				if(!hasWorkflowCustomColumn) {
					line.add(info.getWorkflow());
				}
				if(!hasStatusCustomColumn) {
					line.add(info.getStatus());
				}
				if(!hasStatusDateCustomColumn) {
					line.add(info.getStatusDate().format(UtilsService.HUMAN_READABLE_DATE_TIME));
				}

				writer.writeNext(line.toArray(new String[0]));
			}
		}
	}
}
