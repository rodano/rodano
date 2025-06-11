package ch.rodano.configuration.model.policy;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.utils.DisplayableUtils;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class PrivacyPolicy implements Serializable, SuperDisplayable, Node, Comparable<PrivacyPolicy> {
	private static final long serialVersionUID = -360517234571933188L;

	private Study study;

	private String id;
	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private Map<String, String> content;

	private Set<String> profileIds;

	public PrivacyPolicy() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		content = new HashMap<>();
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

	public final Map<String, String> getContent() {
		return content;
	}

	public final void setContent(final Map<String, String> content) {
		this.content = content;
	}

	public Set<String> getProfileIds() {
		return profileIds;
	}

	public void setProfileIds(final Set<String> profileIds) {
		this.profileIds = profileIds;
	}

	@JsonIgnore
	public final List<Profile> getProfiles() {
		return profileIds.stream().map(study::getProfile).toList();
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

	public final String getLocalizedContent(final String... languages) {
		return DisplayableUtils.getLocalizedMap(content, languages);
	}

	@Override
	public final Entity getEntity() {
		return Entity.PRIVACY_POLICY;
	}

	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}

	@Override
	public final int compareTo(final PrivacyPolicy o) {
		return id.compareTo(o.id);
	}
}
