package ch.rodano.batch.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleConditionList {

	private String mode;
	private List<RuleCondition> conditions;

	public String getMode() {
		return mode;
	}

	public void setMode(final String mode) {
		this.mode = mode;
	}

	public List<RuleCondition> getConditions() {
		return conditions;
	}

	public void setConditions(final List<RuleCondition> conditions) {
		this.conditions = conditions;
	}

	@Override
	public String toString() {
		return "RuleConditionList{" +
			"mode='" + mode + '\'' +
			", conditions=" + conditions +
			'}';
	}
}
