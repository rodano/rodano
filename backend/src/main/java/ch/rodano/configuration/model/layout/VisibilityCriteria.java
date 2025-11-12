package ch.rodano.configuration.model.layout;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.rules.Operator;

import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class VisibilityCriteria implements Node {
	@Serial
	private static final long serialVersionUID = 6078561914633069811L;

	private UUID visibilityCriteriaId;
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

	public UUID getVisibilityCriteriaId() {
		if(this.visibilityCriteriaId == null && this.cell != null
			&& this.cell.getId() != null && !this.cell.getId().isBlank()
			&& this.cell.getLine() != null
			&& this.cell.getLine().getLayout() != null
			&& this.cell.getLine().getLayout().getFormModel() != null
			&& this.cell.getLine().getLayout().getFormModel().getStudy() != null) {
			final int index = this.cell.getVisibilityCriteria().indexOf(this);
			if(index >= 0) {
				visibilityCriteriaId = deterministic(
					this.cell.getLine().getLayout().getFormModel().getStudy().getProjectId(),
					"FORM_CELL_VISIBILITY_CRITERIA",
					this.cell.getLine().getLayout().getFormModel().getId() + "|" + this.cell.getLine().getLayout().getId() + "|" + this.cell.getId() + "|" + index);
			}
		}
		return visibilityCriteriaId;
	}

	public void setVisibilityCriteriaId(final UUID visibilityCriteriaId) {
		this.visibilityCriteriaId = visibilityCriteriaId;
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
			catch(final Exception e) {
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

	@JsonIgnore
	public List<Layout> getTargetLayouts() {
		final var form = this.cell.getLine().getLayout().getFormModel();
		final var ids = this.targetLayoutIds == null ? List.<String> of() : this.targetLayoutIds;
		return ids.stream().map(form::getLayout).toList();
	}

	@JsonIgnore
	public List<Cell> getTargetCells() {
		if(this.targetCellIds == null || this.targetCellIds.isEmpty()) {
			return List.of();
		}
		final var form = this.cell.getLine().getLayout().getFormModel();
		final List<Cell> out = new ArrayList<>();
		for(var layout : form.getLayouts()) {
			for(var line : layout.getLines()) {
				for(var c : line.getCells()) {
					if(this.targetCellIds.contains(c.getId())) {
						out.add(c);
					}
				}
			}
		}
		return out;
	}

	@JsonIgnore
	public List<UUID> getTargetLayoutUuids() {
		if(this.targetLayoutIds == null || this.targetLayoutIds.isEmpty()) {
			return List.of();
		}

		final var form = this.cell.getLine().getLayout().getFormModel();
		final var formCode = form.getId();
		final var projectId = this.cell.getStudy().getProjectId();

		return targetLayoutIds.stream()
			.map(layoutCode -> deterministic(projectId, "FORM_LAYOUT", formCode + "|" + layoutCode))
			.toList();
	}

	@JsonIgnore
	public List<UUID> getTargetCellUuids() {
		if(this.targetCellIds == null || this.targetCellIds.isEmpty()) {
			return List.of();
		}

		final var layout = this.cell.getLine().getLayout();
		final var form = layout.getFormModel();
		final var formCode = form.getId();
		final var layoutCode = layout.getId();
		final var projectId = this.cell.getStudy().getProjectId();

		return targetCellIds.stream()
			.map(cellCode -> deterministic(projectId, "FORM_LAYOUT_CELL", formCode + "|" + layoutCode + "|" + cellCode))
			.toList();
	}
}
