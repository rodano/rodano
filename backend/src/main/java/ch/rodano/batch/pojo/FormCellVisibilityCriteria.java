package ch.rodano.batch.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FormCellVisibilityCriteria {

	private String operator;
	private String action;

	private List<String> values;
	private List<String> targetCellIds;
	private List<String> targetLayoutIds;

	public String getOperator() {
		return operator;
	}

	public void setOperator(final String operator) {
		this.operator = operator;
	}

	public String getAction() {
		return action;
	}

	public void setAction(final String action) {
		this.action = action;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(final List<String> values) {
		this.values = values;
	}

	public List<String> getTargetCellIds() {
		return targetCellIds;
	}

	public void setTargetCellIds(final List<String> targetCellIds) {
		this.targetCellIds = targetCellIds;
	}

	public List<String> getTargetLayoutIds() {
		return targetLayoutIds;
	}

	public void setTargetLayoutIds(final List<String> targetLayoutIds) {
		this.targetLayoutIds = targetLayoutIds;
	}

	@Override
	public String toString() {
		return "FormCellVisibilityCriteria{" +
			"operator='" + operator + '\'' +
			", values='" + values + '\'' +
			", action='" + action + '\'' +
			", targetCellIds=" + targetCellIds +
			", targetLayoutIds=" + targetLayoutIds +
			'}';
	}
}
