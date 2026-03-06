package ch.rodano.batch.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ch.rodano.core.model.jooq.enums.FormCellVisibilityCriteriaAction;
import ch.rodano.core.model.jooq.enums.FormCellVisibilityCriteriaOperator;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FormCellVisibilityCriteria {

	private FormCellVisibilityCriteriaOperator operator;
	private FormCellVisibilityCriteriaAction action;

	private List<String> values;
	private List<String> targetCellIds;
	private List<String> targetLayoutIds;

	public FormCellVisibilityCriteriaOperator getOperator() {
		return operator;
	}

	public void setOperator(final FormCellVisibilityCriteriaOperator operator) {
		this.operator = operator;
	}

	public FormCellVisibilityCriteriaAction getAction() {
		return action;
	}

	public void setAction(final FormCellVisibilityCriteriaAction action) {
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
