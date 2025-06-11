package ch.rodano.core.model.layout.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.exception.MissingDataException;

public class LayoutState {
	private final LayoutGroupState layoutGroupState;
	private final Map<String, Dataset> datasets;

	private final List<CellState> cellStates = new ArrayList<>();

	public LayoutState(final LayoutGroupState layoutGroupState, final Map<String, Dataset> datasets) {
		this.layoutGroupState = layoutGroupState;
		this.datasets = datasets;
	}

	public final Map<String, Dataset> getDatasets() {
		return datasets;
	}

	public void addCellState(final CellState cellState) {
		cellStates.add(cellState);
	}

	public List<CellState> getCellStates() {
		return cellStates;
	}

	public final LayoutGroupState getLayoutGroupState() {
		return layoutGroupState;
	}

	public Optional<CellState> getCellState(final String cellId) {
		return cellStates.stream().filter(c -> c.getCell().getId().equals(cellId)).findFirst();
	}

	@JsonIgnore
	public final Dataset getReferenceDataset() {
		if(getLayoutGroupState().getLayout().getType().hasReferenceDataset()) {
			return getDatasets().entrySet().iterator().next().getValue();
		}
		throw new MissingDataException("No reference dataset for this kind of layout");
	}
}
