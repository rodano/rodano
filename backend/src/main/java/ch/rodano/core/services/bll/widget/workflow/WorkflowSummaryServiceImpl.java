package ch.rodano.core.services.bll.widget.workflow;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record16;
import org.jooq.Select;
import org.jooq.SelectField;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.opencsv.CSVWriter;

import ch.rodano.api.dto.widget.SummaryColumnDTO;
import ch.rodano.api.dto.widget.SummaryDTO;
import ch.rodano.api.dto.widget.SummaryRowDTO;
import ch.rodano.api.scope.ScopeTinyDTO;
import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.model.reports.WorkflowSummary;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowState;
import ch.rodano.configuration.model.workflow.WorkflowableEntity;
import ch.rodano.core.model.jooq.tables.records.WorkflowStatusRecord;
import ch.rodano.core.model.scope.Scope;
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
import static ch.rodano.core.model.jooq.Tables.WORKFLOW_STATUS_AUDIT;

@Service
public class WorkflowSummaryServiceImpl implements WorkflowSummaryService {

	private static final Map<WorkflowableEntity, TableField<WorkflowStatusRecord, ?>> WORKFLOW_ENTITY_COLUMN = Map.of(
		WorkflowableEntity.FIELD, WORKFLOW_STATUS.FIELD_FK,
		WorkflowableEntity.FORM, WORKFLOW_STATUS.FORM_FK,
		WorkflowableEntity.EVENT, WORKFLOW_STATUS.EVENT_FK,
		WorkflowableEntity.SCOPE, WORKFLOW_STATUS.SCOPE_FK
	);

	private final StudyService studyService;
	private final DSLContext create;
	private final AggregateWorkflowDAOService aggregateWorkflowDAOService;

	public WorkflowSummaryServiceImpl(
		final StudyService studyService,
		final DSLContext create,
		final AggregateWorkflowDAOService aggregateWorkflowDAOService
	) {
		this.studyService = studyService;
		this.create = create;
		this.aggregateWorkflowDAOService = aggregateWorkflowDAOService;
	}

