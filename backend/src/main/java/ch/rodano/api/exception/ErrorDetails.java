package ch.rodano.api.exception;

import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;

public record ErrorDetails(
	ZonedDateTime timestamp,
	int status,
	String error,
	String message,
	String path
) {
	public ErrorDetails(
		final HttpStatus status,
		final String message,
		final String path
	) {
		this(
			ZonedDateTime.now(),
			status.value(),
			status.getReasonPhrase(),
			message,
			path
		);
	}
}
