package ch.rodano.core.model.graph.highchart;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRawValue;

@JsonInclude(Include.NON_NULL)
public class HighChart {
	private HighChartChart chart;
	private List<String> colors;
	private HighChartTitle title;
	private HighChartAxis xAxis;
	private HighChartAxis yAxis;
	private HighChartPlotOptions plotOptions;
	private HighChartLegend legend;
	private HighChartTooltip tooltip;

	@JsonRawValue
	private String credits;

	private List<HighChartSerie> series;

	public HighChart() {
		credits = "{\"enabled\": false}";
		series = new ArrayList<>();
	}

	public HighChartChart getChart() {
		return chart;
	}

	public void setChart(final HighChartChart chart) {
		this.chart = chart;
	}

	public List<String> getColors() {
		return colors;
	}

	public void setColors(final List<String> colors) {
		this.colors = colors;
	}

	public HighChartTitle getTitle() {
		return title;
	}

	public void setTitle(final HighChartTitle title) {
		this.title = title;
	}

	public HighChartAxis getxAxis() {
		return xAxis;
	}

	public void setxAxis(final HighChartAxis xAxis) {
		this.xAxis = xAxis;
	}

	public HighChartAxis getyAxis() {
		return yAxis;
	}

	public void setyAxis(final HighChartAxis yAxis) {
		this.yAxis = yAxis;
	}

	public HighChartPlotOptions getPlotOptions() {
		return plotOptions;
	}

	public void setPlotOptions(final HighChartPlotOptions plotOptions) {
		this.plotOptions = plotOptions;
	}

	public HighChartLegend getLegend() {
		return legend;
	}

	public void setLegend(final HighChartLegend legend) {
		this.legend = legend;
	}

	public HighChartTooltip getTooltip() {
		return tooltip;
	}

	public void setTooltip(final HighChartTooltip tooltip) {
		this.tooltip = tooltip;
	}

	public String getCredits() {
		return credits;
	}

	public void setCredits(final String credits) {
		this.credits = credits;
	}

	public List<HighChartSerie> getSeries() {
		return series;
	}

	public void setSeries(final List<HighChartSerie> series) {
		this.series = series;
	}
}
