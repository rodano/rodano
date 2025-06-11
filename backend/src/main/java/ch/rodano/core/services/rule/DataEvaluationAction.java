package ch.rodano.core.services.rule;

import ch.rodano.configuration.model.layout.Cell;

public interface DataEvaluationAction {
	/**
	 * Execute an action
	 *
	 * @param evaluationValid Whether the data evaluation is positive or negative
	 * @param cell            The cell which has been evaluated
	 */
	void execute(Boolean evaluationValid, Cell cell);
}
