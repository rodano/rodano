package ch.rodano.configuration.model.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.rules.Operator;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class VisibilityCriteria implements Node {
	private static final long serialVersionUID = 6078561914633069811L;

	private Cell cell;

	private Operator operator;
	private List<String> values;
	private VisibilityCriterionAction action;
	private List<String> targetLayoutIds;
	private List<String> targetCellIds;

	public VisibilityCriteria() {
		values = new ArrayList<>();
		targetLayoutIds = new ArrayList<>();
		targetCellIds = new ArrayList<>();
	}

	@JsonBackReference
	public final Cell getCell() {
		return cell;
	}

	@JsonBackReference
	public final void setCell(final Cell cell) {
		this.cell = cell;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(final Operator operator) {
		this.operator = operator;
	}

	public final List<String> getValues() {
		return values;
	}

	public final void setValues(final List<String> values) {
		this.values = values;
	}

	public final VisibilityCriterionAction getAction() {
		return action;
	}

	public final void setAction(final VisibilityCriterionAction action) {
		this.action = action;
	}

	public final List<String> getTargetLayoutIds() {
		return targetLayoutIds;
	}

	public final void setTargetLayoutIds(final List<String> targetLayoutIds) {
		this.targetLayoutIds = targetLayoutIds;
	}

	public final List<String> getTargetCellIds() {
		return targetCellIds;
	}

	public final void setTargetCellIds(final List<String> targetCellIds) {
		this.targetCellIds = targetCellIds;
	}

	@Override
	public final Entity getEntity() {
		return Entity.VISIBILITY_CRITERIA;
	}

	@JsonIgnore
	public String getDescription(final String... languages) {
		final var fieldModel = cell.getFieldModel();
		final var valueLabels = new ArrayList<>();
		for(final var value : getValues()) {
			try {
				valueLabels.add(fieldModel.getPossibleValue(value).getLocalizedShortname(languages));
			}
			catch (final Exception e) {
				valueLabels.add(value);
			}
		}
		return String.format("If answer to \"%s\" is [%s], then %s:",
			fieldModel.getLocalizedLabel(languages),
			StringUtils.join(valueLabels, " | "),
			getAction().toString().toLowerCase());
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