	/**
	 * Returns the summary data
	 * @param summary Workflow summary object
	 * @param scope Root scope for the data collection
	 * @return the summary
	 */
	@Override
	public SummaryDTO getSummary(final WorkflowSummary summary, final Scope scope) {
		//check validity of widget
		if(scope.getScopeModelId().equals(summary.getLeafScopeModelId())) {
			throw new UnsupportedOperationException("Can not generate summary data for a leaf scope");
		}
		if(!summary.isValid()) {
			throw new UnsupportedOperationException("Workflow widget configuration is invalid");
		}
		final var entity = summary.getWorkflowEntity();
		//only one workflow is supported
		final var workflow = summary.getWorkflows().get(0);
		final var now = ZonedDateTime.now();

		final Map<Long, SummaryRowDTO> summaryByScopePk = new LinkedHashMap<>();

		final var defaultValues = workflow.getStates()
			.stream()
			.collect(Collectors.toMap(WorkflowState::getId, _ -> 0L));

		//add root scope
		final var rootScopeValues = new HashMap<>(defaultValues);
		summaryByScopePk.put(scope.getPk(), new SummaryRowDTO(new ScopeTinyDTO(scope), rootScopeValues));

		//table should be declared as a Table<WorkflowStatusRecord> and should be used in the query below
		final Table<?> wsTable;
		if(workflow.isAggregator()) {
			//a widget displaying an aggregation workflow is either on scopes or events
			if(WorkflowableEntity.SCOPE.equals(entity)) {
				wsTable = aggregateWorkflowDAOService.generateScopeQuery(Optional.of(workflow), Optional.empty()).asTable(WORKFLOW_STATUS.getName());
			}
			else {
				wsTable = aggregateWorkflowDAOService.generateEventQuery(Optional.of(workflow), Optional.empty()).asTable(WORKFLOW_STATUS.getName());
			}
		}
		else {
			wsTable = WORKFLOW_STATUS;
		}

		final var childScopeTable = SCOPE.as("child");
		final var countColumn = DSL.countDistinct(WORKFLOW_STATUS.PK).coerce(Long.class);

		//query columns
		final var columns = new ArrayList<SelectField<?>>();
		columns.addAll(List.of(childScopeTable.PK, childScopeTable.SCOPE_MODEL_ID, childScopeTable.CODE, childScopeTable.SHORTNAME, childScopeTable.LONGNAME));
		columns.addAll(List.of(WORKFLOW_STATUS.STATE_ID, countColumn));

		//query conditions
		final var conditions = new ArrayList<Condition>();
		conditions.add(
			SCOPE_ANCESTOR.DEFAULT.isTrue()
				.and(SCOPE_ANCESTOR.START_DATE.le(now))
				.and(SCOPE_ANCESTOR.END_DATE.isNull().or(SCOPE_ANCESTOR.END_DATE.ge(now)))
				.and(SCOPE_ANCESTOR.ANCESTOR_DELETED.isFalse())
		);
		conditions.add(
			SCOPE_RELATION.DEFAULT.isTrue()
				.and(SCOPE_RELATION.START_DATE.le(now))
				.and(SCOPE_RELATION.END_DATE.isNull().or(SCOPE_RELATION.END_DATE.ge(now)))
				.and(SCOPE_RELATION.PARENT_FK.eq(scope.getPk()))
		);
		conditions.add(WORKFLOW_STATUS.WORKFLOW_ID.in(summary.getWorkflowIds()));
		conditions.add(WORKFLOW_STATUS.DELETED.isFalse());
		conditions.add(SCOPE.SCOPE_MODEL_ID.eq(summary.getLeafScopeModelId()));
		conditions.add(SCOPE.DELETED.isFalse());
		conditions.add(SCOPE_ANCESTOR.ANCESTOR_DELETED.isFalse());
		//keep in mind that the workflow may be attached directly to scopes (and not be linked to a event nor to a dataset)
		conditions.add(WORKFLOW_STATUS.FORM_FK.isNull().or(FORM.DELETED.isFalse()));
		conditions.add(WORKFLOW_STATUS.EVENT_FK.isNull().or(EVENT.DELETED.isFalse()));
		conditions.add(WORKFLOW_STATUS.FIELD_FK.isNull().or(DATASET.DELETED.isFalse()));
		//do not consider workflows on field that are empty
		if(WorkflowableEntity.FIELD.equals(entity)) {
			conditions.add(FIELD.VALUE.isNotNull());
		}
		//filter workflow states by entity chosen in the configuration
		//the same workflow can be attached to different element
		//think about the query_summary workflow that could be attached to some events and some scopes
		conditions.add(WORKFLOW_ENTITY_COLUMN.get(summary.getWorkflowEntity()).isNotNull());

		final var filterEventModelIds = summary.getFilterEventModelIds();
		if(!filterEventModelIds.isEmpty()) {
			conditions.add(EVENT.PK.isNull().or(EVENT.EVENT_MODEL_ID.in(filterEventModelIds)));
		}
		if(summary.isFilterExpectedEvents()) {
			//date column may be null if workflow is not related to a event
			conditions.add(EVENT.PK.isNull().or(EVENT.DATE.isNotNull()));
		}

		final var query = create.selectDistinct(columns).from(wsTable)
			//join on the scope ancestor table to retrieve workflow statuses that are attached to the selected root scope
			.innerJoin(SCOPE_ANCESTOR).on(WORKFLOW_STATUS.SCOPE_FK.eq(SCOPE_ANCESTOR.SCOPE_FK))
			//we look for last relation in scope relation
			//the goal is to be able to group by children of selected scope
			//the children may are the leaf scopes, in this case, sr.scope_fk = s.pk
			.innerJoin(SCOPE_RELATION).on(SCOPE_ANCESTOR.ANCESTOR_FK.eq(SCOPE_RELATION.SCOPE_FK).or(WORKFLOW_STATUS.SCOPE_FK.eq(SCOPE_RELATION.SCOPE_FK)))
			//join child scope
			.innerJoin(childScopeTable).on(SCOPE_RELATION.SCOPE_FK.eq(childScopeTable.PK))
			//join on entities directly attached to the workflow statuses to be able to apply some filters
			.innerJoin(SCOPE).on(WORKFLOW_STATUS.SCOPE_FK.eq(SCOPE.PK))
			.leftJoin(EVENT).on(WORKFLOW_STATUS.EVENT_FK.eq(EVENT.PK))
			.leftJoin(FORM).on(WORKFLOW_STATUS.FORM_FK.eq(FORM.PK))
			.leftJoin(FIELD).on(WORKFLOW_STATUS.FIELD_FK.eq(FIELD.PK))
			.leftJoin(DATASET).on(FIELD.DATASET_FK.eq(DATASET.PK))
			.where(DSL.and(conditions))
			.groupBy(childScopeTable.PK, WORKFLOW_STATUS.STATE_ID)
			.orderBy(childScopeTable.CODE);

		final var results = query.fetch();
		for(final var record : results) {
			final var childScopePk = record.getValue(childScopeTable.PK);
			final var stateId = record.getValue(WORKFLOW_STATUS.STATE_ID);
			final var count = record.getValue(countColumn);
			if(!summaryByScopePk.containsKey(childScopePk)) {
				final var scopeDTO = new ScopeTinyDTO(
					childScopePk,
					record.getValue(childScopeTable.SCOPE_MODEL_ID),
					record.getValue(childScopeTable.CODE),
					record.getValue(childScopeTable.SHORTNAME),
					record.getValue(childScopeTable.LONGNAME)
				);
				summaryByScopePk.put(childScopePk, new SummaryRowDTO(scopeDTO, new HashMap<>(defaultValues)));
			}
			summaryByScopePk.get(childScopePk).values().put(stateId, count);
		}

		//the SQL query returns results for all children of the selected root scope
		//we need to aggregate the result for the selected root scope
		summaryByScopePk.values().stream()
			.flatMap(r -> r.values().entrySet().stream())
			.forEach(e -> rootScopeValues.put(e.getKey(), rootScopeValues.get(e.getKey()) + e.getValue()));

		final var summaryColumns = new ArrayList<SummaryColumnDTO>();
		workflow.getStates().stream()
			.map(
				state -> new SummaryColumnDTO(
					state.getId(),
					state.getShortname(),
					true,
					false
				)
			)
			.forEach(summaryColumns::add);
		summaryColumns.add(
			new SummaryColumnDTO(
				"total",
				Collections.singletonMap(LanguageStatic.en.name(), "Total"),
				false,
				true
			)
		);

		return new SummaryDTO(
			summary.getId(),
			summary.getTitle(),
			summary.getLeafScopeModelId(),
			summaryColumns,
			new ArrayList<SummaryRowDTO>(summaryByScopePk.values())
		);
	}

