package ch.rodano.api.config;

import java.math.BigDecimal;
import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class ChartRangeDTO {

	@NotNull
	private UUID chartRangeId;
	@NotNull
	private UUID chartId;
	@NotNull
	private String id;

	private String value;

	private SortedMap<String, String> label;

	private BigDecimal min;
	private BigDecimal max;

	private boolean other;

	private Integer sortOrder;

	public UUID getChartRangeId() {
		return chartRangeId;
	}

	public void setChartRangeId(final UUID chartRangeId) {
		this.chartRangeId = chartRangeId;
	}

	public UUID getChartId() {
		return chartId;
	}

	public void setChartId(final UUID chartId) {
		this.chartId = chartId;
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

	public SortedMap<String, String> getLabel() {
		return label;
	}

	public void setLabel(final SortedMap<String, String> label) {
		this.label = label;
	}

	public BigDecimal getMin() {
		return min;
	}

	public void setMin(final BigDecimal min) {
		this.min = min;
	}

	public BigDecimal getMax() {
		return max;
	}

	public void setMax(final BigDecimal max) {
		this.max = max;
	}

	public boolean isOther() {
		return other;
	}

	public void setOther(final boolean other) {
		this.other = other;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(final Integer sortOrder) {
		this.sortOrder = sortOrder;
	}
}
