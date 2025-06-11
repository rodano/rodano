package ch.rodano.configuration.validation.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.configuration.validation.AbstractValidationFieldModel;

public class TextValidationFieldModel extends AbstractValidationFieldModel {

	public TextValidationFieldModel() {
		fieldModel = new FieldModel();
		fieldModel.setId("FIELD_MODEL_TEXT");
		fieldModel.setType(FieldModelType.STRING);
		fieldModel.setDataType(OperandType.STRING);
		fieldModel.setExportable(false);
	}

	public TextValidationFieldModel maxLength(final int maxLength) {
		fieldModel.setMaxLength(maxLength);
		return this;
	}

	public TextValidationFieldModel matcher(final String matcher, final Map<String, String> matcherMessage) {
		fieldModel.setMatcher(matcher);
		fieldModel.setMatcherMessage(matcherMessage);
		return this;
	}

	public void accepts(final String inputValue) {
		value = fieldModel.checkAndSanitizeValue(fieldModel.getPossibleValues(), inputValue);
		assertNull(value.getError());
		assertNotNull(value);
		assertNotNull(value.getSanitizedValue());
		assertEquals(value.getSanitizedValue(), inputValue);
	}

	public TextValidationFieldModel rejects(final String inputValue) {
		value = fieldModel.checkAndSanitizeValue(fieldModel.getPossibleValues(), inputValue);
		assertNotNull(value);
		assertNotNull(value.getError(), String.format("Sanitized value is %s", value.getSanitizedValue()));
		return this;
	}

	public void withErrorCause(final String errorMessage) {
		assertEquals(value.getError().getLocalizedMessage(LANGUAGE), errorMessage);
	}
}
