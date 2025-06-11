package ch.rodano.core.model.graph.highchart;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class HighChartPlotOptions {
	private HighChartColumn column;
	private HighChartSpline spline;

	public HighChartColumn getColumn() {
		return column;
	}

	public void setColumn(final HighChartColumn column) {
		this.column = column;
	}

	public HighChartSpline getSpline() {
		return spline;
	}

	public void setSpline(final HighChartSpline spline) {
		this.spline = spline;
	}
}
