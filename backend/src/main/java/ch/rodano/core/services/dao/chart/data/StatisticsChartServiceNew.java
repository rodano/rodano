package ch.rodano.core.services.dao.chart.data;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.scope.FieldModelCriterion;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.chart.ChartDTO;
import ch.rodano.core.services.dao.scope.ScopeDAOService;

import static ch.rodano.core.model.jooq.Tables.DATASET;
import static ch.rodano.core.model.jooq.Tables.FIELD;
import static ch.rodano.core.model.jooq.Tables.SCOPE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;
import static org.jooq.impl.DSL.val;

@Service
public class StatisticsChartServiceNew {

	private final DSLContext dsl;
	private final ScopeDAOService scopeDAOService;
	private final StudyService studyService;

	public StatisticsChartServiceNew(
		final DSLContext dsl,
		final ScopeDAOService scopeDAOService,
		final StudyService studyService
	) {
		this.dsl = dsl;
		this.scopeDAOService = scopeDAOService;
		this.studyService = studyService;
	}

	public List<ChartDTO.ChartDataSeries> generateValues(
		final String scopeModelId,
		final String leafScopeModelId,
		final String datasetModelId,
		final String fieldModelId,
		final String eventModelId,
		final boolean showOtherCategory,
		final List<ChartDTO.Category> categories,
		final List<String> selectedRootScopeIds,
		final List<FieldModelCriterion> criteria
	) {
		// 1) Determining the root scopes, which represents the entities we want to analyze or the entities the current
		// user has access to
		final var scopesToAnalyze = determineRootScopes(scopeModelId, selectedRootScopeIds);

		final Map<String, Map<String, Integer>> result = new java.util.LinkedHashMap<>();

		// 2) looping through each root scope we want to analyze and extract the counts for the desired field
		for (var scope : scopesToAnalyze) {
			final var rawCounts = eventModelId != null
				? getGroupedFieldValuesViaEvent(scope, leafScopeModelId, datasetModelId, fieldModelId, eventModelId, criteria)
				: getGroupedFieldValues(scope, leafScopeModelId, datasetModelId, fieldModelId, criteria);

			// 3) If categories are defined, categorize the raw counts based on the defined ranges
			final var categorizedCounts = !categories.isEmpty()
				? applyCategoriesToRawData(rawCounts, categories, showOtherCategory)
				: rawCounts;

			result.put(scope.getCode(), categorizedCounts);
		}

		// Initialize the list that will hold the final chart data series
		final List<ChartDTO.ChartDataSeries> seriesList = new ArrayList<>();

		// Determine whether the chart uses predefined categories
		final var noCategories = categories.isEmpty();

		// Iterate through the result map where:
		// - key = label for the data series (e.g., a scope code)
		// - value = map of data points, where each key is a category/value and each value is the count
		for (var entry : result.entrySet()) {
			final var label = entry.getKey(); // e.g., "GERMANY", "FRANCE", etc.
			final List<List<Object>> values = new ArrayList<>();

			// For each category or value in the inner map, transform it into a [x, y] row
			for (var inner : entry.getValue().entrySet()) {
				final var x = getX(inner, noCategories);

				// Add the row [x, y] where x = category/value, y = count
				values.add(List.of(x, inner.getValue()));
			}

			// Create a ChartDataSeries for this label and its values
			seriesList.add(new ChartDTO.ChartDataSeries(label, values));
		}

		// Return the fully constructed list of series
		return seriesList;

	}

	private static Object getX(final Map.Entry<String, Integer> inner, final boolean noCategories) {
		Object x = inner.getKey(); // This could be a numeric string, text label, etc.

		// If no categories are defined, try parsing the key as a number to format nicely
		if (noCategories) {
			try {
				final var d = Double.parseDouble(inner.getKey());
				// If it's a whole number, cast to int (e.g., "42.0" -> 42)
				if (d == Math.rint(d)) {
					x = (int) d;
				}
			}
			catch (NumberFormatException ignored) {
				// Leave x as-is if it's not a number (e.g., "MALE", "FEMALE")
			}
		}
		return x;
	}

	/**
	 * Determines the root scopes based on the provided scope model ID and the selected root scope IDs.
	 * If the list of selected root scope IDs is non-empty, it retrieves the scopes corresponding to those IDs.
	 * Otherwise, it retrieves the scopes associated with the given scope model ID.
	 *
	 * @param scopeModelId the ID of the scope model used to retrieve the scopes when no specific root scope IDs are provided
	 * @param selectedRootScopeIds the list of specific root scope IDs; if non-empty, these IDs will be used to retrieve scopes
	 * @return a list of scopes based on the provided scope model ID or the selected root scope IDs
	 */
	private List<Scope> determineRootScopes(final String scopeModelId, final List<String> selectedRootScopeIds) {
		if (selectedRootScopeIds != null && !selectedRootScopeIds.isEmpty()) {
			return selectedRootScopeIds.stream().map(scopeDAOService::getScopeById).toList();
		}
		else {
			return scopeDAOService.getScopesByScopeModelId(scopeModelId);
		}
	}

