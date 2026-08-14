package ch.rodano.api.scope.search;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.scope.ScopeTinyDTO;
import ch.rodano.api.workflow.WorkflowStatusDTO;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.core.model.jooqutils.JOOQTranslator;
import ch.rodano.core.model.scope.ScopeSearch;
import ch.rodano.core.services.bll.study.StudyService;

import static ch.rodano.core.model.jooq.Tables.DATASET;
import static ch.rodano.core.model.jooq.Tables.FIELD;
import static ch.rodano.core.model.jooq.Tables.SCOPE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;
import static ch.rodano.core.model.jooq.Tables.SCOPE_RELATION;
import static ch.rodano.core.model.jooq.Tables.WORKFLOW_STATUS;

@Service
@Transactional(readOnly = true)
public class ExtendedScopeResultServiceImpl implements ExtendedScopeResultService {

	final Integer defaultPageSize;
	private final StudyService studyService;
	private final DSLContext create;

	public ExtendedScopeResultServiceImpl(
		final StudyService studyService,
		final DSLContext create,
		@Value("${rodano.pagination.maximum-page-size}") final Integer defaultPageSize
	) {
		this.studyService = studyService;
		this.create = create;
		this.defaultPageSize = defaultPageSize;
	}

	private static String sqlWorkflowsStateColumnAlias(final String workflowId) {
		return String.format("%s_%s", "ws", workflowId);
	}

	private static String sqlFieldValueColumnAlias(final String datasetModelId, final String fieldId) {
		return String.format("%s.%s", datasetModelId.toLowerCase(), fieldId.toLowerCase());
	}

	private enum CustomSortType {
		WORKFLOW_STATE,
		FIELD_VALUE
	}

	private record ResolvedCustomSort(
		CustomSortType type,
		String workflowId,
		String datasetModelId,
		String fieldModelId,
		FieldModel fieldModel) {
	}

	private static Optional<ResolvedCustomSort> resolveCustomSort(
		final String customSortTarget,
		final List<Workflow> workflowsOnScopeModel,
		final List<FieldModel> searchableFieldsOnScopeModel) {

		if(customSortTarget == null || customSortTarget.isEmpty()) {
			return Optional.empty();
		}

		if(!customSortTarget.contains(".")) {
			final var normalizedWorkflowId = customSortTarget.startsWith("ws_")
				? customSortTarget.substring(3)
				: customSortTarget;

			final var isAllowedWorkflow = workflowsOnScopeModel.stream()
				.map(Workflow::getId)
				.anyMatch(normalizedWorkflowId::equals);

			if(!isAllowedWorkflow) {
				return Optional.empty();
			}

			return Optional.of(new ResolvedCustomSort(
				CustomSortType.WORKFLOW_STATE,
				normalizedWorkflowId,
				null,
				null,
				null));
		}

		final int separatorIndex = customSortTarget.indexOf('.');
		if(separatorIndex <= 0 || separatorIndex >= customSortTarget.length() - 1) {
			return Optional.empty();
		}

		final var datasetModelId = customSortTarget.substring(0, separatorIndex);
		final var fieldModelId = customSortTarget.substring(separatorIndex + 1);

		final var matchedFieldModel = searchableFieldsOnScopeModel.stream()
			.filter(f -> f.getDatasetModel().getId().equalsIgnoreCase(datasetModelId)
				&& f.getId().equalsIgnoreCase(fieldModelId))
			.findFirst();

		return matchedFieldModel.map(fieldModel -> new ResolvedCustomSort(
			CustomSortType.FIELD_VALUE,
			null,
			datasetModelId,
			fieldModelId,
			fieldModel));

	}

