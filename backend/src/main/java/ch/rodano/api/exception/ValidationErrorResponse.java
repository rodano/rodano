package ch.rodano.api.exception;

import java.util.List;

public record ValidationErrorResponse(
	List<Violation> violations
) { }
