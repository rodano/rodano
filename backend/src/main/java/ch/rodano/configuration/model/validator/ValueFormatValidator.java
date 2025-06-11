package ch.rodano.configuration.model.validator;

public interface ValueFormatValidator {
	/**
	 * Returns a validated value or an error.
	 *
	 * @return ValueCheck
	 */
	ValueCheck validate(String value);
}
