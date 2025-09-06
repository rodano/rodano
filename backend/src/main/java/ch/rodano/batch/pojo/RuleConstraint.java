package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleConstraint {

	private Map<String, RuleConditionList> conditions;
	private List<Object> evaluations;

	public Map<String, RuleConditionList> getConditions() {
		return conditions;
	}

	public void setConditions(final Map<String, RuleConditionList> conditions) {
		this.conditions = conditions;
	}

	public List<Object> getEvaluations() {
		return evaluations;
	}

	public void setEvaluations(final List<Object> evaluations) {
		this.evaluations = evaluations;
	}

	@Override
	public String toString() {
		return "RuleConstraint{" +
			"conditions=" + conditions +
			", evaluations=" + evaluations +
			'}';
	}
}
