package ch.rodano.core.configuration.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Configurator implements InitializingBean {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Environment environment;
	private boolean maintenanceMode;

	private final File tempFolder;
	private final File fileFolder;
	private final File resourceFolder;

	public Configurator(
		@Value("${rodano.environment:DEV}") final Environment environment,
		@Value("${rodano.maintenance.enable:false}") final boolean maintenanceMode,
		@Value("${rodano.path.data}") final String rootFolder,
		@Value("${rodano.instance.uid}") final String instanceUuid
	) {
		this.maintenanceMode = maintenanceMode;
		this.environment = environment;

		Locale.setDefault(Locale.US);
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		tempFolder = Path.of(System.getProperty("java.io.tmpdir"), instanceUuid).toFile();
		fileFolder = Path.of(rootFolder, "files").toFile();
		resourceFolder = Path.of(rootFolder, "uploads").toFile();

		logger.info("Configurator initialized with UUID {}, environment {}, root folder {}, temporary folder {}", instanceUuid, environment, rootFolder, tempFolder);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		checkPaths();
	}

	/**
	 * Check that all initialized paths are valid
	 *
	 * @throws IOException Thrown if an error occurred while checking one the set path
	 */
	private void checkPaths() throws IOException {
		checkPath(tempFolder, true);
		checkPath(fileFolder, true);
		checkPath(resourceFolder, true);
	}

	/**
	 * Check if the given path is valid and optionally writable
	 *
	 * @param file     The file to check
	 * @param writable Check if the path is writable or not
	 * @throws IOException Thrown if an error occurred while checking the path
	 */
	private void checkPath(final File file, final boolean writable) throws IOException {

		// Create folder, this won't do anything if folder already exists
		file.mkdir();

		// Test folder constraints
		if(!file.isDirectory()) {
			throw new IOException(String.format("%s is not a directory", file.getAbsoluteFile()));
		}

		if(writable && !file.canWrite()) {
			throw new IOException(String.format("Unable to write in directory %s", file.getAbsolutePath()));
		}
	}

	public Environment getEnvironment() {
		return environment;
	}

	public boolean getMaintenanceMode() {
		return maintenanceMode;
	}

	public void setMaintenanceMode(final boolean maintenanceMode) {
		this.maintenanceMode = maintenanceMode;
	}

	public File getTempFolder() {
		return tempFolder;
	}

	public File getFileFolder() {
		return fileFolder;
	}

	public File getResourceFolder() {
		return resourceFolder;
	}

}
