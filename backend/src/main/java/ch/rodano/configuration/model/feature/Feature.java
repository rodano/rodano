package ch.rodano.configuration.model.feature;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
public class Feature implements SuperDisplayable, Serializable, Assignable<Feature>, Node {
	private static final long serialVersionUID = 4081701801864249665L;

	private Study study;
	private String id;
	private boolean isStatic;
	private boolean optional;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	public Feature() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
	}

	@JsonBackReference
	public final void setStudy(final Study study) {
		this.study = study;
	}

	@JsonBackReference
	public final Study getStudy() {
		return study;
	}

	@Override
	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@Override
	public final SortedMap<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	@Override
	public SortedMap<String, String> getLongname() {
		return longname;
	}

	public final void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	@Override
	public final SortedMap<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
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
		return Entity.FEATURE;
	}

	@Override
	@JsonIgnore
	public final boolean isStatic() {
		return isStatic;
	}

	@JsonIgnore
	public final void setStatic(final boolean staticFeature) {
		isStatic = staticFeature;
	}

	public final boolean isOptional() {
		return optional;
	}

	public final void setOptional(final boolean optional) {
		this.optional = optional;
	}

	@JsonIgnore
	public static List<String> getIdsFromFeatures(final Collection<Feature> features) {
		return features.stream().filter(Objects::nonNull).map(Feature::getId).toList();
	}

	@Override
	@JsonIgnore
	public final int compareTo(final Feature feature) {
		return id.compareTo(feature.id);
	}

	//node
	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
