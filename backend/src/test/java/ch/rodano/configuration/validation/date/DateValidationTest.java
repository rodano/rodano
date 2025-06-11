package ch.rodano.configuration.validation.date;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.validator.InvalidDate;
import ch.rodano.configuration.validation.AbstractValidationFieldModel;

public class DateValidationTest {

	// ---------------------------------------------------------------
	//  Date Tests
	// ---------------------------------------------------------------

	@Test
	@DisplayName("Valid date is accepted")
	public void validDateIsAccepted() {
		dateModel(true, true).accepts("01.06.2013");
	}

	@Test
	@DisplayName("Valid date time is accepted")
	public void validDateTimeIsAccepted() {
		dateTimeModel(true, true, true, true, true).accepts("05.10.1976 10:45:32");
	}

	@Test
	@DisplayName("Valid partial date is accepted")
	public void validPartialDateIsAccepted() {
		dateModel(true, false).accepts("04.2012");
	}

	@Test
	@DisplayName("Invalid date is rejected")
	public void invalidDateIsRejected() {
		// Historically, formatters did parse 32.06.2013 to 02.07.2013
		dateModel(true, true).rejects("32.06.2013").withErrorCause(invalidDateFormat("dd.mm.yyyy"));
	}

	@Test
	@DisplayName("Incomplete date is rejected")
	public void incompleteDateIsRejected() {
		dateModel(true, true).rejects("1.6.2013").withErrorCause(invalidDateFormat("dd.mm.yyyy"));
	}

	@Test
	@DisplayName("February 29 of a leap year is accepted")
	public void february29OfALeapYearIsAccepted() {
		// Years 2000 and 2004 were leap years
		dateModel(true, true).accepts("29.02.2000");
		dateModel(true, true).accepts("29.02.2004");
	}

	@Test
	@DisplayName("February 29 of a leap year is rejected")
	public void february29OfANonLeapYearIsRejected() {
		// Year 2001 and 1900 were not leap years
		dateModel(true, true).rejects("29.02.2001").withErrorCause(invalidDateFormat("dd.mm.yyyy"));
		dateModel(true, true).rejects("29.02.1900").withErrorCause(invalidDateFormat("dd.mm.yyyy"));
	}

	@Test
	@DisplayName("Date before 1900 is rejected")
	public void dateBefore1900IsRejected() {
		dateModel(true, true).rejects("31.12.1899").withErrorCause(inThePast());
	}

	@Test
	@DisplayName("Date in the future is rejected")
	public void dateInTheFutureIsRejected() {
		final var tomorrow = ZonedDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
		dateModel(true, true).rejects(tomorrow).withErrorCause(inTheFuture());
	}

	@Test
	public void dateInACloseFutureIsRejected() {
		final var tomorrow = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
		dateModel(true, true).rejects(tomorrow).withErrorCause(inTheFuture());
	}

	// ---------------------------------------------------------------
	//  Time Tests
	// ---------------------------------------------------------------

	@Test
	@DisplayName("Time with empty hours and minutes is invalid")
	public void timeWithEmptyHoursAndMinutesPartIsInvalid() {
		timeModel(true, false).rejects(":").withErrorCause(invalidTimeFormat("hh:mm"));
	}

	@Test
	@DisplayName("Time with empty hours is invalid")
	public void timeWithEmptyHoursPartIsInvalid() {
		timeModel(true, false).rejects(":00").withErrorCause(invalidTimeFormat("hh:mm"));
	}

	@Test
	@DisplayName("Time with empty minutes is invalid")
	public void timeWithEmptyMinutesPartIsInvalid() {
		timeModel(true, false).rejects("01:").withErrorCause(invalidTimeFormat("hh:mm"));
	}

	@Test
	@DisplayName("Time with too few digits in hours part is invalid")
	public void timeWithTooFewDigitsInHoursPartIsInvalid() {
		timeModel(true, false).rejects("1:00").withErrorCause(invalidTimeFormat("hh:mm"));
	}

	@Test
	@DisplayName("Time with too few digits in minutes part is invalid")
	public void timeWithTooFewDigitsInMinutesPartIsInvalid() {
		timeModel(true, false).rejects("00:1").withErrorCause(invalidTimeFormat("hh:mm"));
	}

	@Test
	@DisplayName("Time with enough digits in hours and minutes part is valid")
	public void timeWithEnoughDigitsInHoursAndMinutesPartsIsValid() {
		timeModel(true, false).accepts("01:00");
	}

