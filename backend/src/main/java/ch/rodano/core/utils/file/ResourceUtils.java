package ch.rodano.core.utils.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import ch.rodano.api.exception.ForbiddenToWriteToJarFileException;

public class ResourceUtils {

	/**
	 * Read a resource
	 *
	 * @param resource The resource to read
	 * @return An input stream of the resource
	 * @throws FileNotFoundException Thrown if an error occurred when trying to access the resource
	 */
	public static InputStream readResource(final String resource) throws FileNotFoundException {
		// check if it is a jar file
		final var isJarFile = isJarFile(resource);

		if(isJarFile) {
			// Read inside a jar which could be a dependency or read the actual project (local dev mode)
			final var jarIndex = resource.indexOf("jar!");
			return ResourceUtils.class.getResourceAsStream(resource.substring(jarIndex + 4));
		}
		else if(!resource.startsWith("/")) {
			return ResourceUtils.class.getClassLoader().getResourceAsStream(resource);
		}
		else {
			// Read a file which could be anywhere but not in a jar
			return new FileInputStream(resource);
		}
	}

	/**
	 * Write a input stream to a file (only works for resources on the file system)
	 *
	 * @param resource  The resource to write in
	 * @param is        The input stream to transfer to the file
	 */
	public static void writeResource(final String resource, final InputStream is) {
		// check if it is a jar file
		if(isJarFile(resource)) {
			// Cannot write into a jar
			throw new ForbiddenToWriteToJarFileException();
		}

		// Write into file which could be anywhere but not in a jar
		try(OutputStream os = new PrintStream(resource, StandardCharsets.UTF_8)) {
			is.transferTo(os);
		}
		catch(final IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Write a content to a file (only works for resources on the file system)
	 *
	 * @param resource  The resource to write in
	 * @param content   The content of the resource to write
	 */
	public static void writeResource(final String resource, final byte[] content) {
		// check if it is a jar file
		if(isJarFile(resource)) {
			// Cannot write into a jar
			throw new ForbiddenToWriteToJarFileException();
		}

		// Write into file which could be anywhere but not in a jar
		try(var os = new PrintStream(resource, StandardCharsets.UTF_8)) {
			os.write(content);
		}
		catch(final IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Check if the given resource is a jar file
	 */
	private static boolean isJarFile(final String resource) {
		return resource.contains("jar!");
	}
}
