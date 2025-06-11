package ch.rodano.core.model.graph.highchart.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.rodano.configuration.model.chart.Chart;

public class EnrollmentChart extends HighGraph {

	public EnrollmentChart(final Chart configuration, final String language, final Map<String, String> values) {
		super(configuration, language, values);
	}

	@Override
	public String getDefaultTitle() {
		return "Enrollment Status";
	}

	@Override
	public String getType() {
		return "spline";
	}

	@Override
	public List<String> getColors() {
		if(configuration.getColors() != null) {
			final List<String> colors = new ArrayList<>();
			for(var i = 0; i < configuration.getColors().size(); i++) {
				colors.add(configuration.getColors().get(i));
				if(configuration.isDisplayExpected()) {
					colors.add("#e3e3e3");
				}
			}
			return colors;
		}

		return DEFAULT_COLORS;
	}
}
