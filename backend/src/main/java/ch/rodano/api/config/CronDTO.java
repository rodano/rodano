package ch.rodano.api.config;

import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CronDTO {

	@NotNull
	private UUID cronId;
	@NotBlank
	private String id;

	@NotNull
	private SortedMap<String, String> description;

	@NotNull
	private int intervalValue;
	@NotNull
	private String intervalUnit;

	public UUID getCronId() {
		return cronId;
	}

	public void setCronId(final UUID cronId) {
		this.cronId = cronId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public SortedMap<String, String> getDescription() {
		return description;
	}

	public void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public int getIntervalValue() {
		return intervalValue;
	}

	public void setIntervalValue(final int intervalValue) {
		this.intervalValue = intervalValue;
	}

	public String getIntervalUnit() {
		return intervalUnit;
	}

	public void setIntervalUnit(final String intervalUnit) {
		this.intervalUnit = intervalUnit;
	}
}
