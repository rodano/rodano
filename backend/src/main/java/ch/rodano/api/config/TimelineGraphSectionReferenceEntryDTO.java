package ch.rodano.api.config;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public class TimelineGraphSectionReferenceEntryDTO {

	private String timepoint;
	private BigDecimal value;

	@NotNull
	private String label;
	private Integer sortOrder;

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

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(final Integer sortOrder) {
		this.sortOrder = sortOrder;
	}
}
