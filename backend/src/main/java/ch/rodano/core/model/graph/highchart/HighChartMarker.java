package ch.rodano.core.model.graph.highchart;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class HighChartMarker {
	private boolean enabled = false;
	private String symbol = "circle";
	private short lineWidth = 3;
	private Map<String, Map<String, Object>> states;

	public HighChartMarker() {
		states = Map.ofEntries(
			Map.entry(
				"hover", Map.ofEntries(
					Map.entry("symbol", "circle"),
					Map.entry("radius", 6),
					Map.entry("lineWidth", 1)
				)
			)
		);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(final String symbol) {
		this.symbol = symbol;
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
}
