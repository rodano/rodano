package ch.rodano.core.model.chart;

import java.util.Collection;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class Statistics {
	private final DescriptiveStatistics stats;

	public Statistics(final Collection<Double> values) {
		stats = new DescriptiveStatistics();
		for(final var value : values) {
			stats.addValue(value);
		}
	}

	public int getCount() {
		return stats.getValues().length;
	}

	public Double getMin() {
		return stats.getMin();
	}

	public Double getMax() {
		return stats.getMax();
	}

	public Double getMean() {
		return stats.getMean();
	}

	public Double getSd() {
		return stats.getStandardDeviation();
	}

}
