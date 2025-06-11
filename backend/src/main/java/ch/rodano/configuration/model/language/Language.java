package ch.rodano.configuration.model.language;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
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
import ch.rodano.configuration.model.study.Study;


@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class Language implements Serializable, Node, SuperDisplayable, Comparable<Language> {
	private static final long serialVersionUID = 2474749013663395804L;

	public static final Comparator<Language> DEFAULT_COMPARATOR = Comparator.comparing(Language::getId);

	public static final Map<String, String> DEFAULT = new HashMap<>();
	static {
		DEFAULT.put(LanguageStatic.en.getId(), "(default)");
		DEFAULT.put(LanguageStatic.fr.getId(), "(défaut)");
	}

	private Study study;
	private String id;
	private boolean isStatic;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	public Language() {
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
	@JsonIgnore
	public final boolean isStatic() {
		return isStatic;
	}

	@JsonIgnore
	public final void setStatic(final boolean staticLanguage) {
		isStatic = staticLanguage;
	}

	@Override
	public final SortedMap<String, String> getDescription() {
		return description;
	}

	public void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	@Override
	public final SortedMap<String, String> getLongname() {
		return longname;
	}

	public final void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	@Override
	public final SortedMap<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	@JsonIgnore
	public final String getLocalizedLabel(final String... languages) {
		if(isDefault()) {
			final var label = new StringBuilder(getLocalizedShortname(languages));
			label.append(" ");
			label.append(DEFAULT.get(languages[0]));

			return label.toString();
		}

		return getLocalizedShortname(languages);
	}

	@JsonIgnore
	public final boolean isDefault() {
		return getId().equals(getStudy().getDefaultLanguageId());
	}

	public final void setName(final String name) {
		shortname.put(LanguageStatic.en.getId(), name);
	}

	@Override
	public final Entity getEntity() {
		return Entity.LANGUAGE;
	}

	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}

	@Override
	public int compareTo(final Language otherLanguage) {
		return DEFAULT_COMPARATOR.compare(this, otherLanguage);
	}
}
