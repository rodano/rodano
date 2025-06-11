package ch.rodano.core.model.graph.highchart;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class HighChartSpline {
	private boolean animation = false;
	private short lineWidth = 3;
	private Map<String, Map<String, Object>> states;
	private HighChartMarker marker;

	public HighChartSpline() {
		states = Map.ofEntries(
			Map.entry(
				"hover", Map.ofEntries(
					Map.entry("lineWidth", 3)
				)
			)
		);
	}

	public boolean isAnimation() {
		return animation;
	}

	public void setAnimation(final boolean animation) {
		this.animation = animation;
	}

	public short getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(final short lineWidth) {
		this.lineWidth = lineWidth;
	}

	public Map<String, Map<String, Object>> getStates() {
		return states;
	}

	public void setStates(final Map<String, Map<String, Object>> states) {
		this.states = states;
	}

	public HighChartMarker getMarker() {
		return marker;
	}

	public void setMarker(final HighChartMarker marker) {
		this.marker = marker;
	}
}
