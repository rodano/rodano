package ch.rodano.configuration.model.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class RuleCondition implements Node {
	private static final long serialVersionUID = -2106058031155804759L;

	protected String id;

	protected RuleConditionCriterion criterion;
	protected boolean inverse;
	protected boolean dependency;
	protected RuleBreakType breakType;

	protected RuleConditionListEvaluationMode mode;
	protected List<RuleCondition> conditions;

	public RuleCondition() {
		breakType = RuleBreakType.NONE;
		mode = RuleConditionListEvaluationMode.OR;
		conditions = new ArrayList<>();
	}

	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	public final RuleConditionCriterion getCriterion() {
		return criterion;
	}

	public final void setCriterion(final RuleConditionCriterion criterion) {
		this.criterion = criterion;
	}

	public final boolean isInverse() {
		return inverse;
	}

	public final void setInverse(final boolean inverse) {
		this.inverse = inverse;
	}

	public final boolean isDependency() {
		return dependency;
	}

	public final void setDependency(final boolean dependency) {
		this.dependency = dependency;
	}

	public final RuleBreakType getBreakType() {
		return breakType;
	}

	public final void setBreakType(final RuleBreakType breakType) {
		this.breakType = breakType;
	}

	public final RuleConditionListEvaluationMode getMode() {
		return mode;
	}

	public final void setMode(final RuleConditionListEvaluationMode mode) {
		this.mode = mode;
	}

	public final List<RuleCondition> getConditions() {
		return conditions;
	}

	public final void setConditions(final List<RuleCondition> conditions) {
		this.conditions = conditions;
	}

	// a condition is an evaluation if its criterion uses an operator and has values
	@JsonIgnore
	public boolean isEvaluation() {
		return criterion.getOperator() != null && !criterion.getValues().isEmpty();
	}

	@JsonIgnore
	public List<RuleCondition> getEvaluations() {
		final List<RuleCondition> evaluations = new ArrayList<>();
		if(isEvaluation()) {
			evaluations.add(this);
		}

		getConditions().stream().map(RuleCondition::getEvaluations).forEach(evaluations::addAll);
		return evaluations;
	}

	@JsonIgnore
	public List<RuleCondition> getDependencies() {
		final List<RuleCondition> leafs = new ArrayList<>();
		if(isDependency()) {
			leafs.add(this);
		}
		else {
			getConditions().stream().map(RuleCondition::getDependencies).forEach(leafs::addAll);
		}

		return leafs;
	}

	@Override
	public final Entity getEntity() {
		return Entity.RULE_CONDITION;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
