package ch.rodano.core.model.rules;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.rodano.configuration.model.field.PartialDate;
import ch.rodano.core.model.rules.formula.FormulaFunction;
import ch.rodano.core.model.rules.formula.exception.FormulaNullParameterException;
import ch.rodano.core.model.rules.formula.exception.FormulaWrongFunctionParametersException;
import ch.rodano.core.model.rules.formula.exception.FormulaWrongParameterTypeException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FormulaFunctionTest {

	@Test
	@DisplayName("Test function that calculates the difference in seconds between two dates")
	public void test_difference_in_seconds() throws FormulaWrongFunctionParametersException {
		final var date11 = PartialDate.of(2022, 2, 1);
		final var date12 = PartialDate.of(2022, 3, 1);
		assertEquals(2419200d, FormulaFunction.DIFFERENCE_IN_SECONDS.getValue(Arrays.asList(date11, date12)));
		assertEquals(2419200d, FormulaFunction.DIFFERENCE_IN_SECONDS.getValue(Arrays.asList(date12, date11)));
		assertEquals(0d, FormulaFunction.DIFFERENCE_IN_SECONDS.getValue(Arrays.asList(date11, date11)));
	}

	@Test
	@DisplayName("Test function that calculates the difference in months between two dates")
	public void test_difference_in_months() throws FormulaWrongFunctionParametersException {
		final var date11 = PartialDate.of(2000, 1, 1);
		final var date12 = PartialDate.of(2000, 1, 31);
		assertEquals(0d, FormulaFunction.DIFFERENCE_IN_MONTHS.getValue(Arrays.asList(date11, date12)));
		assertEquals(0d, FormulaFunction.DIFFERENCE_IN_MONTHS.getValue(Arrays.asList(date12, date11)));

		//despite being a shorter period than the previous test, the "age" between the two following dates must be equal to 1
		final var date21 = PartialDate.of(2022, 2, 1);
		final var date22 = PartialDate.of(2022, 3, 1);
		assertEquals(1d, FormulaFunction.DIFFERENCE_IN_MONTHS.getValue(Arrays.asList(date21, date22)));
		assertEquals(1d, FormulaFunction.DIFFERENCE_IN_MONTHS.getValue(Arrays.asList(date22, date21)));
		assertEquals(0d, FormulaFunction.DIFFERENCE_IN_MONTHS.getValue(Arrays.asList(date21, date21)));

		final var date31 = PartialDate.of(2021, 3, 15);
		final var date32 = PartialDate.of(2022, 3, 15);
		assertEquals(12d, FormulaFunction.DIFFERENCE_IN_MONTHS.getValue(Arrays.asList(date31, date32)));

		final var date41 = PartialDate.of(2021, 3, 15);
		final var date42 = PartialDate.of(2022, 3, 14);
		assertEquals(11d, FormulaFunction.DIFFERENCE_IN_MONTHS.getValue(Arrays.asList(date41, date42)));
	}

	@Test
	@DisplayName("Test function that calculates the difference in years beween two dates")
	public void test_difference_in_years() throws FormulaWrongFunctionParametersException {
		final var date11 = PartialDate.of(2000, 6, 1);
		final var date12 = PartialDate.of(2000, 5, 30);
		assertEquals(0d, FormulaFunction.DIFFERENCE_IN_YEARS.getValue(Arrays.asList(date11, date12)));
		assertEquals(0d, FormulaFunction.DIFFERENCE_IN_YEARS.getValue(Arrays.asList(date12, date11)));

		final var date21 = PartialDate.of(2020, 2, 1);
		final var date22 = PartialDate.of(2022, 3, 1);
		assertEquals(2d, FormulaFunction.DIFFERENCE_IN_YEARS.getValue(Arrays.asList(date21, date22)));
		assertEquals(2d, FormulaFunction.DIFFERENCE_IN_YEARS.getValue(Arrays.asList(date22, date21)));
	}

	@Test
	@DisplayName("'Greater than' function")

	public void test_greather_than() throws FormulaWrongFunctionParametersException {
		assertTrue((Boolean) FormulaFunction.IS_GREATER_THAN.getValue(Arrays.asList(5d, 3d)));
		assertTrue((Boolean) FormulaFunction.IS_GREATER_THAN.getValue(Arrays.asList(0d, -5d)));

		assertFalse((Boolean) FormulaFunction.IS_GREATER_THAN.getValue(Arrays.asList(3d, 3d)));
		assertFalse((Boolean) FormulaFunction.IS_GREATER_THAN.getValue(Arrays.asList(3d, 5d)));
	}

	@Test
	@DisplayName("'Greater or equal to' function")
	public void test_greather_or_equals_to() throws FormulaWrongFunctionParametersException {
		assertTrue((Boolean) FormulaFunction.IS_GREATER_OR_EQUAL_TO.getValue(Arrays.asList(5d, 3d)));
		assertTrue((Boolean) FormulaFunction.IS_GREATER_OR_EQUAL_TO.getValue(Arrays.asList(0d, -5d)));
		assertTrue((Boolean) FormulaFunction.IS_GREATER_OR_EQUAL_TO.getValue(Arrays.asList(3d, 3d)));

		assertFalse((Boolean) FormulaFunction.IS_GREATER_OR_EQUAL_TO.getValue(Arrays.asList(3d, 5d)));
	}

	@Test
	@DisplayName("'Less than' function")
	public void test_lower_than() throws FormulaWrongFunctionParametersException {
		assertTrue((Boolean) FormulaFunction.IS_LESS_THAN.getValue(Arrays.asList(3d, 5d)));
		assertTrue((Boolean) FormulaFunction.IS_LESS_THAN.getValue(Arrays.asList(-5d, 0d)));

		assertFalse((Boolean) FormulaFunction.IS_LESS_THAN.getValue(Arrays.asList(3d, 3d)));
		assertFalse((Boolean) FormulaFunction.IS_LESS_THAN.getValue(Arrays.asList(5d, 3d)));
	}

	@Test
	@DisplayName("'Less or equal to' function")
	public void test_lower_or_equals_to() throws FormulaWrongFunctionParametersException {
		assertTrue((Boolean) FormulaFunction.IS_LESS_OR_EQUAL_TO.getValue(Arrays.asList(3d, 5d)));
		assertTrue((Boolean) FormulaFunction.IS_LESS_OR_EQUAL_TO.getValue(Arrays.asList(-5d, 0d)));
		assertTrue((Boolean) FormulaFunction.IS_LESS_OR_EQUAL_TO.getValue(Arrays.asList(3d, 3d)));

		assertFalse((Boolean) FormulaFunction.IS_LESS_OR_EQUAL_TO.getValue(Arrays.asList(5d, 3d)));
	}

	@Test
	@DisplayName("Conversion to big decimal works")
	public void test_to_big_decimal() {
		assertEquals(BigDecimal.valueOf(3d), FormulaFunction.toBigDecimal(3d));
		assertEquals(BigDecimal.valueOf(3d), FormulaFunction.toBigDecimal(3));
		assertThrows(FormulaWrongParameterTypeException.class, () -> FormulaFunction.toBigDecimal("3"));
		assertThrows(FormulaNullParameterException.class, () -> FormulaFunction.toBigDecimal(null));
		assertThrows(FormulaWrongParameterTypeException.class, () -> FormulaFunction.toBigDecimal("TOTO"));
	}

	@Test
	@DisplayName("Test sum")
	public void test_sum() throws FormulaWrongFunctionParametersException {
		final Double operand1 = 5.0;
		final Double operand2 = 3.0;
		assertEquals(8d, FormulaFunction.SUM.getValue(Arrays.asList(operand1, operand2)));
		assertEquals(8d, FormulaFunction.SUM.getValue(Arrays.asList(5d, 3d)));
		assertEquals(3d, FormulaFunction.SUM.getValue(Arrays.asList(3d)));
		assertThrows(FormulaWrongParameterTypeException.class, () -> FormulaFunction.SUM.getValue(Arrays.asList("TOTO")));
		assertThrows(FormulaNullParameterException.class, () -> FormulaFunction.SUM.getValue(Arrays.asList(5d, 3d, null, "NA", "2")));
		assertThrows(FormulaWrongParameterTypeException.class, () -> FormulaFunction.SUM.getValue(Arrays.asList("TOTO", null)));
		assertThrows(FormulaWrongParameterTypeException.class, () -> FormulaFunction.SUM.getValue(Arrays.asList("TOTO", null, 2)));
	}

	@Test
	@DisplayName("Test multiply")
	public void test_multiply() throws FormulaWrongFunctionParametersException {
		final Double operand1 = 5.0;
		final Double operand2 = 3.0;
		assertEquals(15d, FormulaFunction.MULTIPLY.getValue(Arrays.asList(operand1, operand2)));
		assertEquals(15d, FormulaFunction.MULTIPLY.getValue(Arrays.asList(5d, 3d)));
		assertEquals(5d, FormulaFunction.MULTIPLY.getValue(Arrays.asList(5d)));
		assertThrows(FormulaNullParameterException.class, () -> FormulaFunction.MULTIPLY.getValue(Arrays.asList(5d, 3d, null, "NA", "2")));
		assertThrows(FormulaWrongParameterTypeException.class, () -> FormulaFunction.MULTIPLY.getValue(Arrays.asList("TITI", null)));
		assertThrows(FormulaWrongParameterTypeException.class, () -> FormulaFunction.MULTIPLY.getValue(Arrays.asList("TITI", null, 2)));
	}

	@Test
	@DisplayName("Test power function")
	public void test_power() throws FormulaWrongFunctionParametersException {
		assertEquals(1d, FormulaFunction.POWER.getValue(Arrays.asList(4d, 0d)));
		assertEquals(4d, FormulaFunction.POWER.getValue(Arrays.asList(2d, 2d)));
		assertEquals(4d, FormulaFunction.POWER.getValue(Arrays.asList(2d, 2d)));
		assertEquals(-8d, FormulaFunction.POWER.getValue(Arrays.asList(-2d, 3d)));
		assertEquals(2d, FormulaFunction.POWER.getValue(Arrays.asList(8d, 1d / 3d)));
		assertEquals(2d, FormulaFunction.POWER.getValue(Arrays.asList(8d, FormulaFunction.DIVIDE.getValue(Arrays.asList(1d, 3d)))));
		assertEquals(2d, FormulaFunction.POWER.getValue(Arrays.asList(4d, 0.5d)));
		assertEquals(0.01d, FormulaFunction.POWER.getValue(Arrays.asList(10d, -2d)));
	}

	@Test
	@DisplayName("Test square root function")
	public void test_square_root() throws FormulaWrongFunctionParametersException {
		assertEquals(0d, FormulaFunction.SQRT.getValue(Arrays.asList(0d)));
		assertEquals(1d, FormulaFunction.SQRT.getValue(Arrays.asList(1d)));
		assertEquals(2d, FormulaFunction.SQRT.getValue(Arrays.asList(4d)));
		assertEquals(Double.NaN, FormulaFunction.SQRT.getValue(Arrays.asList(-1d)));
		assertEquals(Math.sqrt(3d), FormulaFunction.SQRT.getValue(Arrays.asList(3d)));
	}

	@Test
	@DisplayName("Test average function")
	public void test_average() throws FormulaWrongFunctionParametersException {
		assertEquals(5d, FormulaFunction.AVERAGE.getValue(Arrays.asList(1d, 8d, 4d, 2d, 10d)));
		assertEquals(5.2d, FormulaFunction.AVERAGE.getValue(Arrays.asList(2d, 8d, 4d, 2d, 10d)));
		assertEquals(5.333333333333333d, FormulaFunction.AVERAGE.getValue(Arrays.asList(null, 4d, 2d, 10d)));
	}

	@Test
	@DisplayName("Test median function")
	public void test_median() throws FormulaWrongFunctionParametersException {
		assertEquals(7d, FormulaFunction.MEDIAN.getValue(Arrays.asList(1d, 8d, 7d, 2d, 100d)));
		assertEquals(4d, FormulaFunction.MEDIAN.getValue(Arrays.asList(4d, 2d, 10d)));
		assertEquals(4d, FormulaFunction.MEDIAN.getValue(Arrays.asList(null, 4d, 2d, 10d)));
	}

	@Test
	@DisplayName("Test min function")
	public void test_min() throws FormulaWrongFunctionParametersException {
		assertEquals(2d, FormulaFunction.MIN.getValue(Arrays.asList(6.2d, 8d, 4d, 2d, 10d, 2d)));
		assertThrows(FormulaNullParameterException.class, () -> FormulaFunction.MIN.getValue(Arrays.asList(null, 4d, 3.6d, 10d)));
	}

	@Test
	@DisplayName("Test max function")
	public void test_max() throws FormulaWrongFunctionParametersException {
		assertEquals(10d, FormulaFunction.MAX.getValue(Arrays.asList(6.2d, 8d, 4d, 2d, 10d, 2d)));
		assertThrows(FormulaNullParameterException.class, () -> FormulaFunction.MAX.getValue(Arrays.asList(null, 4d, 3.6d, 18d)));
	}
}
