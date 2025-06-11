package ch.rodano.configuration.model.validator;

import org.apache.commons.lang3.StringUtils;

import ch.rodano.configuration.model.field.FieldModel;

public class NumberValidator implements ValueFormatValidator {
	private final FieldModel fieldModel;

	/**
	 * Constructor
	 *
	 * @param fieldModel
	 */
	public NumberValidator(final FieldModel fieldModel) {
		this.fieldModel = fieldModel;
	}

	/**
	 * Returns true if provided string value is a valid number.
	 * The string value is trimmed internally before conversion.
	 *
	 * @param value
	 * @return
	 * @see java.lang.Double#valueOf(String)
	 */
	public boolean isNumber(final String value) {
		try {
			Double.valueOf(value);
			return true;
		}
		catch(final NullPointerException | NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Trims and sanitizes the provided string value, based on the field model type.
	 *
	 * @param value to sanitize
	 * @return sanitized value
	 */
	// TODO move method in the right class: this can be used outside for other field model types as well
	public String sanitizeValue(final String value) {
		if(StringUtils.isBlank(value)) {
			return "";
		}

		var sanitizedValue = value.trim();

		// Remove ending '.' and add leading zero if needed
		if(isNumber(value)) {
			// Strip leading zeros (special case: "0.0" -> ".0")
			sanitizedValue = value.replaceFirst("^0+(?!$)", "");

			if(fieldModel.isDecimal()) {
				// Add a single leading zero for values starting with dot
				if(StringUtils.startsWith(sanitizedValue, ".")) {
					return "0" + sanitizedValue;
				}

				// Strip ending dot
				if(StringUtils.endsWith(sanitizedValue, ".")) {
					return StringUtils.substringBeforeLast(sanitizedValue, ".");
				}
			}
		}

		return sanitizedValue;
	}

	/**
	 * This method must receive a sanitized trimmed string value.
	 * It returns either an object containing the sanitized value, or a format error.
	 *
	 * @param value
	 * @return ValueCheck
	 */
	@Override
	public ValueCheck validate(final String value) {
		// Check if value is a number
		if(!isNumber(value)) {
			return InvalidNumber.wrongType();
		}

		//check if number match regexp
		if(StringUtils.isNotBlank(fieldModel.getMatcher()) && !value.matches(fieldModel.getMatcher())) {
			return InvalidNumber.doesNotMatch(fieldModel.getMatcherMessage(), fieldModel.getMatcher());
		}

		if(fieldModel.isDecimal()) {
			// Validate integer and fractional parts according to given format pattern
			final var integerDigits = StringUtils.substringBefore(value, ".").length();
			if(integerDigits > fieldModel.getMaxIntegerDigits()) {
				return InvalidDecimal.tooManyIntegerDigits(value, fieldModel.getMaxIntegerDigits());
			}
			final var decimalDigits = StringUtils.substringAfter(value, ".").length();
			if(decimalDigits > fieldModel.getMaxDecimalDigits()) {
				return InvalidDecimal.tooManyFractionDigits(value, fieldModel.getMaxDecimalDigits());
			}

		}
		else {
			// Number is configured as an integer
			if(StringUtils.contains(value, ".")) {
				return InvalidInteger.wrongType();
			}

			// Validate integer part according to given format pattern
			if(value.length() > fieldModel.getMaxIntegerDigits()) {
				return InvalidInteger.tooManyDigits(value, fieldModel.getMaxIntegerDigits());
			}
		}

		if(fieldModel.getMinValue() != null || fieldModel.getMaxValue() != null) {
			final Double numberValue = Double.parseDouble(value);
			if(fieldModel.getMinValue() != null && numberValue < fieldModel.getMinValue()) {
				return InvalidNumber.tooSmall(fieldModel.getNumberFormatter(), fieldModel.getMinValue());
			}
			if(fieldModel.getMaxValue() != null && numberValue > fieldModel.getMaxValue()) {
				return InvalidNumber.tooBig(fieldModel.getNumberFormatter(), fieldModel.getMaxValue());
			}
		}

		return new ValueCheck(value);
	}

}
