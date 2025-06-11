package ch.rodano.core.model.graph.highchart;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRawValue;

@JsonInclude(Include.NON_NULL)
public class HighChartLabel {
	private Integer rotation;
	private String align = "right";
	private Map<String, String> style;

	@JsonRawValue
	private String formatter;

	public Integer getRotation() {
		return rotation;
	}

	public void setRotation(final Integer rotation) {
		this.rotation = rotation;
	}

	public String getAlign() {
		return align;
	}

	public void setAlign(final String align) {
		this.align = align;
	}

	public Map<String, String> getStyle() {
		return style;
	}

	public void setStyle(final Map<String, String> style) {
		this.style = style;
	}

	public String getFormatter() {
		return formatter;
	}

	public void setFormatter(final String formatter) {
		this.formatter = formatter;
	}
}
