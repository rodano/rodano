package ch.rodano.configuration.validation.number;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.configuration.validation.AbstractValidationFieldModel;

public class NumberValidationFieldModel extends AbstractValidationFieldModel {

	public NumberValidationFieldModel(final int maxIntegerDigits, final int maxDecimalDigits) {
		fieldModel = new FieldModel();
		fieldModel.setId("FIELD_MODEL_NUMBER");
		fieldModel.setType(FieldModelType.NUMBER);
		fieldModel.setDataType(OperandType.NUMBER);
		fieldModel.setMaxLength(5);
		fieldModel.setMaxIntegerDigits(maxIntegerDigits);
		fieldModel.setMaxDecimalDigits(maxDecimalDigits);
		fieldModel.setExportable(false);
	}

	public NumberValidationFieldModel minValue(final double minValue) {
		fieldModel.setMinValue(minValue);
		return this;
	}

	public NumberValidationFieldModel maxValue(final double maxValue) {
		fieldModel.setMaxValue(maxValue);
		return this;
	}

	public NumberValidationFieldModel accepts(final String inputValue) {
		value = fieldModel.checkAndSanitizeValue(fieldModel.getPossibleValues(), inputValue);
		assertNotNull(value);
		assertNull(value.getError());
		return this;
	}

	public NumberValidationFieldModel asSanitized(final String expectedValue) {
		assertNotNull(value.getSanitizedValue());
		assertEquals(expectedValue, value.getSanitizedValue());
		return this;
	}

	// Rejection path

	public NumberValidationFieldModel rejects(final String inputValue) {
		value = fieldModel.checkAndSanitizeValue(fieldModel.getPossibleValues(), inputValue);
		assertNotNull(value);
		assertNotNull(value.getError(), String.format("Should have been rejected. Sanitized value is %s", value.getSanitizedValue()));
		return this;
	}

	public void withErrorCause(final String errorMessage) {
		assertEquals(errorMessage, value.getError().getLocalizedMessage(LANGUAGE));
	}
}
