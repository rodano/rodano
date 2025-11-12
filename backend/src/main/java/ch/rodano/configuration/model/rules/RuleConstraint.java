package ch.rodano.configuration.model.rules;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

public class RuleConstraint implements Node {
	@Serial
	private static final long serialVersionUID = 8354365482907115176L;

	private UUID ruleConstraintId;
	private Map<RulableEntity, RuleConditionList> conditions;
	private List<RuleEvaluation> evaluations;

	public RuleConstraint() {
		conditions = new TreeMap<>();
		evaluations = new ArrayList<>();
	}

	public UUID getRuleConstraintId() {
		return ruleConstraintId;
	}

	public void setRuleConstraintId(final UUID ruleConstraintId) {
		this.ruleConstraintId = ruleConstraintId;
	}

	public final Map<RulableEntity, RuleConditionList> getConditions() {
		return conditions;
	}

	public final void setConditions(final Map<RulableEntity, RuleConditionList> conditions) {
		this.conditions = conditions;
	}

	public final List<RuleEvaluation> getEvaluations() {
		return evaluations;
	}

	public final void setEvaluations(final List<RuleEvaluation> evaluations) {
		this.evaluations = evaluations;
	}

	@Override
	public final Entity getEntity() {
		return Entity.RULE_CONSTRAINT;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}

}
