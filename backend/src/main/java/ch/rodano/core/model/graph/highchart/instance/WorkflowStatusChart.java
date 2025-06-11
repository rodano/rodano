package ch.rodano.core.model.graph.highchart.instance;

import java.util.Map;

import ch.rodano.configuration.model.chart.Chart;

public class WorkflowStatusChart extends HighGraph {

	public WorkflowStatusChart(final Chart configuration, final String language, final Map<String, String> values) {
		super(configuration, language, values);
	}

	@Override
	public String getDefaultTitle() {
		return "Workflow status chart";
	}

	@Override
	public String getType() {
		return "spline";
	}
}
