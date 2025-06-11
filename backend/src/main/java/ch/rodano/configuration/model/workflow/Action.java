package ch.rodano.configuration.model.workflow;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
import ch.rodano.configuration.model.rights.ProfileRightAssignable;
import ch.rodano.configuration.model.rules.Rule;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class Action implements Serializable, SuperDisplayable, Node, ProfileRightAssignable<Action>, Comparable<Action> {
	private static final long serialVersionUID = -4500446941849236252L;

	public static final Comparator<Action> DEFAULT_COMPARATOR = Comparator.comparing(Action::getId);

	private String id;
	private Workflow workflow;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private String icon;
	private List<Rule> rules;

	private boolean requireSignature;
	private SortedMap<String, String> requiredSignatureText;

	private boolean documentable;
	private List<Map<String, String>> documentableOptions;

	public Action() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
	}

	@Override
	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@JsonBackReference
	public final Workflow getWorkflow() {
		return workflow;
	}

	@JsonBackReference
	public final void setWorkflow(final Workflow worklow) {
		workflow = worklow;
	}

	@Override
	public final SortedMap<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
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
	public String getDefaultLocalizedShortname() {
		return getLocalizedShortname(getWorkflow().getStudy().getDefaultLanguage().getId());
	}

	public final List<Rule> getRules() {
		return rules;
	}

	public final void setRules(final List<Rule> rules) {
		this.rules = rules;
	}

	public final String getIcon() {
		return icon;
	}

	public final void setIcon(final String icon) {
		this.icon = icon;
	}

	public final boolean isDocumentable() {
		return documentable;
	}

	public final void setDocumentable(final boolean documentable) {
		this.documentable = documentable;
	}

	public final List<Map<String, String>> getDocumentableOptions() {
		return documentableOptions;
	}

	public final void setDocumentableOptions(final List<Map<String, String>> documentableOptions) {
		this.documentableOptions = documentableOptions;
	}

	public final boolean isRequireSignature() {
		return requireSignature;
	}

	public final void setRequireSignature(final boolean requireSignature) {
		this.requireSignature = requireSignature;
	}

	public final SortedMap<String, String> getRequiredSignatureText() {
		return requiredSignatureText;
	}

	public final void setRequiredSignatureText(final SortedMap<String, String> requiredSignatureText) {
		this.requiredSignatureText = requiredSignatureText;
	}

	@Override
	@JsonIgnore
	public String getAssignableDescription() {
		return getDefaultLocalizedShortname();
	}

	@Override
	public final Entity getEntity() {
		return Entity.ACTION;
	}

	@Override
	@JsonIgnore
	public int compareTo(final Action otherAction) {
		return DEFAULT_COMPARATOR.compare(this, otherAction);
	}

	@Override
	@JsonIgnore
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}

	@Override
	@JsonIgnore
	public String getParentId() {
		return getWorkflow().getId();
	}
}
