package ch.rodano.api.search;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.field.FieldDTOService;
import ch.rodano.api.scope.ScopeDTO;
import ch.rodano.api.scope.ScopeDTOService;
import ch.rodano.api.scope.ScopeTinyDTO;
import ch.rodano.api.workflow.WorkflowDTOService;
import ch.rodano.api.workflow.WorkflowStatusDTO;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.rules.Operator;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowState;
import ch.rodano.core.model.jooqutils.JOOQTranslator;
import ch.rodano.core.model.scope.FieldModelCriterion;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeSearch;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.dao.workflow.WorkflowStatusDAOService;
import ch.rodano.core.utils.ACL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static ch.rodano.core.model.jooq.Tables.SCOPE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;
import static ch.rodano.core.model.jooq.Tables.SCOPE_RELATION;
import static ch.rodano.core.model.jooq.Tables.WORKFLOW_STATUS;
import static ch.rodano.core.model.jooq.Tables.FIELD;
import static ch.rodano.core.model.jooq.Tables.DATASET;
import static org.jooq.impl.DSL.field;

@Service
@Transactional(readOnly = true)
public class ExtendedScopeResultServiceImpl implements ExtendedScopeResultService {

	private static final Logger log = LoggerFactory.getLogger(ExtendedScopeResultServiceImpl.class);

	private final ScopeDTOService scopeDTOService;
	private final FieldService fieldService;
	private final FieldDAOService fieldDAOService;
	private final WorkflowDTOService workflowDTOService;
	private final WorkflowStatusDAOService workflowStatusDAOService;
	private final FieldDTOService fieldDTOService;
	private final DatasetService datasetService;
	private final StudyService studyService;
	private final DSLContext create;

	public ExtendedScopeResultServiceImpl(
		final ScopeDTOService scopeDTOService,
		final FieldService fieldService, FieldDAOService fieldDAOService,
		final WorkflowDTOService workflowDTOService,
		final WorkflowStatusDAOService workflowStatusDAOService,
		final FieldDTOService fieldDTOService,
		final DatasetService datasetService,
		final StudyService studyService, DSLContext create) {
		this.scopeDTOService = scopeDTOService;
		this.fieldService = fieldService;
		this.fieldDAOService = fieldDAOService;
		this.workflowDTOService = workflowDTOService;
		this.workflowStatusDAOService = workflowStatusDAOService;
		this.fieldDTOService = fieldDTOService;
		this.datasetService = datasetService;
		this.studyService = studyService;
		this.create = create;
	}

	private static String sqlWorkflowsStateColumnAlias(String workflowId) {
		return String.format("%s_%s", "ws", workflowId);
	}

	private static String sqlFieldValueColumnAlias(String datasetModelId, String fieldId) {
		return String.format("%s.%s", datasetModelId.toLowerCase(), fieldId.toLowerCase());
	}

	private static List<FieldModelCriterion> defaultFieldCriteria(ScopeSearch search,
								      List<FieldModel> searchableFieldsOnScopeModel) {
		final var criteria = new ArrayList<FieldModelCriterion>();
		for (var fieldModel : searchableFieldsOnScopeModel) {
			final var fieldCriterion = new FieldModelCriterion(fieldModel,
				Operator.NOT_NULL, null);
			criteria.add(fieldCriterion);
		}
		return criteria;
	}

	private static Map<String, List<String>> defaultWorkflowStateCriteria(ScopeSearch search,
									      List<Workflow> workflowsOnScopeModel) {
		Map<String, List<String>> workflowStates = new HashMap<>();
		for (var workflow : workflowsOnScopeModel) {
			workflowStates.put(workflow.getId(), workflow.getStates().stream().map(WorkflowState::getId).toList());
			search.setWorkflowStates(Optional.of(workflowStates));
		}
		return workflowStates;
	}