	/**
	 * Retrieves a map of grouped field values and their respective counts based on the given scope, model IDs,
	 * and criteria. This method constructs and executes a database query to fetch the required data.
	 *
	 * @param rootScope the root scope used as the basis for the query
	 * @param leafScopeModelId the model ID of the leaf scope
	 * @param datasetModelId the model ID of the dataset
	 * @param fieldModelId the model ID of the field
	 * @param criteria a list of criteria to filter the results
	 * @return a map where the key is the field value (String) and the value is the count (Integer) of occurrences
	 */
	private Map<String, Integer> getGroupedFieldValues(
		final Scope rootScope,
		final String leafScopeModelId,
		final String datasetModelId,
		final String fieldModelId,
		final List<FieldModelCriterion> criteria
	) {
		final var s = SCOPE.as("s");
		final var sa = SCOPE_ANCESTOR.as("sa");
		final var d = DATASET.as("d");
		final var f = FIELD.as("f");

		final var now = ZonedDateTime.now(ZoneOffset.UTC);
		final Field<ZonedDateTime> nowField = val(now);

		var queryBuilder = dsl
			.select(f.VALUE.as("value"), DSL.count().as("cnt"))
			.from(s)
			.join(sa).on(
				sa.SCOPE_FK.eq(s.PK)
					.and(sa.ANCESTOR_FK.eq(rootScope.getPk()))
					.and(sa.START_DATE.lt(nowField))
					.and(sa.END_DATE.isNull().or(sa.END_DATE.gt(nowField)))
			)
			.join(d).on(d.SCOPE_FK.eq(s.PK))
			.join(f).on(f.DATASET_FK.eq(d.PK));

		// Conditions
		final List<Condition> conditions = new ArrayList<>();
		conditions.add(s.SCOPE_MODEL_ID.eq(leafScopeModelId));
		conditions.add(d.DATASET_MODEL_ID.eq(datasetModelId));
		conditions.add(f.FIELD_MODEL_ID.eq(fieldModelId));

		// === Dataset-level criteria ===
		final var datasetCriteria = buildFieldCriteriaJoins(criteria);
		for (var cj : datasetCriteria) {
			queryBuilder = queryBuilder.join(FIELD.as(cj.alias())).on(cj.joinCondition());
			conditions.add(cj.filterCondition());
		}

		// === Scope-level criteria ===
		final var scopeCriteriaJoins = buildScopeCriteriaJoins(leafScopeModelId, criteria);
		for (var cj : scopeCriteriaJoins) {
			final var dAlias = "d_" + cj.alias();
			queryBuilder = queryBuilder
				.join(DATASET.as(dAlias)).on(DATASET.as(dAlias).SCOPE_FK.eq(s.PK))
				.join(FIELD.as(cj.alias())).on(FIELD.as(cj.alias()).DATASET_FK.eq(DATASET.as(dAlias).PK));
			conditions.add(cj.filterCondition());
		}

		// Finalize
		final var query = queryBuilder
			.where(DSL.and(conditions))
			.groupBy(f.VALUE)
			.orderBy(f.VALUE);

		return fetchGroupedValues(query);
	}


