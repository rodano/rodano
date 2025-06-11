package ch.rodano.core.model.graph.highchart.instance;

import java.util.List;
import java.util.Map;

import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.configuration.model.chart.ChartRange;

public class EnrollmentByScopeChart extends HighGraph {
	private final List<ChartRange> ranges;

	public EnrollmentByScopeChart(final Chart configuration, final String language, final List<ChartRange> ranges, final Map<String, String> values) {
		super(configuration, language, values);
		this.ranges = ranges;
	}

	@Override
	public String getDefaultTitle() {
		return "Enrollment Status";
	}

	@Override
	public String getType() {
		return "column";
	}

	@Override
	public List<ChartRange> getRanges() {
		return ranges;
	}

	@Override
	public boolean isInverted() {
		return true;
	}
}
