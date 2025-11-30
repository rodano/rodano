package ch.rodano.api.config;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class LayoutLineDTO {
	@NotNull
	UUID formLayoutLineId;

	@NotNull
	List<CellDTO> cells;

	public UUID getFormLayoutLineId() {
		return formLayoutLineId;
	}

	public void setFormLayoutLineId(final UUID formLayoutLineId) {
		this.formLayoutLineId = formLayoutLineId;
	}

	public List<CellDTO> getCells() {
		return cells;
	}

	public void setCells(final List<CellDTO> cells) {
		this.cells = cells;
	}
}