	/**
	 * Retrieves a map of grouped field values and their corresponding counts based on the given parameters,
	 * filtering and grouping data via associated events.
	 *
	 * @param rootScope the root scope used to filter the data hierarchy
	 * @param leafScopeModelId the ID of the leaf scope model to filter the query
	 * @param datasetModelId the dataset model ID used to refine data selection
	 * @param fieldModelId the field model ID representing the field to group by
	 * @param eventModelId the event model ID to link field data through events
	 * @param criteria a list of field model criteria to apply additional filtering on datasets
	 * @return a map where keys are the grouped field values and values are their respective counts
	 */
	private Map<String, Integer> getGroupedFieldValuesViaEvent(
		final Scope rootScope,
		final String leafScopeModelId,
		final String datasetModelId,
		final String fieldModelId,
		final String eventModelId,
		final List<FieldModelCriterion> criteria
	) {
		final var s = SCOPE.as("s");
		final var sa = SCOPE_ANCESTOR.as("sa");
		final var e = ch.rodano.core.model.jooq.Tables.EVENT.as("e");
		final var d = DATASET.as("d");
		final var f = FIELD.as("f");

		final var now = ZonedDateTime.now(ZoneOffset.UTC);
		final Field<ZonedDateTime> nowField = val(now);

		// Base query: join SCOPE, SCOPE_ANCESTOR, EVENT, DATASET, FIELD
		var queryBuilder = dsl
			.select(f.VALUE.as("value"), DSL.count().as("cnt"))
			.from(s)
			.join(sa).on(
				sa.SCOPE_FK.eq(s.PK)
					.and(sa.ANCESTOR_FK.eq(rootScope.getPk()))
					.and(sa.START_DATE.lt(nowField))
					.and(sa.END_DATE.isNull().or(sa.END_DATE.gt(nowField)))
			)
			.join(e).on(e.SCOPE_FK.eq(s.PK))
			.join(d).on(d.EVENT_FK.eq(e.PK))
			.join(f).on(f.DATASET_FK.eq(d.PK));

		// Conditions list
		final List<Condition> conditions = new java.util.ArrayList<>();
		conditions.add(s.SCOPE_MODEL_ID.eq(leafScopeModelId));
		conditions.add(e.EVENT_MODEL_ID.eq(eventModelId));
		conditions.add(d.DATASET_MODEL_ID.eq(datasetModelId));
		conditions.add(f.FIELD_MODEL_ID.eq(fieldModelId));

		// === Dataset-level criteria ===
		final var criterionJoins = buildFieldCriteriaJoins(criteria);
		for (var cj : criterionJoins) {
			queryBuilder = queryBuilder.join(FIELD.as(cj.alias())).on(cj.joinCondition());
			conditions.add(cj.filterCondition());
		}

		// === Scope-level criteria ===
		final var scopeCriteriaJoins = buildScopeCriteriaJoins(leafScopeModelId, criteria);
		for (var cj : scopeCriteriaJoins) {
			final var dAlias = "d_" + cj.alias();
			queryBuilder = queryBuilder
				.join(DATASET.as(dAlias)).on(DATASET.as(dAlias).SCOPE_FK.eq(s.PK))
				.join(FIELD.as(cj.alias())).on(FIELD.as(cj.alias()).DATASET_FK.eq(DATASET.as(dAlias).PK));
			conditions.add(cj.filterCondition());
		}


		// Finalize query
		final var query = queryBuilder
			.where(DSL.and(conditions))
			.groupBy(f.VALUE)
			.orderBy(f.VALUE);

		return fetchGroupedValues(query);
	}




	/**
	 * Fetches and transforms the results of a database query into a map where each key is a
	 * string value (fetched from the query's "value" field) and its corresponding value is
	 * an integer count (fetched from the query's "cnt" field).
	 *
	 * @param query the database query to be executed and processed
	 * @return a map containing the grouped values and their respective count
	 */
	private Map<String, Integer> fetchGroupedValues(final org.jooq.Select<?> query) {
		final Map<String, Integer> result = new java.util.LinkedHashMap<>();
		query.fetch().forEach(record -> {
			final var val = record.get("value", String.class);
			final var count = record.get("cnt", Integer.class);
			result.put(val != null ? val : "", count);
		});
		return result;
	}

	/**
	 * Categorizes raw data counts into predefined categories and optionally includes an "Other" category for
	 * values that do not match any category.
	 *
	 * @param rawCounts a map containing raw data values as keys and their associated counts as values
	 * @param categories a list of categories defining the label and range for categorization
	 * @param showOtherCategory a flag indicating if unmatched values should be included in an "Other" category
	 * @return a map where keys are category labels (or "Other", if applicable) and values are the aggregated counts
	 */
	private Map<String, Integer> applyCategoriesToRawData(
		final Map<String, Integer> rawCounts,
		final List<ChartDTO.Category> categories,
		final boolean showOtherCategory
	) {
		final Map<String, Integer> categorized = new java.util.LinkedHashMap<>();
		var otherCount = 0;

		// Initialize all category labels to 0
		for (var cat : categories) {
			if (Boolean.TRUE.equals(cat.getShow())) {
				categorized.put(cat.getLabel(), 0);
			}
		}

		for (var entry : rawCounts.entrySet()) {
			final var rawValue = entry.getKey();
			final int count = entry.getValue();

			var matched = false;
			final double value;

			try {
				value = Double.parseDouble(rawValue);
			}
			catch (NumberFormatException e) {
				// Can't categorize non-numeric values
				if (showOtherCategory) {
					otherCount += count;
				}
				continue;
			}

			for (var cat : categories) {
				if (!Boolean.TRUE.equals(cat.getShow())) {
					continue;
				}
				try {
					final var min = Double.parseDouble(cat.getMin());
					final var max = Double.parseDouble(cat.getMax());
					if (value >= min && value <= max) {
						categorized.merge(cat.getLabel(), count, Integer::sum);
						matched = true;
						break;
					}
				}
				catch (NumberFormatException ignored) {
					// Skip invalid category ranges
				}
			}

			if (!matched && showOtherCategory) {
				otherCount += count;
			}
		}

		if (showOtherCategory) {
			categorized.put("Other", otherCount);
		}

		return categorized;
	}

