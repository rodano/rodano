package ch.rodano.api.config;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public class LayoutLineDTO {
	@NotNull
	List<CellDTO> cells;

	public List<CellDTO> getCells() {
		return cells;
	}

	public void setCells(final List<CellDTO> cells) {
		this.cells = cells;
	}
}