	// ---------------------------------------------------------------
	//  Date Select Tests
	// ---------------------------------------------------------------

	@Test
	@DisplayName("Incomplete date select with leading dot is rejected")
	public void incompleteDateSelectWithLeadingDotIsRejected() {
		dateSelectModel(true, true).rejects(".01.2000").withErrorCause(incomplete());
	}

	@Test
	@DisplayName("Date select with empty days is rejected")
	public void dateSelectWithEmptyDaysIsRejected() {
		dateSelectModel(true, true).rejects("01.2000").withErrorCause(impossibleDate());
	}

	@Test
	@DisplayName("Date select with unknown day and known month and year is valid")
	public void dateSelectWithUnknownDayAndKnownMonthAndYearIsValid() {
		dateSelectModel(true, true).accepts("Unknown.01.2000");
	}

	@Test
	@DisplayName("Date select with known year only is valid")
	public void dateSelectWithKnownYearOnlyIsValid() {
		dateSelectModel(true, true).accepts("Unknown.Unknown.2000");
	}

	@Test
	@DisplayName("Date select with known day and year but unknown month is rejected")
	public void dateSelectWithKnownDayAndYearButUnknownMonthIsRejected() {
		dateSelectModel(true, true).rejects("01.Unknown.2000").withErrorCause(inconsitentMonth());
	}

	@Test
	@DisplayName("Date select with known day only is rejected")
	public void dateSelectWithKnownDayOnlyIsRejected() {
		dateSelectModel(true, true).rejects("01.Unknown.Unknown").withErrorCause(inconsitentMonth());
	}

	@Test
	@DisplayName("Date select with known day and month and unknown year is rejected")
	public void dateSelectWithKnownDayAndMonthAndUnknownYearIsRejected() {
		dateSelectModel(true, true).rejects("01.01.Unknown").withErrorCause(inconsitentYear());
	}

	@Test
	@DisplayName("Date select with known month only is rejected")
	public void dateSelectWithKnownMonthOnlyIsRejected() {
		dateSelectModel(true, true).rejects("Unknown.01.Unknown").withErrorCause(inconsitentYear());
	}

	// ---------------------------------------------------------------
	// Helper methods
	// ---------------------------------------------------------------

	private DateValidationFieldModel dateModel(final boolean withMonths, final boolean withDays) {
		return new DateValidationFieldModel(FieldModelType.DATE, true, withMonths, withDays, false, false, false);
	}

	private DateValidationFieldModel timeModel(final boolean withMinutes, final boolean withSeconds) {
		return new DateValidationFieldModel(FieldModelType.DATE, false, false, false, true, withMinutes, withSeconds);
	}

	private DateValidationFieldModel dateTimeModel(final boolean withMonths, final boolean withDays, final boolean withHours, final boolean withMinutes, final boolean withSeconds) {
		return new DateValidationFieldModel(FieldModelType.DATE, true, withMonths, withDays, withHours, withMinutes, withSeconds);
	}

	private DateValidationFieldModel dateSelectModel(final boolean withMonths, final boolean withDays) {
		return new DateValidationFieldModel(FieldModelType.DATE_SELECT, true, withMonths, withDays, false, false, false);
	}

	private String impossibleDate() {
		return InvalidDate.IMPOSSIBLE_DATE.get(AbstractValidationFieldModel.LANGUAGE);
	}

	private String inThePast() {
		return String.format(InvalidDate.IN_THE_PAST.get(AbstractValidationFieldModel.LANGUAGE), 1900);
	}

	private String inTheFuture() {
		return InvalidDate.IN_THE_FUTURE.get(AbstractValidationFieldModel.LANGUAGE);
	}

	private String inconsitentMonth() {
		return InvalidDate.MONTH_INCONSISTENT.get(AbstractValidationFieldModel.LANGUAGE);
	}

	private String inconsitentYear() {
		return InvalidDate.YEAR_INCONSISTENT.get(AbstractValidationFieldModel.LANGUAGE);
	}

	private String incomplete() {
		return InvalidDate.INCOMPLETE_DATE.get(AbstractValidationFieldModel.LANGUAGE);
	}

	private String invalidDateFormat(final String format) {
		return String.format(InvalidDate.INVALID_DATE_FORMAT.get(AbstractValidationFieldModel.LANGUAGE), format);
	}

	private String invalidTimeFormat(final String format) {
		return String.format(InvalidDate.INVALID_TIME_FORMAT.get(AbstractValidationFieldModel.LANGUAGE), format);
	}
}

