package ch.rodano.configuration.model.validator;

import org.apache.commons.lang3.StringUtils;

import ch.rodano.configuration.model.field.FieldModel;

public class TextValidator implements ValueFormatValidator {
	private final FieldModel fieldModel;

	/**
	 * Constructor
	 *
	 * @param fieldModel
	 */
	public TextValidator(final FieldModel fieldModel) {
		this.fieldModel = fieldModel;
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

		return value.trim();
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
		final var maxLength = fieldModel.getMaxLengthOrDefault();
		if(maxLength != null && value.length() > maxLength) {
			return InvalidText.tooLong(maxLength);
		}

		if(StringUtils.isNotBlank(fieldModel.getMatcher()) && !value.matches(fieldModel.getMatcher())) {
			return InvalidText.doesNotMatch(fieldModel.getMatcherMessage(), fieldModel.getMatcher());
		}

		return new ValueCheck(value);
	}
}
