package ch.rodano.api.config;

import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.configuration.model.event.EventGroup;

public class EventGroupDTO {
	@NotNull
	private UUID eventGroupId;
	@NotBlank
	private String id;
	@NotNull
	private SortedMap<String, String> shortname;

	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private UUID scopeModelId;

	/**
	 * Default constructor, needed by some serializer
	 */
	public EventGroupDTO() {

	}

	public EventGroupDTO(final EventGroup eventGroup) {
		eventGroupId = eventGroup.getEventGroupId();
		id = eventGroup.getId();
		shortname = eventGroup.getShortname();
	}

	public UUID getEventGroupId() {
		return eventGroupId;
	}

	public void setEventGroupId(final UUID eventGroupId) {
		this.eventGroupId = eventGroupId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public SortedMap<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	public SortedMap<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	public SortedMap<String, String> getDescription() {
		return description;
	}

	public void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public UUID getScopeModelId() {
		return scopeModelId;
	}

	public void setScopeModelId(final UUID scopeModelId) {
		this.scopeModelId = scopeModelId;
	}
}
