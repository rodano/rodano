package ch.rodano.api.config;

import java.util.SortedMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.configuration.model.form.FormModel;

public class FormModelDTO {
	@NotBlank
	private String id;
	@NotNull
	private SortedMap<String, String> shortname;

	/**
	 * Default constructor, needed by some serializer
	 */
	FormModelDTO() {

	}

	public FormModelDTO(final FormModel formModel) {
		this.id = formModel.getId();
		this.shortname = formModel.getShortname();
	}

	public SortedMap<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}
}
