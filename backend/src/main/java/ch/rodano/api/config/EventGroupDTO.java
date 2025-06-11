package ch.rodano.api.config;

import java.util.SortedMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.configuration.model.event.EventGroup;

public class EventGroupDTO {
	@NotBlank
	private String id;
	@NotNull
	private SortedMap<String, String> shortname;

	/**
	 * Default constructor, needed by some serializer
	 */
	EventGroupDTO() {

	}

	public EventGroupDTO(final EventGroup eventGroup) {
		id = eventGroup.getId();
		shortname = eventGroup.getShortname();
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
}
