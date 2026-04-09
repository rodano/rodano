package ch.rodano.batch.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleActionParameter {

	private String id;
	private String value;
	private String conditionId;
	private String rulableEntity;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public String getConditionId() {
		return conditionId;
	}

	public void setConditionId(final String conditionId) {
		this.conditionId = conditionId;
	}

	public String getRulableEntity() {
		return rulableEntity;
	}

	public void setRulableEntity(final String rulableEntity) {
		this.rulableEntity = rulableEntity;
	}

	@Override
	public String toString() {
		return "RuleActionParameter{" +
			"id='" + id + '\'' +
			", value='" + value + '\'' +
			", conditionId='" + conditionId + '\'' +
			", rulableEntity='" + rulableEntity + '\'' +
			'}';
	}
}
