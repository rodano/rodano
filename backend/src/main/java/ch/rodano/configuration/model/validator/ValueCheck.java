package ch.rodano.configuration.model.validator;


public class ValueCheck {
	private ValueError error;
	private String sanitizedValue;

	/**
	 * Constructor
	 *
	 * @param error          An error
	 * @param sanitizedValue The sanitized value
	 */
	public ValueCheck(final ValueError error, final String sanitizedValue) {
		this.error = error;
		this.sanitizedValue = sanitizedValue;
	}

	/**
	 * Constructor
	 *
	 * @param error An error
	 */
	public ValueCheck(final ValueError error) {
		this(error, null);
	}

	/**
	 * Constructor
	 *
	 * @param value The value
	 */
	public ValueCheck(final String value) {
		this(null, value);
	}

	/**
	 * Check if the value has an error
	 *
	 * @return True if the value has an error and false otherwise
	 */
	public boolean hasError() {
		return error != null;
	}

	/**
	 * Check if the value is valid
	 *
	 * @return True if the value is valid and false otherwise
	 */
	public boolean isValid() {
		return !hasError() && sanitizedValue != null;
	}

	public ValueError getError() {
		return error;
	}

	public void setError(final ValueError error) {
		this.error = error;
	}

	public String getSanitizedValue() {
		return sanitizedValue;
	}

	public void setSanitizedValue(final String sanitizedValue) {
		this.sanitizedValue = sanitizedValue;
	}
}
