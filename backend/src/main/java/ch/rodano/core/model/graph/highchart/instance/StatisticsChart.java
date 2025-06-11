package ch.rodano.core.model.graph.highchart.instance;

import java.util.Map;
import java.util.stream.Collectors;

import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.configuration.model.request.ResultType;
import ch.rodano.core.model.graph.RodanoSqlResult;

public class StatisticsChart extends HighGraph {
	private final Map<String, RodanoSqlResult> sqlResults;

	public StatisticsChart(final Chart configuration, final String language, final Map<String, RodanoSqlResult> sqlResults, final Map<String, String> values) {
		super(configuration, language, values);
		this.sqlResults = sqlResults;
	}

	@Override
	public String getType() {
		return "column";
	}

	@Override
	public String getDefaultTitle() {
		return "Statistics chart";
	}

	@Override
	public Map<String, Double> getMin() {
		return sqlResults.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getMin(), (_, b) -> b));
	}

	@Override
	public Map<String, Double> getMax() {
		return sqlResults.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getMax(), (_, b) -> b));
	}

	@Override
	public Map<String, Double> getPercentile5() {
		return sqlResults.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getPercentile(5), (_, b) -> b));
	}

	@Override
	public Map<String, Double> getPercentile95() {
		return sqlResults.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getPercentile(95), (_, b) -> b));
	}

	@Override
	public Map<String, Double> getCount() {
		return sqlResults.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getCount(), (_, b) -> b));
	}

	@Override
	public Map<String, Double> getMean() {
		return sqlResults.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getMean(), (_, b) -> b));
	}

	@Override
	public Map<String, Double> getSd() {
		return sqlResults.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getSd(), (_, b) -> b));
	}

	@Override
	public Map<String, Double> getMedian() {
		return sqlResults.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getPercentile(50), (_, b) -> b));
	}

	@Override
	public boolean isInPercent() {
		return configuration.getRequest().getResultType() != null && configuration.getRequest().getResultType().equals(ResultType.PERCENT);
	}
}
