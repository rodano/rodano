package ch.rodano.api.exception;

import org.springframework.http.HttpStatus;

import ch.rodano.core.model.exception.TechnicalException;

public class ForbiddenToWriteToJarFileException extends RuntimeException implements ManagedException, TechnicalException {
	private static final long serialVersionUID = -4204084558383591325L;

	public ForbiddenToWriteToJarFileException() {
		super("Cannot write the configuration in the current environment");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.PRECONDITION_FAILED;
	}
}
