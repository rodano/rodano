package ch.rodano.core.services.plugin.validator.exception;

import java.io.Serial;

import ch.rodano.core.model.exception.TechnicalException;
import ch.rodano.core.model.field.Field;

public class BadlyFormattedValue extends Exception implements TechnicalException {

	@Serial
	private static final long serialVersionUID = -5565929659599689728L;

	private final String desiredValue;
	private final String sanitizedValue;

	public BadlyFormattedValue(final Field field, final String desiredValue, final String sanitizedValue) {
		super(String.format("Value does not have the correct format: entered value is %s, expected value is %s", desiredValue, sanitizedValue));
		this.desiredValue = desiredValue;
		this.sanitizedValue = sanitizedValue;
	}

	public final String getDesiredValue() {
		return desiredValue;
	}

	public final String getSanitizedValue() {
		return sanitizedValue;
	}
}
