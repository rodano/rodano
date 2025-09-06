package ch.rodano.batch.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleCondition {

	private String id;
	private RuleConditionCriterion criterion;
	private boolean inverse;
	private boolean dependency;
	private String breakType;
	private String mode;
	private List<RuleCondition> conditions;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public RuleConditionCriterion getCriterion() {
		return criterion;
	}

	public void setCriterion(final RuleConditionCriterion criterion) {
		this.criterion = criterion;
	}

	public boolean isInverse() {
		return inverse;
	}

	public void setInverse(final boolean inverse) {
		this.inverse = inverse;
	}

	public boolean isDependency() {
		return dependency;
	}

	public void setDependency(final boolean dependency) {
		this.dependency = dependency;
	}

	public String getBreakType() {
		return breakType;
	}

	public void setBreakType(final String breakType) {
		this.breakType = breakType;
	}

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
		return "RuleCondition{" +
			"id='" + id + '\'' +
			", criterion=" + criterion +
			", inverse=" + inverse +
			", dependency=" + dependency +
			", breakType='" + breakType + '\'' +
			", mode='" + mode + '\'' +
			", conditions=" + conditions +
			'}';
	}
}
