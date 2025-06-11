package ch.rodano.core.model.graph.highchart.instance;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.configuration.model.predicate.ValueSource;
import ch.rodano.core.helpers.RequestHelperService;
import ch.rodano.core.model.graph.RodanoSqlResult;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.study.StudyService;

@Service
public class StatisticsChartFactoryService {
	private final StudyService studyService;
	private final RequestHelperService requestHelperService;

	public StatisticsChartFactoryService(final StudyService studyService, final RequestHelperService requestHelperService) {
		this.studyService = studyService;
		this.requestHelperService = requestHelperService;
	}

	private Map<String, RodanoSqlResult> buildResults(final Chart configuration, final List<Scope> scopes, final List<ValueSource> criteria) {
		final var leafScopeModel = studyService.getStudy().getLeafScopeModel();
		return scopes.stream().collect(
			Collectors.toMap(
				Scope::getCode,
				s -> requestHelperService.getResults(configuration.getRequest(), leafScopeModel, Collections.singletonList(s), configuration.isWithStatistics(), criteria)
			)
		);
	}

	private Map<String, String> generateValues(final Chart configuration, final List<Scope> scopes, final Map<String, RodanoSqlResult> sqlResults) {
		final Map<String, Double> series = new LinkedHashMap<>();
		final var values = new LinkedHashMap<String, String>();

		for(final var s : scopes) {
			final var scopeCode = s.getCode();
			final Map<String, Double> graphValue = new LinkedHashMap<>();

			//build series and initialize values
			//with ranges
			if(configuration.hasRange()) {
				configuration.getRanges().forEach(r -> {
					graphValue.put(r.getId(), 0.0);
					if(!series.containsKey(r.getId())) {
						series.put(r.getId(), 0.0);
					}
				});
			}
			else {
				//without ranges
				sqlResults.get(scopeCode).getGenericResult().keySet().forEach(key -> {
					if(!graphValue.containsKey(key)) {
						graphValue.put(key, null);
					}
					if(!series.containsKey(key)) {
						series.put(key, null);
					}
				});
			}

			//with ranges
			if(configuration.hasRange()) {
				graphValue.putAll(sqlResults.get(scopeCode).getResultsInPercentForRange(configuration.getRanges()));
			}
			else {
				//without ranges
				sqlResults.get(scopeCode).getGenericResult().forEach((key, value) -> {
					if(value != null) {
						graphValue.merge(key, value, Double::sum);
					}
				});
			}

			if(configuration.getFillDataGaps()) {
				if(!series.isEmpty()) {
					final var keys = series.keySet().toArray();

					//scope value map is sorted
					final var start = Integer.parseInt((String) keys[0]);
					final var stop = Integer.parseInt((String) keys[keys.length - 1]);

					IntStream.rangeClosed(start, stop).filter(i -> !series.containsKey(String.valueOf(i))).forEach(i -> series.put(String.valueOf(i), null));
				}
			}

			final var serie = new StringBuilder();
			final var size = graphValue.size();
			var i = 0;
			for(final var e : graphValue.entrySet()) {
				serie.append(String.format("%f", e.getValue()));

				if(i++ < size - 1) {
					serie.append(',');
				}
			}

			values.put(s.getCode(), serie.toString());
		}

		return values;
	}

	public StatisticsChart createChart(final Chart configuration, final String language, final List<Scope> scopes, final List<ValueSource> criteria) {
		final Map<String, RodanoSqlResult> sqlResults = buildResults(configuration, scopes, criteria);
		final var values = generateValues(configuration, scopes, sqlResults);
		return new StatisticsChart(configuration, language, sqlResults, values);
	}
}
