package ch.rodano.configuration.model.layout;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class Line implements Node {
	@Serial
	private static final long serialVersionUID = -5443844894479046768L;

	private UUID layoutLineId;
	private List<Cell> cells;
	private Layout layout;

	public Line() {
		cells = new ArrayList<>();
	}

	public UUID getLayoutLineId() {
		if(this.layoutLineId == null && this.layout != null
			&& this.layout.getFormModel() != null
			&& this.layout.getFormModel().getStudy() != null) {
			final int index = this.layout.getLines().indexOf(this);
			if(index >= 0) {
				layoutLineId = deterministic(
					this.layout.getFormModel().getStudy().getProjectId(),
					"FORM_LAYOUT_LINE",
					this.layout.getFormModel().getId() + "|" + this.layout.getId() + "|" + index
				);
			}
		}
		return layoutLineId;
	}

	public void setLayoutLineId(final UUID layoutLineId) {
		this.layoutLineId = layoutLineId;
	}

	@JsonBackReference
	public final Layout getLayout() {
		return layout;
	}

	@JsonBackReference
	public final void setLayout(final Layout layout) {
		this.layout = layout;
	}

	@JsonManagedReference
	public final List<Cell> getCells() {
		return cells;
	}

	@JsonManagedReference
	public final void setCells(final List<Cell> cells) {
		this.cells = cells;
	}

	@Override
	public final Entity getEntity() {
		return Entity.LINE;
	}

	@Override
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		if(Objects.requireNonNull(entity) == Entity.CELL) {
			return Collections.unmodifiableList(cells);
		}
		return Collections.emptyList();
	}
}
