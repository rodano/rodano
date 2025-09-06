package ch.rodano.batch.pojo;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimelineGraphSectionReferenceEntry {

	private String timepoint;
	private BigDecimal value;
	private String label;

	public String getTimepoint() {
		return timepoint;
	}

	public void setTimepoint(final String timepoint) {
		this.timepoint = timepoint;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(final BigDecimal value) {
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "TimelineGraphSectionReferenceEntry{" +
			"timepoint='" + timepoint + '\'' +
			", value=" + value +
			", label='" + label + '\'' +
			'}';
	}
}
