package ch.rodano.configuration.model.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class RuleConditionList implements Node {
	private static final long serialVersionUID = 5252246757718306471L;

	private List<RuleCondition> conditions;
	private RuleConditionListEvaluationMode mode;

	public RuleConditionList() {
		conditions = new ArrayList<>();
		mode = RuleConditionListEvaluationMode.OR;
	}

	public final List<RuleCondition> getConditions() {
		return conditions;
	}

	public final void setConditions(final List<RuleCondition> conditions) {
		this.conditions = conditions;
	}

	public final RuleConditionListEvaluationMode getMode() {
		return mode;
	}

	public final void setMode(final RuleConditionListEvaluationMode mode) {
		this.mode = mode;
	}

	@Override
	public final Entity getEntity() {
		return Entity.RULE_CONDITION_LIST;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
