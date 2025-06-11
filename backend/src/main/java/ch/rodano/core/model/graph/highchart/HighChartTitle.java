package ch.rodano.core.model.graph.highchart;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class HighChartTitle {
	private String text;
	private Map<String, String> style;

	public HighChartTitle(final String text) {
		this.text = text;
	}

	public HighChartTitle(final String text, final Map<String, String> style) {
		this(text);
		this.style = style;
	}

	public String getText() {
		return text;
	}

	public void setText(final String text) {
		this.text = text;
	}

	public Map<String, String> getStyle() {
		return style;
	}

	public void setStyle(final Map<String, String> style) {
		this.style = style;
	}
}
