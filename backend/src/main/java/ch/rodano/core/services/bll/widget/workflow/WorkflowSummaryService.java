package ch.rodano.core.services.bll.widget.workflow;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import ch.rodano.api.dto.widget.SummaryDTO;
import ch.rodano.configuration.model.reports.WorkflowSummary;
import ch.rodano.core.model.scope.Scope;

public interface WorkflowSummaryService {

	/**
	 * Get the summary for selected scope.
	 *
	 * @param scope The selected scope
	 * @param workflowSummary The workflow summary
	 * @return The workflow summary
	 */
	SummaryDTO getSummary(WorkflowSummary workflowSummary, Scope scope);

	/**
	 * Write a CSV export on the provided output stream
	 *
	 * @param out                 The output stream
	 * @param summary             The summary
	 * @param scopes              Selected scopes
	 * @param includeDeleted      Included deleted workflow statuses in the export of not
	 * @param languages           The languages
	 * @throws IOException        Thrown if an IO error occurs
	 */
	void getExport(OutputStream out, WorkflowSummary summary, Collection<Scope> scopes, boolean includeDeleted, String[] languages) throws IOException;

	/**
	 * Write a CSV historical export on the provided output stream
	 *
	 * @param out                 The output stream
	 * @param summary             The summary
	 * @param scopes              Selected scopes
	 * @param includeDeleted      Included deleted workflow statuses in the export of not
	 * @param languages           The languages
	 * @throws IOException        Thrown if an IO error occurs
	 */
	void getHistoricalExport(OutputStream out, WorkflowSummary summary, Collection<Scope> scopes, boolean includeDeleted, String[] languages) throws IOException;
}
