package ch.rodano.core.services.plugin.validator.exception;

import java.io.Serial;

import ch.rodano.configuration.model.validator.ValueError;
import ch.rodano.core.model.exception.TechnicalException;
import ch.rodano.core.model.field.Field;

public class InvalidValueException extends Exception implements TechnicalException {

	@Serial
	private static final long serialVersionUID = -5565929659599689728L;

	private final String value;
	private final ValueError error;

	public InvalidValueException(final Field field, final String value, final ValueError error) {
		super(String.format("Value %s is not valid for field %s: %s", value, field.getFieldModelId(), error.getDefaultLocalizedMessage()));
		this.value = value;
		this.error = error;
	}

	public String getValue() {
		return value;
	}

	public final ValueError getError() {
		return error;
	}
}
