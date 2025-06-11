package ch.rodano.core.model.graph.timeline;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class TimelineGraphDataValue implements Serializable {
	private static final long serialVersionUID = 521218783896549641L;

	private ZonedDateTime date;
	private ZonedDateTime endDate;
	private final DateTimeFormatter dateFormatter;
	private boolean ongoing;
	private String label;
	private Object value;
	private String link;
	private String icon;
	private Map<String, Object> metadata;

	/**
	 * Constructor
	 */
	public TimelineGraphDataValue() {
		dateFormatter = DateTimeFormatter.ISO_INSTANT;
		metadata = new HashMap<>();
	}

	@JsonProperty("date_string")
	public final String getDateString() {
		return date == null ? null : date.format(dateFormatter);
	}

	@JsonProperty("end_date_string")
	public final String getEndDateString() {
		return endDate == null ? null : endDate.format(dateFormatter);
	}

	@JsonIgnore
	public final ZonedDateTime getDate() {
		return date;
	}

	public final void setDate(final ZonedDateTime date) {
		this.date = date;
	}

	@JsonIgnore
	public final ZonedDateTime getEndDate() {
		return endDate;
	}

	public final void setEndDate(final ZonedDateTime endDate) {
		this.endDate = endDate;
	}

	public final boolean isOngoing() {
		return ongoing;
	}

	public final void setOngoing(final boolean ongoing) {
		this.ongoing = ongoing;
	}

	public final String getLabel() {
		return label;
	}

	public final void setLabel(final String label) {
		this.label = label;
	}

	public final Object getValue() {
		return value;
	}

	public final void setValue(final Object value) {
		this.value = value;
	}

	public final String getLink() {
		return link;
	}

	public final void setLink(final String link) {
		this.link = link;
	}

	public final String getIcon() {
		return icon;
	}

	public final void setIcon(final String icon) {
		this.icon = icon;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(final Map<String, Object> metadata) {
		this.metadata = metadata;
	}
}
