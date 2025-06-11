package ch.rodano.core.model.graph;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ch.rodano.configuration.model.chart.ChartRange;

public class RodanoSqlResult {

	//there properties are set using the results of the request
	private Map<String, Double> resultCount;

	private Map<String, Double> resultCountForRange;
	private Map<String, Double> resultsInPercent;
	private Map<String, Double> resultsInPercentForRange;

	private boolean inPercent;

	private Double min;
	private Double max;
	private Double count;
	private Double mean;
	private Double sd;

	private DescriptiveStatistics stats;

	private boolean isInPercent() {
		return inPercent;
	}

	public void setInPercent(final boolean inPercent) {
		this.inPercent = inPercent;
	}

	private Map<String, Double> getResultCount() {
		return resultCount;
	}

	public void setResultCount(final Map<String, Double> resultCount) {
		this.resultCount = resultCount;
	}

	private ChartRange findRange(final Collection<ChartRange> ranges, final String key) {
		//look all ranges that have a value
		for(final ChartRange range : ranges) {
			if(range.getOther()) {
				return range;
			}

			if(NumberUtils.isCreatable(key)) {
				if(StringUtils.equals(range.getValue(), key)) {
					return range;
				}

				final double keyNumber = Double.parseDouble(key);
				if(range.getMin() <= keyNumber && keyNumber < range.getMax()) {
					return range;
				}
			}
			else if(key != null && key.equals(range.getValue()) && range.getMin() == 0 && range.getMax() == 0) {
				return range;
			}
		}

		return null;
	}

	//following three methods can not be merged because some returns a map with int and others with double
	//it is not possible to use abstract class "Number" as there is now definition of "add" or "+" for this class
	public Map<String, Double> getResultCountForRange(final Collection<ChartRange> ranges) {
		if(ranges == null || ranges.isEmpty()) {
			return resultCount;
		}

		//cache results
		if(resultCountForRange == null) {
			resultCountForRange = new LinkedHashMap<>();

			//initialize for all ranges
			for(final var range : ranges) {
				resultCountForRange.put(range.getId(), 0.0);
			}

			for(final var entry : resultCount.entrySet()) {
				final var range = findRange(ranges, entry.getKey());
				if(range != null) {
					resultCountForRange.put(range.getId(), resultCountForRange.get(range.getId()) + entry.getValue());
				}
				else {
					System.err.printf("Value [%s] does not match any range and has been excluded from results%n", entry.getValue());
				}
			}
		}
		return resultCountForRange;
	}

	public Map<String, Double> getResultsInPercentForRange(final Collection<ChartRange> ranges) {
		if(ranges == null || ranges.isEmpty()) {
			return resultsInPercent;
		}

		//cache results
		if(resultsInPercentForRange == null) {
			resultsInPercentForRange = new LinkedHashMap<>();

			//initialize for all ranges
			for(final var range : ranges) {
				resultsInPercentForRange.put(range.getId(), 0.0);
			}

			for(final var entry : getResultsInPercent().entrySet()) {
				final var range = findRange(ranges, entry.getKey());
				if(range != null) {
					resultsInPercentForRange.put(range.getId(), resultsInPercentForRange.get(range.getId()) + entry.getValue());
				}
				else {
					System.err.printf("Value [%s] does not match any range and has been excluded from results%n", entry.getValue());
				}
			}
		}
		return resultsInPercentForRange;
	}

	private Map<String, Double> getResultsInPercent() {
		if(resultsInPercent == null) {

			resultsInPercent = new LinkedHashMap<>();
			for(final var entry : getResultCount().entrySet()) {
				resultsInPercent.put(entry.getKey(), entry.getValue() / getCount() * 100);
			}
		}
		return resultsInPercent;
	}

	public Map<String, Double> getGenericResult() {
		if(isInPercent()) {
			return getResultsInPercent();
		}
		return getResultCount();
	}

	public Double getMin() {
		if(min == null) {
			min = getStats().getMin();
		}
		return min;
	}

	public Double getMax() {
		if(max == null) {
			max = getStats().getMax();
		}
		return max;
	}

	public Double getPercentile(final double percentile) {
		return getStats().getPercentile(percentile);
	}

	public Double getCount() {
		if(count == null) {
			count = 0.0;
			for(final double i : resultCount.values()) {
				count += i;
			}
		}
		return count;
	}

	public Double getMean() {
		if(mean == null) {
			mean = getStats().getMean();
		}
		return mean;
	}

	public Double getSd() {
		if(sd == null) {
			sd = getStats().getStandardDeviation();
		}
		return sd;
	}

	private DescriptiveStatistics getStats() {
		if(stats == null) {
			stats = new DescriptiveStatistics();
			for(final var entry : resultCount.entrySet()) {
				//add value the number of times that it appears
				for(double d = 0; d <= entry.getValue(); d++) {
					if(NumberUtils.isCreatable(entry.getKey())) {
						stats.addValue(Double.parseDouble(entry.getKey()));
					}
				}
			}
		}
		return stats;
	}
}
