package ch.rodano.configuration.model.resource;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.common.SuperDisplayable;
import ch.rodano.configuration.model.rights.Assignable;
import ch.rodano.configuration.model.study.Study;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class ResourceCategory implements Serializable, SuperDisplayable, Assignable<ResourceCategory>, Node {
	private static final long serialVersionUID = -813476290184481379L;

	private Study study;
	private String id;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private String color;
	private String icon;

	public ResourceCategory() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
	}

	@JsonBackReference
	public final Study getStudy() {
		return study;
	}

	@JsonBackReference
	public final void setStudy(final Study study) {
		this.study = study;
	}

	@Override
	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	public final String getIcon() {
		return icon;
	}

	public final void setIcon(final String icon) {
		this.icon = icon;
	}

	@Override
	public final SortedMap<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	@Override
	public final SortedMap<String, String> getLongname() {
		return longname;
	}

	public final void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	@Override
	public final SortedMap<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public final String getColor() {
		return color;
	}

	public final void setColor(final String color) {
		this.color = color;
	}

	@JsonIgnore
	public final String getDefaultLocalizedLongname() {
		return getLocalizedLongname(getStudy().getDefaultLanguage().getId());
	}

	@JsonIgnore
	public final String getDefaultLocalizedShortname() {
		return getLocalizedShortname(getStudy().getDefaultLanguage().getId());
	}

	@Override
	@JsonIgnore
	public final String getAssignableDescription() {
		return getDefaultLocalizedShortname();
	}

	@Override
	public final Entity getEntity() {
		return Entity.RESOURCE_CATEGORY;
	}

	@Override
	@JsonIgnore
	public final int compareTo(final ResourceCategory resourceCategory) {
		return getId().compareTo(resourceCategory.getId());
	}

	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
