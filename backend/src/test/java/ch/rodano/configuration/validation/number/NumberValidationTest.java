package ch.rodano.configuration.validation.number;

import java.text.DecimalFormat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.rodano.configuration.model.validator.InvalidDecimal;
import ch.rodano.configuration.model.validator.InvalidInteger;
import ch.rodano.configuration.model.validator.InvalidNumber;
import ch.rodano.configuration.validation.AbstractValidationFieldModel;

public class NumberValidationTest {

	// ---------------------------------------------------------------
	//  Number Tests
	// ---------------------------------------------------------------

	@Test
	@DisplayName("A single dot is invalid for any format")
	public void dotOnlyNumberIsInvalidForAnyFormat() {
		numberModel(1, 0).rejects(".").withErrorCause(wrongNumber());
		numberModel(1, 1).rejects(".").withErrorCause(wrongNumber());
		numberModel(0, 1).rejects(".").withErrorCause(wrongNumber());
	}

	@Test
	@DisplayName("Integer range is invalid")
	public void integerRangeIsInvalid() {
		numberModel(1, 0).minValue(2).rejects("1").withErrorCause(tooSmall("#", 2));
		numberModel(1, 0).minValue(2).accepts("2");
		numberModel(1, 0).minValue(2).accepts("3");

		numberModel(1, 0).maxValue(2).accepts("1");
		numberModel(1, 0).maxValue(2).accepts("2");
		numberModel(1, 0).maxValue(2).rejects("3").withErrorCause(tooBig("#", 2));
	}

	@Test
	@DisplayName("Decimal range is invalid")
	public void decimalRangeIsInvalid() {
		numberModel(2, 2).minValue(42.42).rejects("42.41").withErrorCause(tooSmall("#.#", 42.42));
		numberModel(2, 2).minValue(42.42).accepts("42.43");

		numberModel(2, 2).maxValue(42.42).accepts("42.41");
		numberModel(2, 2).maxValue(42.42).rejects("42.43").withErrorCause(tooBig("#.#", 42.42));
	}

	// ---------------------------------------------------------------
	//  Decimals Tests
	// ---------------------------------------------------------------

	@Test
	@DisplayName("Decimal with zero frac digit is valid for optional and frac format")
	public void decimalWithZeroFracDigitIsValidForOptionalIntAndFracFormat() {
		numberModel(1, 1).accepts("1.0").asSanitized("1.0");
	}

	@Test
	@DisplayName("Decimal with no dot is valid for optional int and frac format")
	public void decimalWithNoDotIsValidForOptionalIntAndFracFormat() {
		numberModel(1, 1).accepts("1").asSanitized("1");
	}

	@Test
	@DisplayName("Decimal ending with dot is valid for optional int and frac format and ending dot is stripped")
	public void decimalEndingWithDotIsValidForOptionalIntAndFracFormatAndEndingDotIsStripped() {
		numberModel(1, 1).accepts("1.").asSanitized("1");
	}

	@Test
	@DisplayName("Decimal starting with dot is valid for optional int and frac format and leading zero is added")
	public void decimalStartingWithDotIsValidForOptionalIntAndFracFormatAndLeadingZeroIsAdded() {
		numberModel(1, 1).accepts(".1").asSanitized("0.1");

	}

	// This test is kept for historic reasons: '0.1' used to be incorrectly sanitized to '.1'
	@Test
	@DisplayName("Decimal with zero int digit is valid for optional int and frac format")
	public void decimalWithZeroIntDigitIsValidForOptionalIntAndFracFormat() {
		numberModel(1, 1).accepts("0.1").asSanitized("0.1");
	}

	@Test
	@DisplayName("Decimal with non zero frac digit is valid for optional int and frac format")
	public void decimalWithNonZeroFracDigitIsValidForOptionalIntAndFracFormat() {
		numberModel(1, 1).accepts("1.1").asSanitized("1.1");
	}

	@Test
	@DisplayName("Decimal with too many frac digit is invalid for optional int and frac format")
	public void decimalWithTooManyFracDigitsIsInvalidForOptionalIntAndFracFormat() {
		numberModel(1, 1).rejects("1.11").withErrorCause(tooManyDecimalsDigits(1));
	}

	@Test
	@DisplayName("Decimal with leading zero and enough digit is valid for optional int and frac format")
	public void decimalWithLeadingZeroAndEnoughDigitsIsValidForOptionalIntAndFracFormat() {
		numberModel(1, 1).accepts("01").asSanitized("1");
	}

	@Test
	@DisplayName("Decimal with non zero frac digit is valid for optional int mandatory frac format")
	public void decimalWithNonZeroFracDigitIsValidForOptionalIntMandatoryFracFormat() {
		numberModel(1, 1).accepts("1.1").asSanitized("1.1");
	}

	// This test is kept for historic reasons: '1.0' used to be invalid, which should not happen anymore
	@Test
	@DisplayName("Decimal with zero frac digit is valid for optional int mandatory frac format")
	public void decimalWithZeroFracDigitIsValidForOptionalIntMandatoryFracFormat() {
		numberModel(1, 1).accepts("1.0").asSanitized("1.0");
	}

	@Test
	@DisplayName("Decimal zero is valid for mandatory int and frac format")
	public void decimalZeroIsValidForMandatoryIntAndFracFormat() {
		numberModel(1, 1).accepts("0.0").asSanitized("0.0");
	}

	@Test
	@DisplayName("Decimal with leading zero is valid for mandatory int and frac format")
	public void decimalWithLeadingZeroIsValidForMandatoryIntAndFracFormat() {
		numberModel(1, 1).accepts("01.0").asSanitized("1.0");
	}

