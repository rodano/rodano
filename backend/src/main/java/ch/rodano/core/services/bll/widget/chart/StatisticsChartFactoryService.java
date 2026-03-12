package ch.rodano.core.services.bll.widget.chart;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.math.NumberUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.configuration.model.chart.ChartRange;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.core.model.chart.ChartDatasetDTO;
import ch.rodano.core.model.chart.ChartDatasetPoint;
import ch.rodano.core.model.chart.Statistics;
import ch.rodano.core.model.jooq.tables.Dataset;
import ch.rodano.core.model.jooqutils.JOOQTranslator;
import ch.rodano.core.model.scope.FieldModelCriterion;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.study.StudyService;

import static ch.rodano.core.model.jooq.Tables.DATASET;
import static ch.rodano.core.model.jooq.Tables.FIELD;
import static ch.rodano.core.model.jooq.Tables.SCOPE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;

@Service
public class StatisticsChartFactoryService {
	private final StudyService studyService;
	private final DSLContext create;

	public StatisticsChartFactoryService(final StudyService studyService, final DSLContext create) {
		this.studyService = studyService;
		this.create = create;
	}

	private Optional<ChartRange> findRange(final Chart chart, final String key) {
		//try to find exact match
		for(final ChartRange range : chart.getValueRanges()) {
			if(Strings.CS.equals(range.getValue(), key)) {
				return Optional.of(range);
			}
		}
		//try to match range for numbers
		if(NumberUtils.isCreatable(key)) {
			for(final ChartRange range : chart.getNumericRanges()) {
				final double keyNumber = Double.parseDouble(key);
				if(range.getMin() <= keyNumber && keyNumber < range.getMax()) {
					return Optional.of(range);
				}
			}
		}
		//return "other" range if any
		return chart.getOtherRange();
	}

	public Map<String, Integer> processScopeResult(final Chart chart, final String[] languages, final Map<String, Integer> result) {
		//used a LinkedHashMap because the sort is important
		final var processedResult = new LinkedHashMap<String, Integer>();
		//initialize the map with the configured range
		//that's because we want to display all the ranges even those without data
		//also, that allows to initialize the map in the right order (ordered by range as defined in the configuration)
		for(final var range : chart.getRanges()) {
			processedResult.put(range.getLocalizedLabel(languages), 0);
		}
		for(final var entry : result.entrySet()) {
			final var range = findRange(chart, entry.getKey());
			final var key = range.map(r -> r.getLocalizedLabel(languages));
			//keep result only if it's part on a range
			if(key.isPresent()) {
				processedResult.merge(key.get(), entry.getValue(), (current, value) -> current + value);
			}
		}
		return processedResult;
	}

