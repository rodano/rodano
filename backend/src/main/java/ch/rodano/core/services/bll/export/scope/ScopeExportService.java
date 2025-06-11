package ch.rodano.core.services.bll.export.scope;

import java.io.IOException;
import java.io.OutputStream;

import ch.rodano.core.model.scope.ScopeSearch;

public interface ScopeExportService {

	/**
	 * Export the scopes in a CSV file
	 * @param out        The output stream where the CSV will be written
	 * @param search  Given scope search
	 * @param languages  The languages
	 * @throws IOException Thrown if an IO error occurs
	 */
	void exportScopes(OutputStream out, ScopeSearch search, String[] languages) throws IOException;
}
