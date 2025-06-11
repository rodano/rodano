package ch.rodano.api.exception.http;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public class BadArgumentException extends RuntimeException implements ManagedException {

	@Serial
	private static final long serialVersionUID = 219070813892476147L;

	public BadArgumentException(final String message) {
		super(message);
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
