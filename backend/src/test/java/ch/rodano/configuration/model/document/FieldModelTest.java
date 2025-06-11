package ch.rodano.configuration.model.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.field.PartialDate;

@Tag("configuration")
public class FieldModelTest {
	private static final String FIELD_MODEL_ID = "FIELD_MODEL";

	@Test
	@DisplayName("Date field model operations")
	public void dateOperations() {
		//Create field model
		final var fieldModel = new FieldModel();
		fieldModel.setId(FIELD_MODEL_ID);
		fieldModel.setType(FieldModelType.DATE);
		fieldModel.setWithYears(true);
		fieldModel.setWithMonths(true);
		fieldModel.setWithDays(true);

		var date = PartialDate.of(2020, 4, 22);

		assertEquals(date, fieldModel.stringToObject("22.04.2020"));
		assertEquals("22.04.2020", fieldModel.objectToString(date));

		date = PartialDate.of(2020);

		//date is equal to any date in 2020
		assertEquals(date, fieldModel.stringToObject("01.01.2020"));
		assertEquals(date, fieldModel.stringToObject("12.08.2020"));
		assertEquals("01.01.2020", fieldModel.objectToString(date));

		//change format
		fieldModel.setWithDays(false);

		assertEquals(date, fieldModel.stringToObject("01.2020"));
		assertEquals(date, fieldModel.stringToObject("08.2020"));
		assertEquals("01.2020", fieldModel.objectToString(date));

		date = PartialDate.of(2020, 8);
		assertNotEquals(date, fieldModel.stringToObject("01.2020"));
		assertEquals(date, fieldModel.stringToObject("08.2020"));
		assertEquals("08.2020", fieldModel.objectToString(date));
	}

	@Test
	@DisplayName("Date select field model operations")
	public void dateSelectOperations() {
		//Create field model
		final var fieldModel = new FieldModel();
		fieldModel.setId(FIELD_MODEL_ID);
		fieldModel.setType(FieldModelType.DATE_SELECT);
		fieldModel.setWithYears(true);
		fieldModel.setWithMonths(true);
		fieldModel.setWithDays(true);

		var date = PartialDate.of(2020, 4, 22);

		assertEquals(date, fieldModel.stringToObject("22.04.2020"));
		assertEquals("22.04.2020", fieldModel.objectToString(date));

		date = PartialDate.of(Optional.of(2020), Optional.empty(), Optional.empty());
		assertEquals(date, fieldModel.stringToObject("10.04.2020"));
		assertEquals(date, fieldModel.stringToObject("Unknown.04.2020"));
		assertEquals(date, fieldModel.stringToObject("Unknown.Unknown.2020"));
		assertEquals("Unknown.Unknown.2020", fieldModel.objectToString(date));

		date = PartialDate.TOTALLY_UNKNOWN;
		assertEquals(date, fieldModel.stringToObject("Unknown.Unknown.Unknown"));
		assertEquals("Unknown.Unknown.Unknown", fieldModel.objectToString(date));

		fieldModel.setWithDays(false);
		fieldModel.setWithMonths(false);

		date = PartialDate.of(2020);
		assertEquals(date, fieldModel.stringToObject("20.04.2020"));
		assertEquals("2020", fieldModel.objectToString(date));

		date = PartialDate.TOTALLY_UNKNOWN;
		assertEquals(date, fieldModel.stringToObject("Unknown"));
		assertEquals("Unknown", fieldModel.objectToString(date));
	}
}