	public List<ChartDatasetDTO<String, Integer>> buildChartDatasets(final Chart chart, final String[] languages, final Collection<Scope> scopes, final List<FieldModelCriterion> criteria) {
		final var scopePks = scopes.stream().map(Scope::getPk).toList();
		final var now = ZonedDateTime.now();

		//conditions
		final List<Condition> conditions = new ArrayList<>();
		conditions.add(SCOPE.DELETED.isFalse());
		conditions.add(SCOPE.SCOPE_MODEL_ID.eq(chart.getLeafScopeModelId()));
		conditions.add(DATASET.DATASET_MODEL_ID.eq(chart.getDatasetModelId()));
		conditions.add(FIELD.FIELD_MODEL_ID.eq(chart.getFieldModelId()));
		conditions.add(SCOPE_ANCESTOR.ANCESTOR_FK.in(scopePks));
		conditions.add(SCOPE_ANCESTOR.START_DATE.lessOrEqual(now));
		conditions.add(SCOPE_ANCESTOR.END_DATE.isNull().or(SCOPE_ANCESTOR.END_DATE.greaterOrEqual(now)));

		final var total = DSL.count(SCOPE.PK).as("total");
		final var query = create
			.select(SCOPE_ANCESTOR.ANCESTOR_FK, FIELD.VALUE, total)
			.from(FIELD)
			.join(DATASET).on(FIELD.DATASET_FK.eq(DATASET.PK))
			.join(SCOPE).on(DATASET.SCOPE_FK.eq(SCOPE.PK))
			.join(SCOPE_ANCESTOR).on(DATASET.SCOPE_FK.eq(SCOPE_ANCESTOR.SCOPE_FK));

		//TODO this is very similar to ScopeDAOService::search

		//register all joins made to the dataset table to reuse them when possible
		//that's because filters can be applied to the same dataset
		//this does not work if multiple filters are applied to the same field, but that's ok
		//do not try to include the main field (Chart::getFieldModelId) in this cache to be able to apply a filter on it
		final var datasetJoins = new HashMap<String, Dataset>();

		for(final var criterion : criteria) {
			if(!datasetJoins.containsKey(criterion.datasetModelId())) {
				final var datasetJoin = DATASET.as(criterion.datasetModelId());
				datasetJoins.put(criterion.datasetModelId(), datasetJoin);
				query.join(datasetJoin).on(datasetJoin.SCOPE_FK.eq(SCOPE.PK).and(datasetJoin.DATASET_MODEL_ID.eq(criterion.datasetModelId())));
			}
			final var datasetJoin = datasetJoins.get(criterion.datasetModelId());
			final var fieldJoin = FIELD.as(criterion.fieldModelId());
			query.join(fieldJoin).on(fieldJoin.DATASET_FK.eq(datasetJoin.PK).and(fieldJoin.FIELD_MODEL_ID.eq(criterion.fieldModelId())));

			conditions.add(JOOQTranslator.translate(studyService.getStudy(), criterion, fieldJoin.VALUE));
		}

		query
			.where(DSL.and(conditions))
			.groupBy(SCOPE_ANCESTOR.ANCESTOR_FK, FIELD.VALUE)
			.orderBy(SCOPE_ANCESTOR.ANCESTOR_FK, FIELD.VALUE);

		final var results = query.fetchGroups(SCOPE_ANCESTOR.ANCESTOR_FK);

		//build chart dataset
		final List<ChartDatasetDTO<String, Integer>> datasets = new ArrayList<>();
		//sort scopes to be able to sort results
		final var sortedScopes = new ArrayList<>(scopes);
		sortedScopes.sort(Scope.DEPTH_COMPARATOR);
		for(final var scope : sortedScopes) {
			final var scopeResult = new LinkedHashMap<String, Integer>();

			//there may be no data for the scope
			if(results.containsKey(scope.getPk())) {
				for(final var record : results.get(scope.getPk())) {
					final var key = StringUtils.defaultIfBlank(record.value2(), "");
					scopeResult.put(key, record.value3());
				}
			}

			//calculate statistics
			Statistics statistics = null;
			if(OperandType.NUMBER.equals(chart.getFieldModel().getDataType())) {
				final var values = new ArrayList<Double>();
				for(final var entry : scopeResult.entrySet()) {
					if(NumberUtils.isCreatable(entry.getKey())) {
						final var value = Double.parseDouble(entry.getKey());
						final var weight = entry.getValue();
						//add value the number of times that it appears
						for(int i = 0; i < weight; i++) {
							values.add(value);
						}
					}
				}
				//values can be empty if the result contains only the NULL or BLANK value
				if(!values.isEmpty()) {
					statistics = new Statistics(values);
				}
			}

			//sort data according to chart range if any
			final var processedScopeResult = chart.hasRange() ? processScopeResult(chart, languages, scopeResult) : scopeResult;
			final var points = processedScopeResult.entrySet()
				.stream()
				.map(e -> new ChartDatasetPoint<>(e.getKey(), e.getValue()))
				.toList();

			final var dataset = new ChartDatasetDTO<String, Integer>(scope.getCodeAndShortname(), points, statistics);
			datasets.add(dataset);
		}

		return datasets;
	}
}