	@Test
	@DisplayName("Decimal with too many int digit is invalid for mandatory int optional frac format")
	public void decimalWithTooManyIntDigitsIsInvalidForMandatoryIntOptionalFracFormat() {
		numberModel(2, 1).rejects("100").withErrorCause(tooManyIntegerDigits(2));
	}

	@Test
	@DisplayName("Decimal with no frac part is invalid for mandatory int and optional frac format")
	public void decimalWithNoFractPartIsInvalidForMandatoryIntAndOptionalFracFormat() {
		numberModel(1, 1).accepts("1").asSanitized("1");
	}

	@Test
	@DisplayName("Decimal with too many integer digits is invalid for mandatory int and frac format")
	public void decimalWithTooManyDigitsIsInvalidForMandatoryIntAndFracFormat() {
		numberModel(1, 1).rejects("11.1").withErrorCause(tooManyIntegerDigits(1));
	}

	@Test
	@DisplayName("Leading zero are stripped for decimal with any format")
	public void leadingZerosAreStrippedForDecimalWithAnyFormat() {
		numberModel(1, 1).accepts("00.0").asSanitized("0.0");
		numberModel(1, 1).accepts("00.0").asSanitized("0.0");
		numberModel(1, 1).accepts("00.0").asSanitized("0.0");
		numberModel(1, 1).accepts("00.0").asSanitized("0.0");
	}

	// ---------------------------------------------------------------
	//  Integer Tests
	// ---------------------------------------------------------------

	@Test
	@DisplayName("Decimal is invalid fot any integer format")
	public void decimalIsInvalidForAnyIntegerFormat() {
		numberModel(1, 0).rejects("1.0").withErrorCause(wrongInteger());
		numberModel(1, 0).rejects("1.1").withErrorCause(wrongInteger());
		numberModel(1, 0).rejects("1.0").withErrorCause(wrongInteger());
		numberModel(1, 0).rejects("1.1").withErrorCause(wrongInteger());
	}

	@Test
	@DisplayName("Integer with too many digits is invalid for optional format")
	public void integerWithTooManyDigitsIsInvalidForOptionalFormat() {
		numberModel(1, 0).rejects("11").withErrorCause(tooManyDigits(1));
		numberModel(2, 0).rejects("123").withErrorCause(tooManyDigits(2));
	}

	@Test
	@DisplayName("Integer zero with leading zero is valid for any format")
	public void integerZeroWithLeadingZeroIsValidForAnyFormat() {
		numberModel(1, 0).accepts("00").asSanitized("0");
		numberModel(1, 0).accepts("00").asSanitized("0");
	}

	@Test
	@DisplayName("Integer with too many digits is invalid for mandatory format")
	public void integerWithTooManyDigitsIsInvalidForMandatoryFormat() {
		numberModel(1, 0).rejects("11").withErrorCause(tooManyDigits(1));
	}

	@Test
	@DisplayName("Leading zero are stripped from integer with any format")
	public void leadingZerosAreStrippedFromIntegerWithAnyFormat() {
		numberModel(3, 0).accepts("00123").asSanitized("123");
		numberModel(3, 0).accepts("00123").asSanitized("123");
	}

	@Test
	@DisplayName("Integer zero with leading zero is valid for integer format")
	public void integerZeroWithLeadingZeroIsValidForIntegerFormat() {
		numberModel(1, 0).accepts("00").asSanitized("0");
		numberModel(2, 0).accepts("00").asSanitized("0");
	}

	@Test
	@DisplayName("Empty integer is valid for any format")
	public void emptyIntegerIsValidForAnyFormat() {
		numberModel(1, 0).accepts("").asSanitized("");
		numberModel(1, 0).accepts("").asSanitized("");
	}

	// ---------------------------------------------------------------
	// Helper methods
	// ---------------------------------------------------------------


	private NumberValidationFieldModel numberModel(final int maxIntegerDigits, final int maxDecimalDigits) {
		return new NumberValidationFieldModel(maxIntegerDigits, maxDecimalDigits);
	}

	private String wrongNumber() {
		return InvalidNumber.NOT_A_NUMBER.get(AbstractValidationFieldModel.LANGUAGE);
	}

	private String tooSmall(final String format, final double minValue) {
		final var formatter = new DecimalFormat(format);
		return String.format(InvalidNumber.TOO_SMALL.get(AbstractValidationFieldModel.LANGUAGE), formatter.format(minValue));
	}

	private String tooBig(final String format, final double maxValue) {
		final var formatter = new DecimalFormat(format);
		return String.format(InvalidNumber.TOO_BIG.get(AbstractValidationFieldModel.LANGUAGE), formatter.format(maxValue));
	}

	private String tooManyDecimalsDigits(final int maxExpectedDecimals) {
		return String.format(InvalidDecimal.TOO_MANY_FRACTION_DIGITS.get(AbstractValidationFieldModel.LANGUAGE), maxExpectedDecimals);
	}

	private String wrongInteger() {
		return InvalidInteger.NOT_AN_INTEGER.get(AbstractValidationFieldModel.LANGUAGE);
	}

	private String tooManyIntegerDigits(final int maxExpectedDigits) {
		return String.format(InvalidDecimal.TOO_MANY_INTEGER_DIGITS.get(AbstractValidationFieldModel.LANGUAGE), maxExpectedDigits);
	}

	private String tooManyDigits(final int maxExpectedDigits) {
		return String.format(InvalidInteger.TOO_MANY_DIGITS.get(AbstractValidationFieldModel.LANGUAGE), maxExpectedDigits);
	}
}

