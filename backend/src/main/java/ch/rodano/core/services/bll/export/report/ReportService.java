package ch.rodano.core.services.bll.export.report;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.scope.Scope;

public interface ReportService {
	/**
	 * Stream the transfert report for a leaf scope model
	 *
	 * @param out            The output stream where to send the report
	 * @param rootScopes     Generate the report for all transfers in these root scopes
	 * @param scopeModel     The scope model used for the report
	 */
	void getTransferReport(OutputStream out, Collection<Scope> rootScopes, ScopeModel scopeModel) throws IOException;

	/**
	 * Stream the event report for a leaf scope model
	 *
	 * @param out            The output stream where to send the report
	 * @param rootScopes     Generate the report for all events in these root scopes
	 * @param scopeModel     The scope model of the visits
	 */
	void getVisitReport(OutputStream out, Collection<Scope> rootScopes, ScopeModel scopeModel) throws IOException;

	/**
	 * Stream the data structure of a dataset model
	 *
	 * @param out                       The output stream in which the report will be produced
	 * @param datasetModel              The dataset model
	 * @param withModificationDates     Include the modification dates ?
	 * @param languages                 Languages of the output document
	 */
	void getDataStructure(
		OutputStream out,
		DatasetModel datasetModel,
		boolean withModificationDates,
		String... languages
	) throws IOException;

	/**
	 * Stream several data structure files into a zip stream
	 *
	 * @param out                       The output stream in which the report will be produced
	 * @param datasetModels             Dataset models
	 * @param withModificationDates     Include the modification dates ?
	 * @param languages                 Languages of the output document
	 */
	void zipDataStructures(
		OutputStream out,
		Collection<DatasetModel> datasetModels,
		boolean withModificationDates,
		String... languages
	);

	String getDataStructureFilename(DatasetModel datasetModel);
}
