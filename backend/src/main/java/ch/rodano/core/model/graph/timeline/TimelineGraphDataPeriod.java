package ch.rodano.core.model.graph.timeline;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class TimelineGraphDataPeriod implements Serializable {
	private static final long serialVersionUID = -2982982586763193668L;

	private ZonedDateTime startDate;
	private ZonedDateTime stopDate;
	private String label;
	private boolean isDefault;
	private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_INSTANT;

	@JsonProperty("start_date_string")
	public final String getStartDateString() {
		return startDate == null ? null : startDate.format(DATE_FORMATTER);
	}

	@JsonProperty("stop_date_string")
	public final String getStopDateString() {
		return stopDate == null ? null : stopDate.format(DATE_FORMATTER);
	}

	@JsonIgnore
	public final ZonedDateTime getStartDate() {
		return startDate;
	}

	public final void setStartDate(final ZonedDateTime startDate) {
		this.startDate = startDate;
	}

	@JsonIgnore
	public final ZonedDateTime getStopDate() {
		return stopDate;
	}

	public final void setStopDate(final ZonedDateTime stopDate) {
		this.stopDate = stopDate;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(final boolean isDefault) {
		this.isDefault = isDefault;
	}

	public final String getLabel() {
		return label;
	}

	public final void setLabel(final String label) {
		this.label = label;
	}
}
