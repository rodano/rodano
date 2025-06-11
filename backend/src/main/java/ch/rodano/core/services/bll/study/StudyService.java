package ch.rodano.core.services.bll.study;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ch.rodano.configuration.model.study.Study;

public interface StudyService {

	/**
	 * Read the current configuration to the specified output stream
	 * @param os
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	void read(OutputStream os) throws FileNotFoundException, IOException;

	/**
	 * Update the current configuration with the content of the input stream
	 * @param is
	 * @param compressed
	 * @throws IOException
	 */
	void save(InputStream is, boolean compressed) throws IOException;

	/**
	 * Reload the configuration of the study
	 * @throws IOException
	 */
	void reload() throws IOException;

	/**
	 * @return the study
	 */
	Study getStudy();
}
