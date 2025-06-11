package ch.rodano.api.config;

import java.util.Map;
import java.util.SortedMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.configuration.model.field.PossibleValue;

public class PossibleValueDTO {
	@NotBlank
	private String id;
	@NotNull
	private Map<String, String> shortname;
	@NotNull
	private boolean specify;

	/**
	 * Default constructor, needed by some serializer
	 */
	PossibleValueDTO() {

	}

	public PossibleValueDTO(final PossibleValue possibleValue) {
		this.id = possibleValue.getId();
		this.shortname = possibleValue.getShortname();
		this.specify = possibleValue.isSpecify();
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	public boolean isSpecify() {
		return specify;
	}

	public void setSpecify(final boolean specify) {
		this.specify = specify;
	}
}