	@Override
	public void getExport(final OutputStream out, final WorkflowSummary summary, final Collection<Scope> scopes, final boolean includeDeleted, final String[] languages) throws IOException {
		Assert.notEmpty(scopes, "A non empty list of scopes must be provided");

		final var scopeModels = studyService.getStudy().getLineageOfScopeModels(summary.getLeafScopeModel());
		final var workflows = summary.getWorkflows();
		final var entity = summary.getWorkflowEntity();
		final var isAggregator = workflows.stream().allMatch(Workflow::isAggregator);
		final var now = ZonedDateTime.now();

		final Table<?> table;
		if(isAggregator) {
			Select<Record16<Long, ZonedDateTime, ZonedDateTime, Boolean, Long, Long, Long, Long, Long, Long, String, String, String, String, String, String>> tableQuery = null;
			//a widget displaying an aggregation workflow is either on scopes or events
			if(WorkflowableEntity.SCOPE.equals(entity)) {
				tableQuery = aggregateWorkflowDAOService.generateScopeQuery(Optional.of(workflows.get(0)), Optional.empty());
			}
			else {
				tableQuery = aggregateWorkflowDAOService.generateEventQuery(Optional.of(workflows.get(0)), Optional.empty());
			}
			table = DSL.table(tableQuery);
		}
		else {
			table = WORKFLOW_STATUS;
		}

		//subselect to retrieve all scope ancestors
		final var ancestorRelationsTable = SCOPE_ANCESTOR.as("ancestor_relations");
		final var ancestorTable = SCOPE.as("ancestor");
		final var ancestorsField = DSL.multisetAgg(
			ancestorTable.PK,
			ancestorTable.SCOPE_MODEL_ID,
			ancestorTable.CODE,
			ancestorTable.SHORTNAME,
			ancestorTable.LONGNAME
		).as("ancestors").convertFrom(r -> r.map(ra -> new ScopeTinyDTO(ra.value1(), ra.value2(), ra.value3(), ra.value4(), ra.value5())));

		final var removedField = DSL.greatest(
			WORKFLOW_STATUS.DELETED,
			SCOPE.DELETED,
			SCOPE_ANCESTOR.ANCESTOR_DELETED,
			DSL.ifnull(EVENT.DELETED, 0),
			DSL.ifnull(FORM.DELETED, 0),
			DSL.ifnull(DATASET.DELETED, 0)
		);
		//query columns
		final var columns = new ArrayList<SelectField<?>>(
			List.of(
				WORKFLOW_STATUS.PK,
				WORKFLOW_STATUS.CREATION_TIME,
				WORKFLOW_STATUS.LAST_UPDATE_TIME,
				WORKFLOW_STATUS.WORKFLOW_ID,
				WORKFLOW_STATUS.TRIGGER_MESSAGE,
				WORKFLOW_STATUS.VALIDATOR_ID,
				WORKFLOW_STATUS.STATE_ID
			)
		);
		columns.add(removedField);
		columns.addAll(List.of(ancestorsField, SCOPE.PK, SCOPE.SCOPE_MODEL_ID, SCOPE.CODE, SCOPE.SHORTNAME, SCOPE.LONGNAME));
		columns.addAll(List.of(EVENT.PK, EVENT.EVENT_MODEL_ID, EVENT.DATE));
		columns.addAll(List.of(FORM.PK, FORM.FORM_MODEL_ID));
		columns.addAll(List.of(DATASET.PK, DATASET.DATASET_MODEL_ID, FIELD.PK, FIELD.FIELD_MODEL_ID, FIELD.VALUE));

		//query conditions
		final var conditions = new ArrayList<Condition>();

		final var scopePks = scopes.stream().map(Scope::getPk).toList();
		conditions.add(
			SCOPE.PK.in(scopePks).or(
				SCOPE_ANCESTOR.ANCESTOR_FK.in(scopePks)
					.and(SCOPE_ANCESTOR.START_DATE.le(now))
					.and(SCOPE_ANCESTOR.END_DATE.isNull().or(SCOPE_ANCESTOR.END_DATE.ge(now)))
					.and(SCOPE_ANCESTOR.VIRTUAL.isFalse())
					.and(SCOPE_ANCESTOR.ANCESTOR_DELETED.isFalse())
			)
		);
		conditions.add(WORKFLOW_STATUS.WORKFLOW_ID.in(summary.getWorkflowIds()));
		conditions.add(SCOPE.SCOPE_MODEL_ID.eq(summary.getLeafScopeModelId()));
		//do not consider workflows on field that are empty
		if(WorkflowableEntity.FIELD.equals(entity)) {
			conditions.add(FIELD.VALUE.isNotNull());
		}
		//filter workflow states by entity chosen in the configuration
		//the same workflow can be attached to different element
		//think about the query_summary workflow that could be attached to some events and some scopes
		conditions.add(WORKFLOW_ENTITY_COLUMN.get(summary.getWorkflowEntity()).isNotNull());

		final var filterEventModelIds = summary.getFilterEventModelIds();
		if(!filterEventModelIds.isEmpty()) {
			conditions.add(EVENT.PK.isNull().or(EVENT.EVENT_MODEL_ID.in(filterEventModelIds)));
		}
		if(summary.isFilterExpectedEvents()) {
			//date column may be null if workflow is not related to a event
			conditions.add(EVENT.PK.isNull().or(EVENT.DATE.isNotNull()));
		}
		if(!includeDeleted) {
			conditions.add(WORKFLOW_STATUS.DELETED.isFalse());
			conditions.add(SCOPE_ANCESTOR.ANCESTOR_DELETED.isFalse());
			conditions.add(SCOPE.DELETED.isFalse());
			//keep in mind that the workflow may be attached directly to scopes (and not be linked to a event nor to a dataset)
			conditions.add(FORM.PK.isNull().or(FORM.DELETED.isFalse()));
			conditions.add(EVENT.PK.isNull().or(EVENT.DELETED.isFalse()));
			conditions.add(DATASET.PK.isNull().or(DATASET.DELETED.isFalse()));
		}

		final var query = create.select(columns).from(table)
			.innerJoin(SCOPE).on(WORKFLOW_STATUS.SCOPE_FK.eq(SCOPE.PK))
			//first join with the scope ancestor table to be able to check rights
			.innerJoin(SCOPE_ANCESTOR).on(WORKFLOW_STATUS.SCOPE_FK.eq(SCOPE_ANCESTOR.SCOPE_FK))
			//second join with the scope ancestor table to retrieve all scope ancestors
			.innerJoin(ancestorRelationsTable).on(WORKFLOW_STATUS.SCOPE_FK.equal(ancestorRelationsTable.SCOPE_FK).and(ancestorRelationsTable.DEFAULT.isTrue()))
			.innerJoin(ancestorTable).on(ancestorTable.PK.equal(ancestorRelationsTable.ANCESTOR_FK))
			.leftJoin(EVENT).on(WORKFLOW_STATUS.EVENT_FK.eq(EVENT.PK))
			.leftJoin(FORM).on(WORKFLOW_STATUS.FORM_FK.eq(FORM.PK))
			.leftJoin(FIELD).on(WORKFLOW_STATUS.FIELD_FK.eq(FIELD.PK))
			.leftJoin(DATASET).on(FIELD.DATASET_FK.eq(DATASET.PK))
			.where(DSL.and(conditions))
			.groupBy(WORKFLOW_STATUS.PK)
			.orderBy(WORKFLOW_STATUS.PK);

		//write CSV
		try(final var writer = new CSVWriter(new OutputStreamWriter(out))) {

			//header
			final var header = new ArrayList<>();

			for(final var scopeModel : scopeModels) {
				final var modelLabel = scopeModel.getLocalizedShortname(languages);
				header.add(String.format("%s ID", modelLabel));
				header.add(modelLabel);
			}

			if(WorkflowableEntity.EVENT.equals(entity) || WorkflowableEntity.FORM.equals(entity) || WorkflowableEntity.FIELD.equals(entity)) {
				header.add("Event ID");
				header.add("Event");
				header.add("Event date");
			}

			//add columns if workflow is on forms
			if(WorkflowableEntity.FORM.equals(entity)) {
				header.add("Form ID");
				header.add("Form");
			}

			//add columns if workflow is on field
			//form will not be fetched from database, instead it will be guessed from configuration
			if(WorkflowableEntity.FIELD.equals(entity)) {
				header.add("Dataset ID");
				header.add("Dataset");
				header.add("Field ID");
				header.add("Field");
				header.add("Field value");
			}

			header.add("Workflow ID");
			header.add("Workflow");
			header.add("Workflow initialisation date");
			header.add("Workflow initialisation message");
			header.add("Workflow validator");
			header.add("Current workflow status");
			header.add("Current workflow status date");
			header.add("Removed");

			writer.writeNext(header.toArray(new String[0]));

			//lines
			final var study = studyService.getStudy();

			final var results = query.fetch();
			for(final var record : results) {
				final var line = new ArrayList<String>(header.size());

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

				//column of ancestors' scopes
				for(final var scopeModel : scopeModels) {
					final var ancestor = ancestors.stream().filter(a -> a.modelId().equals(scopeModel.getId())).findAny();
					line.add(ancestor.map(ScopeTinyDTO::pk).map(p -> Long.toString(p)).orElse(""));
					line.add(ancestor.map(ScopeTinyDTO::code).orElse(""));
				}

				if(WorkflowableEntity.EVENT.equals(entity) || WorkflowableEntity.FORM.equals(entity) || WorkflowableEntity.FIELD.equals(entity)) {
					//event
					final var scopeModelId = record.getValue(SCOPE.SCOPE_MODEL_ID);
					final var eventModelId = record.getValue(EVENT.EVENT_MODEL_ID);
					if(StringUtils.isNoneBlank(scopeModelId, eventModelId)) {
						line.add(Long.toString(record.getValue(EVENT.PK)));
						final var eventModel = study.getScopeModel(scopeModelId).getEventModel(eventModelId);
						line.add(eventModel.getLocalizedShortname(languages));
						final var eventDate = record.getValue(EVENT.DATE);
						line.add(eventDate != null ? UtilsService.HUMAN_READABLE_DATE_TIME.format(eventDate) : "");
					}
					else {
						line.addAll(List.of("", "", ""));
					}

					//form
					if(WorkflowableEntity.FORM.equals(entity)) {
						final var formModelId = record.getValue(FORM.FORM_MODEL_ID);
						if(formModelId != null) {
							line.add(Long.toString(record.getValue(FORM.PK)));
							final var formModel = study.getFormModel(formModelId);
							line.add(formModel.getLocalizedShortname(languages));
						}
						else {
							line.addAll(List.of("", ""));
						}
					}

					//field
					if(WorkflowableEntity.FIELD.equals(entity)) {
						final var datasetModelId = record.getValue(DATASET.DATASET_MODEL_ID);
						final var fieldModelId = record.getValue(FIELD.FIELD_MODEL_ID);
						if(StringUtils.isNoneBlank(datasetModelId, fieldModelId)) {
							line.add(Long.toString(record.getValue(DATASET.PK)));
							line.add(datasetModelId);
							line.add(Long.toString(record.getValue(FIELD.PK)));
							line.add(fieldModelId);
							line.add(record.getValue(FIELD.VALUE));
						}
						else {
							line.addAll(List.of("", "", "", "", ""));
						}
					}
				}

				line.add(Long.toString(record.getValue(WORKFLOW_STATUS.PK)));
				line.add(record.getValue(WORKFLOW_STATUS.WORKFLOW_ID));
				line.add(UtilsService.HUMAN_READABLE_DATE_TIME.format(record.getValue(WORKFLOW_STATUS.CREATION_TIME)));
				line.add(record.getValue(WORKFLOW_STATUS.TRIGGER_MESSAGE));
				line.add(record.getValue(WORKFLOW_STATUS.VALIDATOR_ID));
				line.add(record.getValue(WORKFLOW_STATUS.STATE_ID));
				line.add(UtilsService.HUMAN_READABLE_DATE_TIME.format(record.getValue(WORKFLOW_STATUS.LAST_UPDATE_TIME)));
				line.add(Boolean.toString(record.getValue(removedField)));

				writer.writeNext(line.toArray(new String[0]));
			}
		}
	}