	@Override
	public PagedResult<ExtendedScopeSearchResultDTO> search(final ScopeSearch search) {

		search.setScopeModelId(Optional.of("PATIENT"));

		final var scopeModel = studyService.getStudy().getScopeModel(search.getScopeModelId().orElse(null));
		final var now = ZonedDateTime.now();
		final List<Condition> conditions = new ArrayList<>();

		final var workflowsOnScopeModel = studyService.getStudy().getWorkflowsOnScopeModel(scopeModel.getId());
		final var searchableFieldsOnScopeModel = studyService.getStudy().getSearchableFieldsOnScopeModel(scopeModel.getId());

		// if no criteria are provided, add default criteria to only retrieve scopes
		// that have values for the searchable fields
		if (search.fieldModelCriteria.isEmpty()) {
			search.setFieldModelCriteria(Optional.of(defaultFieldCriteria(search, searchableFieldsOnScopeModel)));
		}

		// if no criteria are provided, add default criteria to only retrieve scopes
		// that have workflow statuses
		if (search.workflowStates.isEmpty()) {
			search.setWorkflowStates(Optional.of(defaultWorkflowStateCriteria(search, workflowsOnScopeModel)));
		}

		final var sqlWorkflowsStateColumnAlias = workflowsOnScopeModel.stream()
			.map(wf -> sqlWorkflowsStateColumnAlias(wf.getId()))
			.toList();

		// Add workflowIds as selected fields: ws_<workflowId>.*
		var selectFields = new ArrayList<>(List.of(SCOPE.asterisk(), DSL.count().over().as("total")));
		for (String sqlWorkflowsStateColumn : sqlWorkflowsStateColumnAlias) {
			// Expose workflow state with a stable alias so it can be used in ORDER BY
			final var workflowStateAlias = sqlWorkflowsStateColumn + ".state_id";
			selectFields.add(DSL.field(DSL.name(sqlWorkflowsStateColumn, "state_id")).as(workflowStateAlias));
		}

		// Add searchable field values to select list (will be populated during field criteria processing)
		for (var fieldModel : searchableFieldsOnScopeModel) {
			final var datasetModel = fieldModel.getDatasetModel();
			final var selectAlias = sqlFieldValueColumnAlias(datasetModel.getId(), fieldModel.getId());
			final var fieldAlias = datasetModel.getId().toLowerCase() + "_" + fieldModel.getId().toLowerCase();
			selectFields.add(DSL.field(fieldAlias + ".value").as(selectAlias));
		}

		final var sa = SCOPE_ANCESTOR.as("scope_ancestor");
		final var ancestor = SCOPE.as("ancestor");

		// add ancestor columns to the select list so they are available in the result
		selectFields.add(ancestor.PK.as("ancestor_pk"));
		selectFields.add(ancestor.ID.as("ancestor_id"));
		selectFields.add(ancestor.CODE.as("ancestor_code"));
		selectFields.add(ancestor.SHORTNAME.as("ancestor_shortname"));
		selectFields.add(ancestor.LONGNAME.as("ancestor_longname"));

		final var query = create.select(selectFields)
			.from(SCOPE)
			// join the scope_ancestor row (only default ancestor rows)
			.leftJoin(sa).on(SCOPE.PK.eq(sa.SCOPE_FK).and(sa.DEFAULT.isTrue()))
			// join the actual ancestor scope using the ancestor fk
			.leftJoin(ancestor).on(sa.ANCESTOR_FK.eq(ancestor.PK));


		// join tables if necessary
		search.getParentPks().ifPresent(parentPks -> {
			// perform an inner join because the goal is to filter scopes
			query.innerJoin(SCOPE_RELATION)
				.on(
					SCOPE.PK.eq(SCOPE_RELATION.SCOPE_FK)
						.and(SCOPE_RELATION.PARENT_FK.in(parentPks))
						.and(SCOPE_RELATION.END_DATE.isNull().or(SCOPE_RELATION.END_DATE.ge(now))));
		});

		search.getAncestorPks().ifPresent(ancestorPks -> {
			final var aliasedTable = SCOPE_ANCESTOR.as("SA");
			query.innerJoin(aliasedTable)
				.on(SCOPE.PK.eq(aliasedTable.SCOPE_FK).and(aliasedTable.ANCESTOR_FK.in(ancestorPks)));
			if (!search.getIncludeDeleted()) {
				conditions.add(aliasedTable.ANCESTOR_DELETED.isFalse());
			}
		});

		// one join per workflow filter is required
		search.getWorkflowStates().get().forEach((key, value) -> {
			final var workflowJoin = WORKFLOW_STATUS.as(sqlWorkflowsStateColumnAlias(key));
			// perform a left join to include scopes without workflow status rows
			query.leftJoin(workflowJoin).on(
				SCOPE.PK.eq(workflowJoin.SCOPE_FK)
					.and(workflowJoin.WORKFLOW_ID.eq(key))
					.and(workflowJoin.DELETED.isFalse()));
		});

		var workflowJoins = new ArrayList();
		if (!search.getWorkflowStates().get().isEmpty()) {
			for (final var entry : search.getWorkflowStates().get().entrySet()) {
				final var workflowJoin = WORKFLOW_STATUS.as(sqlWorkflowsStateColumnAlias(entry.getKey() + ".state_id"));
				// perform an inner join to only include scopes that have matching workflow
				// status rows
				workflowJoins.add(workflowJoin);

				query.innerJoin(workflowJoin).on(
					SCOPE.PK.eq(workflowJoin.SCOPE_FK)
						.and(workflowJoin.WORKFLOW_ID.eq(entry.getKey()))
						.and(workflowJoin.DELETED.isFalse())
						.and(workflowJoin.STATE_ID.in(entry.getValue())));
			}
		}

		// retrieve all workflow statuses for the workflows on the scope model

		workflowsOnScopeModel.forEach(workflow -> {

			final var workflowJoin =
				WORKFLOW_STATUS.as(sqlWorkflowsStateColumnAlias(workflow.getId()));
			if (workflowJoins.contains(
				WORKFLOW_STATUS.as(sqlWorkflowsStateColumnAlias(workflow.getId() + ".state_id")))) {
				return; // already joined above
			}
			// perform a left join to include scopes without workflow status rows
			query.leftJoin(workflowJoin).on(
				SCOPE.PK.eq(workflowJoin.SCOPE_FK)
					.and(workflowJoin.WORKFLOW_ID.eq(workflow.getId()))
					.and(workflowJoin.DELETED.isFalse()));
		});


		search.getFieldModelCriteria().ifPresent(criteria -> {
			// Track dataset tables so we can reference them in field joins
			final Map<String, org.jooq.Table<?>> datasetTables = new HashMap<>();

			// Collect all dataset models that have searchable fields
			for (final var fieldModel : searchableFieldsOnScopeModel) {
				final var datasetModel = fieldModel.getDatasetModel();
				final var datasetModelId = datasetModel.getId();
				final var datasetAlias = "dataset_" + datasetModelId.toLowerCase();

				// Join DATASET table once per dataset model (check if already joined by criteria)
				if (!datasetTables.containsKey(datasetModelId)) {
					final var datasetTable = DATASET.as(datasetAlias);
					datasetTables.put(datasetModelId, datasetTable);
					query.innerJoin(datasetTable).on(
						SCOPE.PK.eq(datasetTable.SCOPE_FK)
							.and(datasetTable.DATASET_MODEL_ID.eq(datasetModelId)));
				}

				// Join FIELD table for each searchable field (with or without criteria)
				final var fieldAlias = datasetModelId.toLowerCase() + "_" + fieldModel.getId().toLowerCase();
				final var fieldTable = FIELD.as(fieldAlias);
				final var datasetTable = datasetTables.get(datasetModelId);

				// Check if this field has a criterion
				final var criterionOpt = criteria.stream()
					.filter(c -> c.datasetModelId().equals(datasetModelId) && c.fieldModelId().equals(fieldModel.getId()))
					.findFirst();

				if (criterionOpt.isPresent()) {
					// Field has a criterion: join with the condition
					final var criterion = criterionOpt.get();
					final var operator = criterion.operator();
					final var criterionValue = criterion.value();

					final var operatedFieldName = String.format("%s.value", fieldAlias);
					final var operatedField = field(operatedFieldName, String.class);
					final var fieldCondition = JOOQTranslator.translate(operator, fieldModel, operatedField, criterionValue);

					query.innerJoin(fieldTable).on(
						fieldTable.DATASET_FK.eq(datasetTable.field("pk", Long.class))
							.and(fieldTable.FIELD_MODEL_ID.eq(fieldModel.getId()))
							.and(fieldCondition));
				} else {
					// Field has no criterion: join without condition (just to populate select values)
					query.leftJoin(fieldTable).on(
						fieldTable.DATASET_FK.eq(datasetTable.field("pk", Long.class))
							.and(fieldTable.FIELD_MODEL_ID.eq(fieldModel.getId())));
				}
			}
		});

		// restrict search according to rights and manage the scope models
		search.getScopeModelId().ifPresent(scopeModelId -> {
			conditions.add(SCOPE.SCOPE_MODEL_ID.eq(scopeModelId));
		});

		search.getScopeModelAncestorPks().ifPresent(ancestorPks -> {
			if (ancestorPks.size() > 0) {
				final List<Condition> scopeConditions = new ArrayList<>();
				for (final var entry : ancestorPks.entrySet()) {
					scopeConditions.add(
						SCOPE.PK.in(entry.getValue())
							.or(SCOPE_ANCESTOR.ANCESTOR_FK.in(entry.getValue()))
							.and(SCOPE.SCOPE_MODEL_ID.eq(entry.getKey())));
				}
				conditions.add(DSL.or(scopeConditions));
			}
		});

		if (!search.getIncludeDeleted()) {
			// if we exclude the deleted scopes, then we also exclude the scopes that
			// descend from a deleted scope
			conditions.add(
				SCOPE.DELETED.isFalse()
					// scope ancestor table is null for the root scope
					.and(SCOPE_ANCESTOR.ANCESTOR_FK.isNull().or(SCOPE_ANCESTOR.ANCESTOR_DELETED.isFalse())));
		}

		search.getPks().ifPresent(pks -> {
			conditions.add(SCOPE.PK.in(pks));
		});

		search.getIds().ifPresent(ids -> {
			conditions.add(SCOPE.ID.in(ids));
		});

		search.getCode().ifPresent(code -> {
			conditions.add(SCOPE.CODE.containsIgnoreCase(code));
		});

		search.getLeaf().ifPresent(leaf -> {
			final var leafId = studyService.getStudy().getLeafScopeModel().getId();
			if (leaf) {
				conditions.add(SCOPE.SCOPE_MODEL_ID.eq(leafId));
			} else {
				conditions.add(SCOPE.SCOPE_MODEL_ID.notEqual(leafId));
			}
		});

		search.getFullText().ifPresent(fullText -> {
			conditions.add(SCOPE.CODE.containsIgnoreCase(fullText)
				.or(SCOPE.SHORTNAME.containsIgnoreCase(fullText))
				.or(SCOPE.LONGNAME.containsIgnoreCase(fullText)));
		});

		// group results by unique scope pk to avoid duplicates
		query.where(conditions).groupBy(SCOPE.PK);
		
		// Apply sorting - use extendedSortBy if provided, otherwise use default sortBy
		if (search.getExtendedSortBy() != null && !search.getExtendedSortBy().isEmpty()) {
			// Dynamic field sorting
			var sortTarget = search.getExtendedSortBy();
			final var workflowIds = workflowsOnScopeModel.stream().map(Workflow::getId).toList();
			final var fieldIds = searchableFieldsOnScopeModel.stream().map(f -> f.getId().toLowerCase()).toList();
			// Normalize workflow sort keys to the exposed select alias using sqlWorkflowsStateColumnAlias
			if (!sortTarget.contains(".")) { //Workflos state sort
				if (workflowIds.contains(sortTarget)) {
					sortTarget = sqlWorkflowsStateColumnAlias(sortTarget) + ".state_id";
				} else if (sortTarget.startsWith("ws_")) {
					sortTarget = sortTarget + ".state_id";
				}
			} else { // Field sort
				final var parts = sortTarget.split("\\.");
				if (parts.length == 2 && fieldIds.contains(parts[1])) {
					var sortColumn = DSL.field(String.format("%s_%s.value",parts[0], parts[1]));
					// get configuration to check field type
					var fieldmodel = studyService.getStudy().getDatasetModel(parts[0]).getFieldModel(parts[1]);
					if (FieldModelType.NUMBER.equals(fieldmodel.getType())) {
						// cast to double for numeric sorting
						sortTarget = "CAST(" + DSL.field(sortColumn) + " AS DOUBLE)";
					}
					else if (FieldModelType.DATE.equals(fieldmodel.getType())) {
						// cast to date for date sorting
						sortTarget = "STR_TO_DATE(" + sortColumn + ", '" + JOOQTranslator.SQL_FIELD_DATE_FORMAT.getValue() + "')";

					}
					else {
						sortTarget = sortColumn.getName();
					}
				}
			}

			final var sortField = DSL.field((sortTarget));

			query.orderBy(search.getOrder() == org.jooq.SortOrder.ASC ? sortField.asc() : sortField.desc());
		} else {
			// Standard field sorting
			query.orderBy(search.getSortBy().getField().sort(search.getOrder()));
		}
		
		query.limit(search.getLimitField())
			.offset(search.getOffsetField());

		final var result = query.fetch();
		var total = 0;
		if (result.size() > 0) {
			total = result.get(0).getValue("total", Integer.class);
		}
		var records = query.fetch();
		List<ExtendedScopeSearchResultDTO> results = new ArrayList<>();

		for (var record : records) {
			ExtendedScopeSearchResultDTO dto = new ExtendedScopeSearchResultDTO();
			ScopeDTO scopeDTO = new ScopeDTO();
			scopeDTO.setCode(record.get("code").toString());
			scopeDTO.setPk(Long.parseLong(record.get("pk").toString()));
			scopeDTO.setId(record.get("id").toString());
			scopeDTO.setShortname(record.get("shortname").toString());
			scopeDTO.setLongname(record.get("longname") != null ? record.get("longname").toString() : null);
			scopeDTO.setModelId(record.get("scope_model_id").toString());
			scopeDTO.setVirtual((Boolean) record.get("virtual"));
			final ScopeTinyDTO ancestorDTO = new ScopeTinyDTO(
				record.get("ancestor_pk", Long.class),
				record.get("ancestor_id", String.class),
				record.get("ancestor_code", String.class),
				record.get("ancestor_shortname", String.class),
				record.get("ancestor_longname", String.class)
			);

			scopeDTO.setParentScope(ancestorDTO);

			// TODO fix date parsing
			scopeDTO.setStartDate(
				record.get("start_date") != null ? ZonedDateTime.parse(record.get("start_date").toString()) : null);
			scopeDTO.setStartDate(
				record.get("stop_date") != null ? ZonedDateTime.parse(record.get("stop_date").toString()) : null);

			dto.setScope(scopeDTO);
			workflowsOnScopeModel.forEach(workflow -> {
				final var workflowId = workflow.getId();
				final var sqlWorkflowStateColumnAlias = sqlWorkflowsStateColumnAlias(workflowId);
				final var stateId = record.get(sqlWorkflowStateColumnAlias + ".state_id", String.class);
				if (stateId != null) {
					final var workflowStatusDTO = new WorkflowStatusDTO();
					workflowStatusDTO.setWorkflowId(workflowId);
					if (dto.getWorkflowStatuses() == null) {
						dto.setWorkflowStatuses(new ArrayList<>());
					}
					dto.getWorkflowStatuses().add(Collections.singletonMap(workflowId, stateId));
				}
			});
			// retrieve field values
			searchableFieldsOnScopeModel.forEach(fieldModel -> {
				final var datasetModel = fieldModel.getDatasetModel();
				final var alias = sqlFieldValueColumnAlias(datasetModel.getId(), fieldModel.getId());
				final var fieldValue = record.get(alias, String.class);
				if (fieldValue != null) {
					if (dto.getFieldValues() == null) {
						dto.setFieldValues(new HashMap<>());
					}
					if (!dto.getFieldValues().containsKey(datasetModel.getId())) {
						dto.getFieldValues().put(datasetModel.getId(), new HashMap<>());
					}
					dto.getFieldValues().get(datasetModel.getId()).put(fieldModel.getId(), fieldValue);
				}
			});

			results.add(dto);
		}
		return new PagedResult<>(results, search.getPageSize(), search.getPageIndex(), total);
	}

	@Override
	public ExtendedScopeSearchResultDTO createDTO(Scope scope, List<ACL> acls) {
		return null;
	}

}
