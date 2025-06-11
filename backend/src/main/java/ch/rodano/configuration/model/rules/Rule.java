package ch.rodano.configuration.model.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class Rule implements Node {
	private static final long serialVersionUID = 6038492625572866859L;

	private String description;
	private SortedMap<String, String> message;
	private SortedSet<String> tags;
	private RuleConstraint constraint;
	private List<RuleAction> actions;

	public Rule() {
		message = new TreeMap<>();
		tags = new TreeSet<>();
		actions = new ArrayList<>();
	}

	public final String getDescription() {
		return description;
	}

	public final void setDescription(final String description) {
		this.description = description;
	}

	public final SortedMap<String, String> getMessage() {
		return message;
	}

	public final void setMessage(final SortedMap<String, String> message) {
		this.message = message;
	}

	public final SortedSet<String> getTags() {
		return tags;
	}

	public final void setTags(final SortedSet<String> tags) {
		this.tags = tags;
	}

	public final RuleConstraint getConstraint() {
		return constraint;
	}

	public final void setConstraint(final RuleConstraint constraint) {
		this.constraint = constraint;
	}

	public final List<RuleAction> getActions() {
		return actions;
	}

	public final void setActions(final List<RuleAction> actions) {
		this.actions = actions;
	}

	@Override
	public final Entity getEntity() {
		return Entity.RULE;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
