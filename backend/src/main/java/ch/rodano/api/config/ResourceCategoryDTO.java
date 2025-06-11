package ch.rodano.api.config;

import java.util.SortedMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ch.rodano.configuration.model.resource.ResourceCategory;

public class ResourceCategoryDTO {
	@NotBlank
	private String id;
	@NotNull
	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;
	private String color;
	private String icon;

	public ResourceCategoryDTO() { }

	public ResourceCategoryDTO(final ResourceCategory resourceCategory) {
		id = resourceCategory.getId();
		shortname = resourceCategory.getShortname();
		longname = resourceCategory.getLongname();
		description = resourceCategory.getDescription();
		color = resourceCategory.getColor();
		icon = resourceCategory.getIcon();
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

	public String getColor() {
		return color;
	}

	public void setColor(final String color) {
		this.color = color;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(final String icon) {
		this.icon = icon;
	}
}