	@Override
	public void getHistoricalExport(final OutputStream out, final WorkflowSummary summary, final Collection<Scope> scopes, final boolean includeDeleted, final String[] languages) throws IOException {
		Assert.notEmpty(scopes, "A non empty list of scopes must be provided");

		final List<ScopeModel> scopeModels = studyService.getStudy().getLineageOfScopeModels(summary.getLeafScopeModel());
		final var entity = summary.getWorkflowEntity();
		final ZonedDateTime now = ZonedDateTime.now();

		final var ancestorRelationsTable = SCOPE_ANCESTOR.as("ancestor_relations");
		final var ancestorTable = SCOPE.as("ancestor");
		final var ancestorsField = DSL.multisetAgg(
			ancestorTable.PK,
			ancestorTable.SCOPE_MODEL_ID,
			ancestorTable.CODE,
			ancestorTable.SHORTNAME,
			ancestorTable.LONGNAME
		).as("ancestors").convertFrom(r -> r.map(ra -> new ScopeTinyDTO(ra.value1(), ra.value2(), ra.value3(), ra.value4(), ra.value5())));

		final var removedField = DSL.greatest(
			WORKFLOW_STATUS.DELETED,
			SCOPE.DELETED,
			SCOPE_ANCESTOR.ANCESTOR_DELETED,
			DSL.ifnull(EVENT.DELETED, 0),
			DSL.ifnull(FORM.DELETED, 0),
			DSL.ifnull(DATASET.DELETED, 0)
		);
		//query columns
		final var columns = new ArrayList<SelectField<?>>();
		columns.addAll(
			List.of(
				WORKFLOW_STATUS.PK,
				WORKFLOW_STATUS.CREATION_TIME,
				WORKFLOW_STATUS.WORKFLOW_ID,
				WORKFLOW_STATUS.TRIGGER_MESSAGE,
				WORKFLOW_STATUS.VALIDATOR_ID
			)
		);
		columns.addAll(List.of(WORKFLOW_STATUS_AUDIT.PK, WORKFLOW_STATUS_AUDIT.AUDIT_DATETIME, WORKFLOW_STATUS_AUDIT.AUDIT_ACTOR, WORKFLOW_STATUS_AUDIT.AUDIT_CONTEXT, WORKFLOW_STATUS_AUDIT.STATE_ID));
		columns.add(removedField);
		columns.addAll(List.of(ancestorsField, SCOPE.PK, SCOPE.SCOPE_MODEL_ID, SCOPE.CODE, SCOPE.SHORTNAME, SCOPE.LONGNAME));
		columns.addAll(List.of(EVENT.PK, EVENT.EVENT_MODEL_ID, EVENT.DATE));
		columns.addAll(List.of(FORM.PK, FORM.FORM_MODEL_ID));
		columns.addAll(List.of(DATASET.PK, DATASET.DATASET_MODEL_ID, FIELD.PK, FIELD.FIELD_MODEL_ID, FIELD.VALUE));

		//query conditions
		final var conditions = new ArrayList<Condition>();

		final var scopePks = scopes.stream().map(Scope::getPk).toList();
		conditions.add(
			SCOPE.PK.in(scopePks).or(
				SCOPE_ANCESTOR.ANCESTOR_FK.in(scopePks)
					.and(SCOPE_ANCESTOR.START_DATE.le(now))
					.and(SCOPE_ANCESTOR.END_DATE.isNull().or(SCOPE_ANCESTOR.END_DATE.ge(now)))
					.and(SCOPE_ANCESTOR.VIRTUAL.isFalse()).and(SCOPE_ANCESTOR.ANCESTOR_DELETED.isFalse())
			)
		);
		conditions.add(WORKFLOW_STATUS.WORKFLOW_ID.in(summary.getWorkflowIds()));
		conditions.add(SCOPE.SCOPE_MODEL_ID.eq(summary.getLeafScopeModelId()));
		//do not consider workflows on field that are empty
		if(WorkflowableEntity.FIELD.equals(entity)) {
			conditions.add(FIELD.VALUE.isNotNull());
		}
		//filter workflow states by entity chosen in the configuration
		//the same workflow can be attached to different element
		//think about the query_summary workflow that could be attached to some events and some scopes
		conditions.add(WORKFLOW_ENTITY_COLUMN.get(summary.getWorkflowEntity()).isNotNull());

		final var filterEventModelIds = summary.getFilterEventModelIds();
		if(!filterEventModelIds.isEmpty()) {
			conditions.add(EVENT.PK.isNull().or(EVENT.EVENT_MODEL_ID.in(filterEventModelIds)));
		}
		if(summary.isFilterExpectedEvents()) {
			//date column may be null if workflow is not related to a event
			conditions.add(EVENT.PK.isNull().or(EVENT.DATE.isNotNull()));
		}
		if(!includeDeleted) {
			conditions.add(WORKFLOW_STATUS.DELETED.isFalse());
			conditions.add(SCOPE_ANCESTOR.ANCESTOR_DELETED.isFalse());
			conditions.add(SCOPE.DELETED.isFalse());
			//keep in mind that the workflow may be attached directly to scopes (and not be linked to a event nor to a dataset)
			conditions.add(FORM.PK.isNull().or(FORM.DELETED.isFalse()));
			conditions.add(EVENT.PK.isNull().or(EVENT.DELETED.isFalse()));
			conditions.add(DATASET.PK.isNull().or(DATASET.DELETED.isFalse()));
		}

		final var query = create.select(columns).from(WORKFLOW_STATUS)
			.innerJoin(WORKFLOW_STATUS_AUDIT).on(WORKFLOW_STATUS.PK.eq(WORKFLOW_STATUS_AUDIT.AUDIT_OBJECT_FK()))
			.innerJoin(SCOPE).on(WORKFLOW_STATUS.SCOPE_FK.eq(SCOPE.PK))
			//first join with the scope ancestor table to be able to check rights
			.innerJoin(SCOPE_ANCESTOR).on(WORKFLOW_STATUS.SCOPE_FK.eq(SCOPE_ANCESTOR.SCOPE_FK))
			//second join with the scope ancestor table to retrieve all scope ancestors
			.innerJoin(ancestorRelationsTable).on(WORKFLOW_STATUS.SCOPE_FK.equal(ancestorRelationsTable.SCOPE_FK).and(ancestorRelationsTable.DEFAULT.isTrue()))
			.innerJoin(ancestorTable).on(ancestorTable.PK.equal(ancestorRelationsTable.ANCESTOR_FK))
			.leftJoin(EVENT).on(WORKFLOW_STATUS.EVENT_FK.eq(EVENT.PK))
			.leftJoin(FORM).on(WORKFLOW_STATUS.FORM_FK.eq(FORM.PK))
			.leftJoin(FIELD).on(WORKFLOW_STATUS.FIELD_FK.eq(FIELD.PK))
			.leftJoin(DATASET).on(FIELD.DATASET_FK.eq(DATASET.PK))
			.where(DSL.and(conditions))
			.groupBy(WORKFLOW_STATUS.PK, WORKFLOW_STATUS_AUDIT.PK)
			.orderBy(WORKFLOW_STATUS.PK, WORKFLOW_STATUS_AUDIT.PK);

		try(final var writer = new CSVWriter(new OutputStreamWriter(out))) {

			//header
			final var header = new ArrayList<>();

			for(final var scopeModel : scopeModels) {
				final var modelLabel = scopeModel.getLocalizedShortname(languages);
				header.add(String.format("%s ID", modelLabel));
				header.add(modelLabel);
			}

			if(WorkflowableEntity.EVENT.equals(entity) || WorkflowableEntity.FORM.equals(entity) || WorkflowableEntity.FIELD.equals(entity)) {
				header.add("Event ID");
				header.add("Event");
				header.add("Event date");
			}

			//add columns if workflow is on forms
			if(WorkflowableEntity.FORM.equals(entity)) {
				header.add("Form ID");
				header.add("Form");
			}

			//add columns if workflow is on field
			//form will not be fetched from database, instead it will be guessed from configuration
			if(WorkflowableEntity.FIELD.equals(entity)) {
				header.add("Dataset ID");
				header.add("Dataset");
				header.add("Field ID");
				header.add("Field");
				header.add("Field value");
			}

			header.add("Workflow ID");
			header.add("Workflow");
			header.add("Workflow initialisation date");
			header.add("Workflow initialisation message");
			header.add("Workflow validator");
			header.add("Removed");
			header.add("Actor");
			header.add("Workflow audit ID");
			header.add("Workflow audit date");
			header.add("Workflow audit status");
			header.add("Workflow audit message");

			writer.writeNext(header.toArray(new String[0]));

			//lines
			final var study = studyService.getStudy();

			final var results = query.fetch();
			for(final var record : results) {
				final var line = new ArrayList<String>(header.size());

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

				//column of ancestors' scopes
				for(final var scopeModel : scopeModels) {
					final var ancestor = ancestors.stream().filter(a -> a.modelId().equals(scopeModel.getId())).findAny();
					line.add(ancestor.map(ScopeTinyDTO::pk).map(p -> Long.toString(p)).orElse(""));
					line.add(ancestor.map(ScopeTinyDTO::code).orElse(""));
				}

				if(WorkflowableEntity.EVENT.equals(entity) || WorkflowableEntity.FORM.equals(entity) || WorkflowableEntity.FIELD.equals(entity)) {
					//event
					final var scopeModelId = record.getValue(SCOPE.SCOPE_MODEL_ID);
					final var eventModelId = record.getValue(EVENT.EVENT_MODEL_ID);
					if(StringUtils.isNoneBlank(scopeModelId, eventModelId)) {
						line.add(Long.toString(record.getValue(EVENT.PK)));
						final var eventModel = study.getScopeModel(scopeModelId).getEventModel(eventModelId);
						line.add(eventModel.getLocalizedShortname(languages));
						final var eventDate = record.getValue(EVENT.DATE);
						line.add(eventDate != null ? UtilsService.HUMAN_READABLE_DATE_TIME.format(eventDate) : "");
					}
					else {
						line.addAll(List.of("", "", ""));
					}

					//form
					if(WorkflowableEntity.FORM.equals(entity)) {
						final var formModelId = record.getValue(FORM.FORM_MODEL_ID);
						if(StringUtils.isNotBlank(formModelId)) {
							line.add(Long.toString(record.getValue(FORM.PK)));
							final var formModel = study.getFormModel(formModelId);
							line.add(formModel.getLocalizedShortname(languages));
						}
						else {
							line.addAll(List.of("", ""));
						}
					}

					//field
					if(WorkflowableEntity.FIELD.equals(entity)) {
						final var datasetModelId = record.getValue(DATASET.DATASET_MODEL_ID);
						final var fieldModelId = record.getValue(FIELD.FIELD_MODEL_ID);
						if(StringUtils.isNoneBlank(datasetModelId, fieldModelId)) {
							line.add(Long.toString(record.getValue(DATASET.PK)));
							line.add(datasetModelId);
							line.add(Long.toString(record.getValue(FIELD.PK)));
							line.add(fieldModelId);
							line.add(record.getValue(FIELD.VALUE));
						}
						else {
							line.addAll(List.of("", "", "", "", ""));
						}
					}
				}

				line.add(Long.toString(record.getValue(WORKFLOW_STATUS.PK)));
				line.add(record.getValue(WORKFLOW_STATUS.WORKFLOW_ID));
				line.add(UtilsService.HUMAN_READABLE_DATE_TIME.format(record.getValue(WORKFLOW_STATUS.CREATION_TIME)));
				line.add(record.getValue(WORKFLOW_STATUS.TRIGGER_MESSAGE));
				line.add(record.getValue(WORKFLOW_STATUS.VALIDATOR_ID));
				line.add(Boolean.toString(record.getValue(removedField)));
				line.add(record.getValue(WORKFLOW_STATUS_AUDIT.AUDIT_ACTOR()));
				line.add(Long.toString(record.getValue(WORKFLOW_STATUS_AUDIT.PK)));
				line.add(UtilsService.HUMAN_READABLE_DATE_TIME.format(record.getValue(WORKFLOW_STATUS_AUDIT.AUDIT_DATETIME())));
				line.add(record.getValue(WORKFLOW_STATUS_AUDIT.STATE_ID));
				line.add(record.getValue(WORKFLOW_STATUS_AUDIT.AUDIT_CONTEXT));

				writer.writeNext(line.toArray(new String[0]));
			}
		}
	}
}
