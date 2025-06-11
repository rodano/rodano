package ch.rodano.configuration.model.chart;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.rodano.configuration.utils.DisplayableUtils;

public class Legend {
	protected int x;
	protected int y;
	protected Map<String, String> labels;
	protected String color;

	public Legend() {
		labels = new HashMap<>();
	}

	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(final Map<String, String> labels) {
		this.labels = labels;
	}

	@JsonIgnore
	public String getLocalizedLabel(final String... languages) {
		return DisplayableUtils.getLocalizedMap(labels, languages);
	}

	public String getColor() {
		return color;
	}

	public void setColor(final String color) {
		this.color = color;
	}

	public int getX() {
		return x;
	}

	public void setX(final int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(final int y) {
		this.y = y;
	}
}
