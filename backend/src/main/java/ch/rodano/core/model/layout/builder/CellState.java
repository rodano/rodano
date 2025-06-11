package ch.rodano.core.model.layout.builder;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.configuration.model.layout.Cell;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.field.Field;

public class CellState {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final LayoutState layoutState;
	private final Cell cell;
	private final Dataset dataset;
	private final Field field;
	private boolean visible = true;

	public CellState(final LayoutState layoutState, final Cell cell, final Dataset dataset, final Field field) {
		this.layoutState = layoutState;
		this.cell = cell;
		this.dataset = dataset;
		this.field = field;
	}

	public Optional<CellState> getTargetCell(final String targetCellId) {
		//try to find cell on the same layout builder
		final Optional<CellState> cellState = layoutState.getCellState(targetCellId);
		if(cellState.isEmpty()) {
			logger.info(
				"No cell with id {} in layout {} from page {} (targetted by cell {}), may be hidden by a constraint",
				targetCellId,
				layoutState.getLayoutGroupState().getLayout().getId(),
				layoutState.getLayoutGroupState().getPageState().getFormModel().getId(),
				getCell().getId()
			);
		}
		return cellState;
	}

	public Optional<LayoutGroupState> getTargetLayout(final String targetLayoutId) {
		final var layoutGroupState = layoutState.getLayoutGroupState().getPageState().getLayoutGroup(targetLayoutId);
		if(layoutGroupState.isEmpty()) {
			logger.info(
				"No layout with id {} in page {} (targetted by cell {} from layout {}), may be hidden by a constraint",
				targetLayoutId,
				layoutState.getLayoutGroupState().getPageState().getFormModel().getId(),
				getCell().getId(),
				layoutState.getLayoutGroupState().getLayout().getId()
			);
		}
		return layoutGroupState;
	}

	public final LayoutState getLayoutState() {
		return layoutState;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public final Field getField() {
		return field;
	}

	public final boolean hasField() {
		return field != null;
	}

	public final Cell getCell() {
		return cell;
	}

	public void setVisible(final boolean visible) {
		this.visible = visible;
	}

	public final boolean isVisible() {
		return visible;
	}
}
