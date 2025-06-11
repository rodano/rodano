package ch.rodano.configuration.validation.date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.configuration.validation.AbstractValidationFieldModel;

public class DateValidationFieldModel extends AbstractValidationFieldModel {

	public DateValidationFieldModel(final FieldModelType type, final boolean withYears, final boolean withMonths, final boolean withDays,  final boolean withHours, final boolean withMinutes, final boolean withSeconds) {
		fieldModel = new FieldModel();
		fieldModel.setId("FIELD_MODEL_DATE");
		fieldModel.setType(type);
		fieldModel.setDataType(OperandType.DATE);
		fieldModel.setWithYears(withYears);
		fieldModel.setWithMonths(withMonths);
		fieldModel.setWithDays(withDays);
		fieldModel.setWithHours(withHours);
		fieldModel.setWithMinutes(withMinutes);
		fieldModel.setWithSeconds(withSeconds);
		fieldModel.setYearsMandatory(false);
		fieldModel.setMonthsMandatory(false);
		fieldModel.setDaysMandatory(false);
		fieldModel.setHoursMandatory(false);
		fieldModel.setMinutesMandatory(false);
		fieldModel.setSecondsMandatory(false);
	}

	// Acceptation path
	public void accepts(final String inputValue) {
		value = fieldModel.checkAndSanitizeValue(fieldModel.getPossibleValues(), inputValue);
		assertNotNull(value);
		assertNull(value.getError());
		assertNotNull(value.getSanitizedValue());
		assertEquals(value.getSanitizedValue(), inputValue);
		assertNull(value.getError());
	}

	// Rejection path
	public DateValidationFieldModel rejects(final String inputValue) {
		value = fieldModel.checkAndSanitizeValue(fieldModel.getPossibleValues(), inputValue);
		assertNotNull(value);
		assertNotNull(value.getError(), String.format("Sanitized value is %s", value.getSanitizedValue()));
		return this;
	}

	public void withErrorCause(final String errorMessage) {
		assertEquals(errorMessage, value.getError().getLocalizedMessage(LANGUAGE));
	}
}
