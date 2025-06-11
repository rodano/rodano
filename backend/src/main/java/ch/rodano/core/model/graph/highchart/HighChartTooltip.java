package ch.rodano.core.model.graph.highchart;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class HighChartTooltip {
	@JsonRawValue
	private String formatter;

	public String getFormatter() {
		return formatter;
	}

	public void setFormatter(final String formatter) {
		this.formatter = formatter;
	}
}
