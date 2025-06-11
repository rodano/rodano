package ch.rodano.core.services.plugin.validator.exception;

import java.io.Serial;

import ch.rodano.configuration.model.validator.Validator;
import ch.rodano.core.model.exception.TechnicalException;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.rules.data.DataEvaluation;

public class ValidatorException extends Exception implements TechnicalException {
	@Serial
	private static final long serialVersionUID = 8964273245396515249L;

	private final DataEvaluation evaluation;
	private final Validator validator;

	public ValidatorException(final Field field, final Validator validator, final DataEvaluation evaluation) {
		super(String.format("Value %s is not valid for field %s according to validator %s", field.getValue(), field.getFieldModelId(), validator.getId()));
		this.validator = validator;
		this.evaluation = evaluation;
	}

	public ValidatorException(final Field value, final Validator validator) {
		this(value, validator, null);
	}

	public Validator getValidator() {
		return validator;
	}

	public DataEvaluation getEvaluation() {
		return evaluation;
	}

	public String getUserFriendlyErrorMessage(final String... languages) {
		if(validator.isRequired()) {
			if(validator.hasMessage()) {
				return validator.getLocalizedMessage(languages);
			}
			return Validator.REQUIRED.get(languages[0]);
		}
		return validator.getLocalizedMessage(languages);
	}

}
