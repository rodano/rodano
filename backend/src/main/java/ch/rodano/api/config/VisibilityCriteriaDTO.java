package ch.rodano.api.config;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import ch.rodano.configuration.model.layout.VisibilityCriteria;
import ch.rodano.configuration.model.layout.VisibilityCriterionAction;
import ch.rodano.configuration.model.rules.Operator;

public class VisibilityCriteriaDTO {
	@NotNull
	private Operator operator;
	@NotNull
	private List<String> values;
	@NotNull
	private VisibilityCriterionAction action;
	@NotNull
	private List<String> targetLayoutIds;
	@NotNull
	private List<String> targetCellIds;

	public VisibilityCriteriaDTO(final VisibilityCriteria visibilityCriteria) {
		this.operator = visibilityCriteria.getOperator();
		this.values = visibilityCriteria.getValues();
		this.action = visibilityCriteria.getAction();
		this.targetLayoutIds = visibilityCriteria.getTargetLayoutIds();
		this.targetCellIds = visibilityCriteria.getTargetCellIds();
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(final Operator operator) {
		this.operator = operator;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(final List<String> values) {
		this.values = values;
	}

	public VisibilityCriterionAction getAction() {
		return action;
	}

	public void setAction(final VisibilityCriterionAction action) {
		this.action = action;
	}

	public List<String> getTargetLayoutIds() {
		return targetLayoutIds;
	}

	public void setTargetLayoutIds(final List<String> targetLayoutIds) {
		this.targetLayoutIds = targetLayoutIds;
	}

	public List<String> getTargetCellIds() {
		return targetCellIds;
	}

	public void setTargetCellIds(final List<String> targetCellIds) {
		this.targetCellIds = targetCellIds;
	}
}
