package ch.rodano.core.model.graph.highchart;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class HighChartAxis {
	private String type;
	private List<String> categories;

	private Map<String, String> dateTimeLabelFormats;
	private Integer min;
	private Integer max;

	private Float gridLineWidth;
	private String lineColor;
	private String alternateGridColor;

	private HighChartLabel labels;
	private HighChartTitle title;

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(final List<String> categories) {
		this.categories = categories;
	}

	public Map<String, String> getDateTimeLabelFormats() {
		return dateTimeLabelFormats;
	}

	public void setDateTimeLabelFormats(final Map<String, String> dateTimeLabelFormats) {
		this.dateTimeLabelFormats = dateTimeLabelFormats;
	}

	public Integer getMin() {
		return min;
	}

	public void setMin(final Integer min) {
		this.min = min;
	}

	public Integer getMax() {
		return max;
	}

	public void setMax(final Integer max) {
		this.max = max;
	}

	public Float getGridLineWidth() {
		return gridLineWidth;
	}

	public void setGridLineWidth(final Float gridLineWidth) {
		this.gridLineWidth = gridLineWidth;
	}

	public String getLineColor() {
		return lineColor;
	}

	public void setLineColor(final String lineColor) {
		this.lineColor = lineColor;
	}

	public String getAlternateGridColor() {
		return alternateGridColor;
	}

	public void setAlternateGridColor(final String alternateGridColor) {
		this.alternateGridColor = alternateGridColor;
	}

	public HighChartLabel getLabels() {
		return labels;
	}

	public void setLabels(final HighChartLabel labels) {
		this.labels = labels;
	}

	public HighChartTitle getTitle() {
		return title;
	}

	public void setTitle(final HighChartTitle title) {
		this.title = title;
	}
}
