package ch.rodano.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Use this interface to mark exception that are api oriented
 */
public interface ManagedException {
	/**
	 * Get the http error status of the exception
	 *
	 * @return The http error status of the exception
	 */
	HttpStatus getHttpErrorStatus();
}
