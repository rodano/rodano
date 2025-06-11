package ch.rodano.core.services.bll.export.extract;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.core.model.scope.Scope;

public interface ExtractService {
	/**
	 * Get the export info and write the export file to the given output stream in CSV format.
	 *
	 * @param out                   Output stream to which the file is written.
	 * @param datasetModel          The dataset model for which the export will be made.
	 * @param languageId            Language of the export.
	 * @param scopes                Scopes for which the export will be performed.
	 * @param withModificationDates Should the modification dates be included ?
	 */
	void getDataExtract(
		final OutputStream out,
		final DatasetModel datasetModel,
		final String languageId,
		final Collection<Scope> scopes,
		boolean withModificationDates
	) throws IOException;

	/**
	 * Get the export info on several datasetModels and write all the export files to the given output stream in ZIP format.
	 *
	 * @param out                   Output stream to which the files are written.
	 * @param datasetModels         Dataset models for which the export will be made.
	 * @param languageId            Language of the exports.
	 * @param scopes                Scopes for which the export will be performed.
	 * @param withModificationDates Should the modification dates be included ?
	 */
	void zipExtracts(
		OutputStream out,
		List<DatasetModel> datasetModels,
		String languageId,
		Collection<Scope> scopes,
		boolean withModificationDates
	);

	String getCSVDocumentFilename(DatasetModel datasetModel);
}
