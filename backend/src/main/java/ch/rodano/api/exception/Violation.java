package ch.rodano.api.exception;

public record Violation(
	String fieldName,
	String message
) { }
