package ch.rodano.configuration.model.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class Line implements Node {
	private static final long serialVersionUID = -5443844894479046768L;

	private List<Cell> cells;
	private Layout layout;

	public Line() {
		cells = new ArrayList<>();
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
		switch(entity) {
			case CELL:
				return Collections.unmodifiableList(cells);
			default:
				return Collections.emptyList();
		}
	}
}
