package ch.rodano.batch.pojo;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChartRange {

	private String id;
	private Map<String, String> labels;

	private String value;
	private BigDecimal min;
	private BigDecimal max;
	private Boolean other;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(final Map<String, String> labels) {
		this.labels = labels;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
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

	public Boolean getOther() {
		return other;
	}

	public void setOther(final Boolean other) {
		this.other = other;
	}

	@Override
	public String toString() {
		return "ChartRange{" +
			"id='" + id + '\'' +
			", labels=" + labels +
			", value='" + value + '\'' +
			", min=" + min +
			", max=" + max +
			", other=" + other +
			'}';
	}
}
