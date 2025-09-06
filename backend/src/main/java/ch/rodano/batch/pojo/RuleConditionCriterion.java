package ch.rodano.batch.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleConditionCriterion {

	private String property;
	private String operator;
	private List<String> values;

	public String getProperty() {
		return property;
	}

	public void setProperty(final String property) {
		this.property = property;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(final String operator) {
		this.operator = operator;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(final List<String> values) {
		this.values = values;
	}

	@Override
	public String toString() {
		return "RuleConditionCriterion{" +
			"property='" + property + '\'' +
			", operator='" + operator + '\'' +
			", values=" + values +
			'}';
	}
}