	private record CriterionJoin(String alias, Condition joinCondition, Condition filterCondition) {}

	/**
	 * Builds a list of CriterionJoin objects based on the provided criteria.
	 * Each CriterionJoin consists of the join condition and the criterion condition
	 * used for filtering results based on the given field model criteria.
	 *
	 * @param criteria     the list of FieldModelCriterion objects defining the filtering conditions
	 *                     and their validity
	 * @return a list of CriterionJoin objects, each containing a join condition
	 * and its corresponding criterion condition
	 */
	private List<CriterionJoin> buildFieldCriteriaJoins(
		final List<FieldModelCriterion> criteria
	) {
		final List<CriterionJoin> joins = new ArrayList<>();
		final var study = studyService.getStudy();

		var counter = 0;
		for (var criterion : criteria) {
			if (!criterion.isValid()) {
				continue;
			}

			// SKIP scope-level criteria, handled separately
			if (criterionAppliesToScope(criterion, "d")) {
				continue;
			}

			final var alias = "fc_" + counter++;
			final var f = FIELD.as(alias);

			final var datasetPkField = DSL.field(DSL.name("d", "pk"), Long.class);

			final var joinCond = f.DATASET_FK.eq(datasetPkField);
			final var filterCond = DSL.and(
				f.FIELD_MODEL_ID.eq(criterion.fieldModelId()),
				criterion.toCondition(dsl, study, alias)
			);

			joins.add(new CriterionJoin(alias, joinCond, filterCond));
		}

		return joins;
	}


	/**
	 * Builds a list of CriterionJoin objects that represent scope-level criteria joins
	 * based on the given scope alias, leaf scope model ID, and a list of field model criteria.
	 *
	 * @param leafScopeModelId the identifier of the leaf scope model used to determine applicable criteria
	 * @param criteria         the list of FieldModelCriterion objects to be processed and converted into join and filter conditions
	 * @return a list of CriterionJoin objects defining the join and filter conditions for the given scope and criteria
	 */
	private List<CriterionJoin> buildScopeCriteriaJoins(
		final String leafScopeModelId,
		final List<FieldModelCriterion> criteria
	) {
		final List<CriterionJoin> joins = new ArrayList<>();
		final var study = studyService.getStudy();
		var counter = 0;

		for (var criterion : criteria) {
			if (!criterion.isValid()) {
				continue;
			}

			// Only apply to scope-level datasets (e.g., PATIENT), not the dataset of the field being aggregated
			if (!criterionAppliesToScope(criterion, leafScopeModelId)) {
				continue;
			}

			final var alias = "sc_" + counter++;
			final var f = FIELD.as(alias);
			final var d = DATASET.as("d_" + alias);

			// Join FIELD to DATASET via f.DATASET_FK.eq(d.PK)
			// AND join DATASET to SCOPE via d.SCOPE_FK.eq(s.PK)
			final var joinCondition = f.DATASET_FK.eq(d.PK)
				.and(d.SCOPE_FK.eq(DSL.field(DSL.name("s", "pk"), Long.class)));

			final var filterCondition = DSL.and(
				d.DATASET_MODEL_ID.eq(criterion.datasetModelId()),
				f.FIELD_MODEL_ID.eq(criterion.fieldModelId()),
				criterion.toCondition(dsl, study, alias)
			);

			joins.add(new CriterionJoin(alias, joinCondition, filterCondition));
		}

		return joins;
	}


	/**
	 * Determines whether a given criterion applies to a specific scope based on the dataset model ID.
	 *
	 * @param criterion          the criterion to evaluate, including its associated dataset model ID
	 * @param leafScopeModelId   the identifier of the leaf scope model to compare against
	 * @return true if the criterion's dataset model ID is not null and does not match the leaf scope model ID (case-insensitive); false otherwise
	 */
	private boolean criterionAppliesToScope(final FieldModelCriterion criterion, final String leafScopeModelId) {
		return criterion.datasetModelId() != null && !criterion.datasetModelId().equalsIgnoreCase(leafScopeModelId);
	}

}
