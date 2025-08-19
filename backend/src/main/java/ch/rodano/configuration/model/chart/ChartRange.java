package ch.rodano.configuration.model.chart;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.utils.DisplayableUtils;

public class ChartRange implements Node {

	private static final long serialVersionUID = 4582052131761798996L;

	private String id;

	private Map<String, String> labels = new HashMap<>();

	private Double min;
	private Double max;
	private String value;

	private boolean other;

	public ChartRange() {
		//use for deserialization
	}

	public ChartRange(final Map<String, String> labels, final double min, final double max) {
		this.labels = labels;
		this.min = min;
		this.max = max;
	}

	public ChartRange(final Map<String, String> labels, final String value) {
		this.labels = labels;
		this.value = value;
	}

	public static ChartRange otherChartRange(final Map<String, String> labels) {
		final var range = new ChartRange();
		range.setLabels(labels);
		range.setOther(true);
		return range;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public Double getMin() {
		return min;
	}

	public void setMin(final Double min) {
		this.min = min;
	}

	public Double getMax() {
		return max;
	}

	public void setMax(final Double max) {
		this.max = max;
	}

	public void setOther(final boolean other) {
		this.other = other;
	}

	public boolean getOther() {
		return other;
	}

	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(final Map<String, String> labels) {
		this.labels = labels;
	}

	@JsonIgnore
	public String getLocalizedLabel(final String... languages) {
		return DisplayableUtils.getLocalizedMapWithDefault(labels, getValue(), languages);
	}

	@Override
	public Entity getEntity() {
		return Entity.CHART_RANGE;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
