package ch.rodano.configuration.model.rules;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

public class RuleConditionCriterion implements Node {
	private static final long serialVersionUID = -7897881749660873674L;

	private String property;
	private Operator operator;
	private Set<String> values;
	private String conditionId;

	public RuleConditionCriterion() {
		values = new HashSet<>();
	}

	public final String getProperty() {
		return property;
	}

	public final void setProperty(final String property) {
		this.property = property;
	}

	public final Operator getOperator() {
		return operator;
	}

	public final void setOperator(final Operator operator) {
		this.operator = operator;
	}

	public final Set<String> getValues() {
		return values;
	}

	public final void setValues(final Set<String> values) {
		this.values = values;
	}

	/**
	 * Returns the first value. It may return any value if there is more than one.
	 *
	 * @return
	 */
	@JsonIgnore
	public final String getFirstValue() {
		return values.stream().findFirst().orElse(null);
	}

	public final String getConditionId() {
		return conditionId;
	}

	public final void setConditionId(final String conditionId) {
		this.conditionId = conditionId;
	}

	@Override
	public final Entity getEntity() {
		return Entity.RULE_CONDITION_CRITERION;
	}

	@Override
	public String toString() {
		return String.format("Property %s - Operator %s - Values : %s", getProperty(), getOperator(), getValues());
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