	@Override
	public PagedResult<ExtendedScopeSearchResultDTO> search(final ScopeSearch search) {

		final var now = ZonedDateTime.now();
		final List<Condition> conditions = new ArrayList<>();

		final var workflowsOnScopeModel = studyService.getStudy().getScopeModel(search.getScopeModelId().get()).getSearchableWorkflows();
		final var searchableFieldsOnScopeModel = studyService.getStudy().getScopeModel(search.getScopeModelId().get()).getSearchableFields();

		final var workflowStateColumnAliases = workflowsOnScopeModel.stream()
			.map(wf -> sqlWorkflowsStateColumnAlias(wf.getId()))
			.toList();

		// Build a lightweight query used only for filtering, counting and selecting page PKs.
		final var sa = SCOPE_ANCESTOR.as("scope_ancestor");
		final var sr = SCOPE_RELATION.as("scope_default_parent");
		final var defaultParentScope = SCOPE.as("default_scope_parent");
		final var pkQuery = create.select(SCOPE.PK)
			.from(SCOPE)
			.leftJoin(sa).on(SCOPE.PK.eq(sa.SCOPE_FK).and(sa.DEFAULT.isTrue()));

		var parentJoinCondition = SCOPE.PK.eq(sr.SCOPE_FK)
			.and(sr.DEFAULT.isTrue())
			.and(sr.END_DATE.isNull().or(sr.END_DATE.ge(now)));

		final var parentPks = search.getParentPks().orElse(Collections.emptyList());
		if(!parentPks.isEmpty()) {
			parentJoinCondition = parentJoinCondition.and(sr.PARENT_FK.in(parentPks));
		}

		pkQuery.leftJoin(sr)
			.on(parentJoinCondition)
			.innerJoin(defaultParentScope)
			.on(defaultParentScope.PK.eq(sr.PARENT_FK));

		search.getAncestorPks().ifPresent(ancestorPks -> {
			if(ancestorPks.isEmpty()) {
				return;
			}

			final var saFilter = SCOPE_ANCESTOR.as("SA");
			pkQuery.innerJoin(saFilter)
				.on(SCOPE.PK.eq(saFilter.SCOPE_FK).and(saFilter.ANCESTOR_FK.in(ancestorPks)));

			if(!search.getIncludeRemoved()) {
				conditions.add(saFilter.ANCESTOR_REMOVED.isFalse());
			}
		});

		search.getScopeModelAncestorPks().ifPresent(ancestorPks -> {
			if(!ancestorPks.isEmpty()) {
				final List<Condition> scopeConditions = new ArrayList<>();
				for(final var entry : ancestorPks.entrySet()) {
					scopeConditions.add(
						DSL.or(
							SCOPE.PK.in(entry.getValue()),
							sa.ANCESTOR_FK.in(entry.getValue())
						).and(SCOPE.SCOPE_MODEL_ID.eq(entry.getKey()))
					);
				}
				conditions.add(DSL.or(scopeConditions));
			}
		});

		if(!search.getIncludeRemoved()) {
			conditions.add(
				SCOPE.REMOVED.isFalse()
					.and(sa.ANCESTOR_FK.isNull().or(sa.ANCESTOR_REMOVED.isFalse())));
		}

		final var workflowStatesByWorkflow = search.getWorkflowStates().orElse(Collections.emptyMap());
		workflowsOnScopeModel.forEach(workflow -> {
			final var workflowId = workflow.getId();
			final var filteredStates = workflowStatesByWorkflow.get(workflowId);

			if(filteredStates != null && !filteredStates.isEmpty()) {
				final var workflowFilter = WORKFLOW_STATUS.as(sqlWorkflowsStateColumnAlias(workflowId) + "_filter");
				conditions.add(SCOPE.PK.in(
					DSL.select(workflowFilter.SCOPE_FK)
						.from(workflowFilter)
						.where(workflowFilter.WORKFLOW_ID.eq(workflowId))
						.and(workflowFilter.REMOVED.isFalse())
						.and(workflowFilter.STATE_ID.in(filteredStates))
				));
			}
		});

		search.getFieldModelCriteria().ifPresent(criteria -> {
			for(final var fieldModel : searchableFieldsOnScopeModel) {
				final var datasetModelId = fieldModel.getDatasetModel().getId();
				final var fieldAlias = datasetModelId.toLowerCase() + "_" + fieldModel.getId().toLowerCase();
				final var fieldCriteria = criteria.stream()
					.filter(c -> c.datasetModelId().equals(datasetModelId) && c.fieldModelId().equals(fieldModel.getId()))
					.toList();

				if(!fieldCriteria.isEmpty()) {
					final var criteriaDatasetAlias = fieldAlias + "_dataset_criteria";
					final var criteriaFieldAlias = fieldAlias + "_criteria";
					final var criteriaDataset = DATASET.as(criteriaDatasetAlias);
					final var criteriaField = FIELD.as(criteriaFieldAlias);
					final var operatedField = DSL.field(DSL.name(criteriaFieldAlias, "value"), String.class);

					Condition combinedCondition = null;
					for(var criterion : fieldCriteria) {
						final var fieldCondition = JOOQTranslator.translate(criterion.operator(), fieldModel, operatedField, criterion.value());
						combinedCondition = combinedCondition == null ? fieldCondition : combinedCondition.or(fieldCondition);
					}

					conditions.add(SCOPE.PK.in(
						DSL.select(criteriaDataset.SCOPE_FK)
							.from(criteriaDataset)
							.join(criteriaField)
							.on(criteriaField.DATASET_FK.eq(criteriaDataset.field("pk", Long.class))
								.and(criteriaField.FIELD_MODEL_ID.eq(fieldModel.getId())))
							.where(criteriaDataset.DATASET_MODEL_ID.eq(datasetModelId))
							.and(combinedCondition)
					));
				}
			}
		});

		search.getScopeModelId().ifPresent(scopeModelId -> conditions.add(SCOPE.SCOPE_MODEL_ID.eq(scopeModelId)));
		search.getPks().ifPresent(pks -> conditions.add(SCOPE.PK.in(pks)));
		search.getIds().ifPresent(ids -> conditions.add(SCOPE.ID.in(ids)));
		search.getCode().ifPresent(code -> conditions.add(SCOPE.CODE.containsIgnoreCase(code)));

		search.getLeaf().ifPresent(leaf -> {
			final var leafId = studyService.getStudy().getLeafScopeModel().getId();
			if(leaf) {
				conditions.add(SCOPE.SCOPE_MODEL_ID.eq(leafId));
			}
			else{
				conditions.add(SCOPE.SCOPE_MODEL_ID.notEqual(leafId));
			}
		});

		search.getFullText().ifPresent(fullText -> conditions.add(
			SCOPE.CODE.containsIgnoreCase(fullText)
				.or(SCOPE.SHORTNAME.containsIgnoreCase(fullText))
				.or(SCOPE.LONGNAME.containsIgnoreCase(fullText))));

		// Custom sort handling: only add joins to PK query if custom sorting is requested
		Field<?> sortField = null;
		final var resolvedCustomSort = resolveCustomSort(search.getExtendedSortBy(), workflowsOnScopeModel, searchableFieldsOnScopeModel);

		if(resolvedCustomSort.isPresent()) {
			final var sort = resolvedCustomSort.get();
			switch(sort.type()) {
				case WORKFLOW_STATE:
					final var workflowSortJoin = WORKFLOW_STATUS.as(sqlWorkflowsStateColumnAlias(sort.workflowId()) + "_sort");
					pkQuery.leftJoin(workflowSortJoin).on(
						SCOPE.PK.eq(workflowSortJoin.SCOPE_FK)
							.and(workflowSortJoin.WORKFLOW_ID.eq(sort.workflowId()))
							.and(workflowSortJoin.REMOVED.isFalse()));
					sortField = DSL.field(DSL.name(sqlWorkflowsStateColumnAlias(sort.workflowId()) + "_sort", "state_id"), String.class);
					break;
				case FIELD_VALUE:
					final var sortDatasetAlias = "sort_dataset_" + sort.datasetModelId().toLowerCase();
					final var sortFieldAlias = "sort_" + sort.datasetModelId().toLowerCase() + "_" + sort.fieldModelId().toLowerCase();
					final var sortDataset = DATASET.as(sortDatasetAlias);
					final var sortFieldTable = FIELD.as(sortFieldAlias);

					pkQuery.leftJoin(sortDataset).on(
						SCOPE.PK.eq(sortDataset.SCOPE_FK)
							.and(sortDataset.DATASET_MODEL_ID.eq(sort.fieldModel().getDatasetModel().getId())));

					pkQuery.leftJoin(sortFieldTable).on(
						sortFieldTable.DATASET_FK.eq(sortDataset.field("pk", Long.class))
							.and(sortFieldTable.FIELD_MODEL_ID.eq(sort.fieldModel().getId())));

					final var valueField = DSL.field(DSL.name(sortFieldAlias, "value"), String.class);
					sortField = JOOQTranslator.translateSortableField(sort.fieldModel(), valueField);
					break;
				default:
					break;
			}
		}

		final var filteredPkQuery = pkQuery.where(conditions).groupBy(SCOPE.PK);
		final var total = create.selectCount()
			.from(filteredPkQuery.asTable("matching_scopes"))
			.fetchOne(0, Integer.class);

		// Apply sorting once using custom sort field when available, otherwise default sort field.
		final var effectiveSortField = sortField != null ? sortField : search.getSortBy().getField();
		filteredPkQuery.orderBy(effectiveSortField.sort(search.getOrder()));

		// Counting and paging
		final int totalCount = total == null ? 0 : total;
		final int pageSize = Math.max(1, search.getPageSize().orElse(this.defaultPageSize));
		final int pageIndex = Math.max(0, search.getPageIndex().orElse(0));

		//That line is just a safe calculation of the SQL OFFSET used for pagination.
		final long offsetLong = Math.min(Integer.MAX_VALUE, (long) pageSize * (long) pageIndex);
		final int offset = (int) offsetLong;

		// Establish the list of scope PKs for the requested page.
		final var pagePks = filteredPkQuery.limit(pageSize)
			.offset(offset)
			.fetch(SCOPE.PK);

		if(pagePks.isEmpty()) {
			return new PagedResult<>(Collections.emptyList(), pageSize, pageIndex, totalCount);
		}

		// Start of the "details" query construction: select all fields needed to build the final DTO, including workflow states and field values.
		final List<SelectFieldOrAsterisk> selectFields = new ArrayList<>();
		selectFields.add(SCOPE.asterisk());
		for(String workflowColumnAlias : workflowStateColumnAliases) {
			final var workflowStateAlias = workflowColumnAlias + ".state_id";
			selectFields.add(DSL.field(DSL.name(workflowColumnAlias, "state_id")).as(workflowStateAlias));
		}

		for(var fieldModel : searchableFieldsOnScopeModel) {
			final var datasetModel = fieldModel.getDatasetModel();
			final var selectAlias = sqlFieldValueColumnAlias(datasetModel.getId(), fieldModel.getId());
			final var fieldAlias = datasetModel.getId().toLowerCase() + "_" + fieldModel.getId().toLowerCase();
			selectFields.add(DSL.field(DSL.name(fieldAlias, "value")).as(selectAlias));
		}

		final var detailSa = SCOPE_ANCESTOR.as("scope_ancestor");
		final var detailSr = SCOPE_RELATION.as("scope_default_parent");
		final var detailDefaultParentScope = SCOPE.as("default_scope_parent");

		selectFields.add(detailDefaultParentScope.PK.as("default_parent_sc_pk"));
		selectFields.add(detailDefaultParentScope.SCOPE_MODEL_ID.as("default_parent_sc_model_id"));
		selectFields.add(detailDefaultParentScope.CODE.as("ancestor_code"));
		selectFields.add(detailDefaultParentScope.SHORTNAME.as("default_parent_sc_shortname"));
		selectFields.add(detailDefaultParentScope.LONGNAME.as("default_parent_sc_longname"));
		selectFields.add(detailDefaultParentScope.CODE.as("default_parent_sc_code"));

		final var detailQuery = create.select(selectFields)
			.from(SCOPE)
			.leftJoin(detailSa).on(SCOPE.PK.eq(detailSa.SCOPE_FK).and(detailSa.DEFAULT.isTrue()));

		var detailParentJoinCondition = SCOPE.PK.eq(detailSr.SCOPE_FK)
			.and(detailSr.DEFAULT.isTrue())
			.and(detailSr.END_DATE.isNull().or(detailSr.END_DATE.ge(now)));

		if(!parentPks.isEmpty()) {
			detailParentJoinCondition = detailParentJoinCondition.and(detailSr.PARENT_FK.in(parentPks));
		}

		detailQuery.leftJoin(detailSr)
			.on(detailParentJoinCondition)
			.innerJoin(detailDefaultParentScope)
			.on(detailDefaultParentScope.PK.eq(detailSr.PARENT_FK));

		workflowsOnScopeModel.forEach(workflow -> {
			final var workflowId = workflow.getId();
			final var workflowJoin = WORKFLOW_STATUS.as(sqlWorkflowsStateColumnAlias(workflowId));
			detailQuery.leftJoin(workflowJoin).on(
				SCOPE.PK.eq(workflowJoin.SCOPE_FK)
					.and(workflowJoin.WORKFLOW_ID.eq(workflowId))
					.and(workflowJoin.REMOVED.isFalse()));
		});

		final Map<String, org.jooq.Table<?>> datasetTables = new HashMap<>();
		for(final var fieldModel : searchableFieldsOnScopeModel) {
			final var datasetModel = fieldModel.getDatasetModel();
			final var datasetModelId = datasetModel.getId();
			final var datasetAlias = "dataset_" + datasetModelId.toLowerCase();

			if(!datasetTables.containsKey(datasetModelId)) {
				final var datasetTable = DATASET.as(datasetAlias);
				datasetTables.put(datasetModelId, datasetTable);
				detailQuery.leftJoin(datasetTable).on(
					SCOPE.PK.eq(datasetTable.SCOPE_FK)
						.and(datasetTable.DATASET_MODEL_ID.eq(datasetModelId)));
			}

			final var fieldAlias = datasetModelId.toLowerCase() + "_" + fieldModel.getId().toLowerCase();
			final var fieldTable = FIELD.as(fieldAlias);
			final var datasetTable = datasetTables.get(datasetModelId);
			detailQuery.leftJoin(fieldTable).on(
				fieldTable.DATASET_FK.eq(datasetTable.field("pk", Long.class))
					.and(fieldTable.FIELD_MODEL_ID.eq(fieldModel.getId())));
		}

		// Fetch details for paged PKs only.
		final var records = detailQuery.where(SCOPE.PK.in(pagePks))
			.fetch();
		final Map<Long, ExtendedScopeSearchResultDTO> resultsByPk = new HashMap<>();

		for(var record : records) {
			final ExtendedScopeSearchResultDTO dto = new ExtendedScopeSearchResultDTO();
			dto.setCode(record.get("code").toString());
			dto.setPk(record.get("pk", Long.class));
			dto.setId(record.get("id").toString());
			dto.setShortname(record.get("shortname").toString());
			dto.setLongname(record.get("longname") != null ? record.get("longname").toString() : null);
			dto.setModelId(record.get("scope_model_id").toString());
			dto.setVirtual((Boolean) record.get("virtual"));
			dto.setRemoved(record.get("removed") != null ? (Boolean) record.get("removed") : false);
			final ScopeTinyDTO parentDTO = new ScopeTinyDTO(
				record.get("default_parent_sc_pk", Long.class),
				record.get("default_parent_sc_model_id", String.class),
				record.get("default_parent_sc_code", String.class),
				record.get("default_parent_sc_shortname", String.class),
				record.get("default_parent_sc_longname", String.class)
			);

			dto.setParentScope(parentDTO);
			dto.setStartDate(record.get(SCOPE.START_DATE));
			dto.setStopDate(record.get(SCOPE.STOP_DATE));

			workflowsOnScopeModel.forEach(workflow -> {
				final var workflowId = workflow.getId();
				final var sqlWorkflowStateColumnAlias = sqlWorkflowsStateColumnAlias(workflowId);
				final var stateId = record.get(sqlWorkflowStateColumnAlias + ".state_id", String.class);
				if(stateId != null) {
					final var workflowStatusDTO = new WorkflowStatusDTO();
					workflowStatusDTO.setWorkflowId(workflowId);
					workflowStatusDTO.setStatusId(stateId);

					if(dto.getWorkflowStatuses() == null) {
						dto.setWorkflowStatuses(new ArrayList<>());
					}
					dto.getWorkflowStatuses().add(workflowStatusDTO);
				}
			});
			// retrieve field values
			searchableFieldsOnScopeModel.forEach(fieldModel -> {
				final var datasetModel = fieldModel.getDatasetModel();
				final var alias = sqlFieldValueColumnAlias(datasetModel.getId(), fieldModel.getId());
				final var fieldValue = record.get(alias, String.class);
				if(fieldValue != null) {
					if(dto.getFieldValues() == null) {
						dto.setFieldValues(new HashMap<>());
					}
					if(!dto.getFieldValues().containsKey(datasetModel.getId())) {
						dto.getFieldValues().put(datasetModel.getId(), new HashMap<>());
					}
					dto.getFieldValues().get(datasetModel.getId()).put(fieldModel.getId(), fieldValue);
				}
			});

			resultsByPk.put(dto.getPk(), dto);
		}

		final List<ExtendedScopeSearchResultDTO> results = new ArrayList<>();
		for(final var pk : pagePks) {
			final var dto = resultsByPk.get(pk);
			if(dto != null) {
				results.add(dto);
			}
		}

		return new PagedResult<>(results, pageSize, pageIndex, totalCount);
	}
}
