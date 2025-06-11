package ch.rodano.configuration.model.date;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.InvalidParameterException;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.rodano.configuration.model.field.PartialDate;

public class PartialDateTest {
	@BeforeAll
	public static void initialize() {
		//first thing to do, set locale to US
		Locale.setDefault(Locale.US);
	}

	@Test
	@DisplayName("Test constructor")
	public void testConstructor() {
		final var partialDate1 = PartialDate.of(Optional.of(2012), Optional.of(10), Optional.empty());

		assertAll(
			() -> assertTrue(partialDate1.isDaysUnknown()),
			() -> assertFalse(partialDate1.isMonthsUnknown()),
			() -> assertFalse(partialDate1.isYearsUnknown()),
			() -> assertEquals(10, partialDate1.getMonth().get().intValue()),
			() -> assertEquals(2012, partialDate1.getYear().get().intValue())
			);

		final var partialDate2 = PartialDate.of(Optional.of(2012), Optional.empty(), Optional.empty());

		assertAll(
			() -> assertTrue(partialDate2.isDaysUnknown()),
			() -> assertTrue(partialDate2.isMonthsUnknown()),
			() -> assertFalse(partialDate2.isYearsUnknown()),
			() -> assertEquals(2012, partialDate2.getYear().get().intValue())
			);

		final var partialDate3 = PartialDate.of(2012, 10, 5);

		assertAll(
			() -> assertFalse(partialDate3.isDaysUnknown()),
			() -> assertFalse(partialDate3.isMonthsUnknown()),
			() -> assertFalse(partialDate3.isYearsUnknown()),
			() -> assertEquals(5, partialDate3.getDay().get().intValue()),
			() -> assertEquals(10, partialDate3.getMonth().get().intValue()),
			() -> assertEquals(2012, partialDate3.getYear().get().intValue())
			);

		final var partialDate4 = PartialDate.of(2012, 10);

		assertAll(
			() -> assertTrue(partialDate4.isDaysUnknown()),
			() -> assertFalse(partialDate4.isMonthsUnknown()),
			() -> assertFalse(partialDate4.isYearsUnknown()),
			() -> assertTrue(partialDate4.getDay().isEmpty()),
			() -> assertEquals(PartialDate.UNKNOWN_FIELD_LITERAL, partialDate4.getDayLiteral()),
			() -> assertEquals(10, partialDate4.getMonth().get().intValue()),
			() -> assertEquals(2012, partialDate4.getYear().get().intValue())
			);

		final var partialDate5 = PartialDate.of(2012);

		assertAll(
			() -> assertTrue(partialDate5.isDaysUnknown()),
			() -> assertTrue(partialDate5.isMonthsUnknown()),
			() -> assertFalse(partialDate5.isYearsUnknown()),
			() -> assertTrue(partialDate5.getDay().isEmpty()),
			() -> assertEquals(PartialDate.UNKNOWN_FIELD_LITERAL, partialDate5.getDayLiteral()),
			() -> assertTrue(partialDate5.getMonth().isEmpty()),
			() -> assertEquals(PartialDate.UNKNOWN_FIELD_LITERAL, partialDate5.getMonthLiteral()),
			() -> assertEquals(2012, partialDate5.getYear().get().intValue())
			);

		final var partialDateUnknown = PartialDate.TOTALLY_UNKNOWN;

		assertAll(
			() -> assertTrue(partialDateUnknown.isDaysUnknown()),
			() -> assertTrue(partialDateUnknown.isMonthsUnknown()),
			() -> assertTrue(partialDateUnknown.isYearsUnknown()),

			() -> assertFalse(partialDate1.isAfter(partialDate2)),
			() -> assertFalse(partialDate1.isAfter(partialDate3)),
			() -> assertFalse(partialDate1.isAfter(partialDateUnknown)),

			() -> assertFalse(partialDate1.isBefore(partialDate2)),
			() -> assertFalse(partialDate1.isBefore(partialDate3)),
			() -> assertFalse(partialDate1.isBefore(partialDateUnknown)),

			() -> assertEquals(partialDate2, partialDate1),
			() -> assertEquals(partialDate3, partialDate1),
			() -> assertEquals(partialDateUnknown, partialDate1)
			);
	}


	@Test
	@DisplayName("Test zoned date time constructor")
	public void constructor() {
		final var partialDate = PartialDate.of(2002, 1, 1, 22, 22, 0);
		assertAll(
			() -> assertEquals(partialDate.getSecond(), partialDate.getSecond()),
			() -> assertEquals(partialDate.getMonth(), partialDate.getMonth()),
			() -> assertEquals(partialDate.getDay(), partialDate.getDay())
			);
	}

	@Test
	@DisplayName("Test string constructor with date")
	public void testDateString() {
		final var partialDate1 = PartialDate.of(String.format("%s.10.2012", PartialDate.UNKNOWN_FIELD_LITERAL));

		assertAll(
			() -> assertTrue(partialDate1.isDaysUnknown()),
			() -> assertFalse(partialDate1.isMonthsUnknown()),
			() -> assertFalse(partialDate1.isYearsUnknown()),
			() -> assertEquals("10", partialDate1.getMonthLiteral()),
			() -> assertEquals("2012", partialDate1.getYearLiteral())
			);

		final var partialDate2 = PartialDate.of(String.format("%1$s.%1$s.2012", PartialDate.UNKNOWN_FIELD_LITERAL));

		assertAll(
			() -> assertTrue(partialDate2.isDaysUnknown()),
			() -> assertTrue(partialDate2.isMonthsUnknown()),
			() -> assertFalse(partialDate2.isYearsUnknown()),
			() -> assertEquals("2012", partialDate2.getYearLiteral())
			);

		final var partialDate3 = PartialDate.of("05.10.2012");

		assertAll(
			() -> assertFalse(partialDate3.isDaysUnknown()),
			() -> assertFalse(partialDate3.isMonthsUnknown()),
			() -> assertFalse(partialDate3.isYearsUnknown()),
			() -> assertEquals("05", partialDate3.getDayLiteral()),
			() -> assertEquals("10", partialDate3.getMonthLiteral()),
			() -> assertEquals("2012", partialDate3.getYearLiteral())
			);

		final var partialDate4 = PartialDate.of("10.2012");

		assertAll(
			() -> assertTrue(partialDate4.isDaysUnknown()),
			() -> assertFalse(partialDate4.isMonthsUnknown()),
			() -> assertFalse(partialDate4.isYearsUnknown()),
			() -> assertEquals(PartialDate.UNKNOWN_FIELD_LITERAL, partialDate4.getDayLiteral()),
			() -> assertEquals("10", partialDate4.getMonthLiteral()),
			() -> assertEquals("2012", partialDate4.getYearLiteral())
			);

		final var partialDate5 = PartialDate.of("2012");

		assertAll(
			() -> assertTrue(partialDate5.isDaysUnknown()),
			() -> assertTrue(partialDate5.isMonthsUnknown()),
			() -> assertFalse(partialDate5.isYearsUnknown()),
			() -> assertEquals(PartialDate.UNKNOWN_FIELD_LITERAL, partialDate5.getDayLiteral()),
			() -> assertEquals(PartialDate.UNKNOWN_FIELD_LITERAL, partialDate5.getMonthLiteral()),
			() -> assertEquals("2012", partialDate5.getYearLiteral())
			);

		final var partialDateUnknown = PartialDate.of(PartialDate.TOTALLY_UNKNOWN_DATE_LITERAL);

		assertAll(
			() -> assertTrue(partialDateUnknown.isDaysUnknown()),
			() -> assertTrue(partialDateUnknown.isMonthsUnknown()),
			() -> assertTrue(partialDateUnknown.isYearsUnknown()),

			() -> assertFalse(partialDate1.isAfter(partialDate2)),
			() -> assertFalse(partialDate1.isAfter(partialDate3)),
			() -> assertFalse(partialDate1.isAfter(partialDateUnknown)),

			() -> assertFalse(partialDate1.isBefore(partialDate2)),
			() -> assertFalse(partialDate1.isBefore(partialDate3)),
			() -> assertFalse(partialDate1.isBefore(partialDateUnknown)),

			() -> assertEquals(partialDate2, partialDate1),
			() -> assertEquals(partialDate3, partialDate1),
			() -> assertEquals(partialDateUnknown, partialDate1)
			);
	}

	@Test
	@DisplayName("Test string constructor with time")
	public void testTimeString() {
		final var partialDate1 = PartialDate.of(String.format("23:15:%s", PartialDate.UNKNOWN_FIELD_LITERAL));

		assertAll(
			() -> assertTrue(partialDate1.isDaysUnknown()),
			() -> assertTrue(partialDate1.isMonthsUnknown()),
			() -> assertTrue(partialDate1.isYearsUnknown()),
			() -> assertFalse(partialDate1.isHoursUnknown()),
			() -> assertFalse(partialDate1.isMinutesUnknown()),
			() -> assertTrue(partialDate1.isSecondsUnknown()),
			() -> assertEquals(PartialDate.UNKNOWN_FIELD_LITERAL, partialDate1.getYearLiteral()),
			() -> assertEquals(23, partialDate1.getHour().get().intValue()),
			() -> assertEquals(15, partialDate1.getMinute().get().intValue()),
			() -> assertTrue(partialDate1.getSecond().isEmpty())
			);

		final var partialDate2 = PartialDate.of("23:15");

		assertAll(
			() -> assertTrue(partialDate2.isDaysUnknown()),
			() -> assertTrue(partialDate2.isMonthsUnknown()),
			() -> assertTrue(partialDate2.isYearsUnknown()),
			() -> assertFalse(partialDate2.isHoursUnknown()),
			() -> assertFalse(partialDate2.isMinutesUnknown()),
			() -> assertTrue(partialDate2.isSecondsUnknown()),
			() -> assertEquals(PartialDate.UNKNOWN_FIELD_LITERAL, partialDate2.getYearLiteral()),
			() -> assertEquals(23, partialDate2.getHour().get().intValue()),
			() -> assertEquals(15, partialDate2.getMinute().get().intValue()),
			() -> assertTrue(partialDate2.getSecond().isEmpty())
			);

		final var partialDate3 = PartialDate.of("23:15:42");

		assertAll(
			() -> assertTrue(partialDate3.isDaysUnknown()),
			() -> assertTrue(partialDate3.isMonthsUnknown()),
			() -> assertTrue(partialDate3.isYearsUnknown()),
			() -> assertFalse(partialDate3.isHoursUnknown()),
			() -> assertFalse(partialDate3.isMinutesUnknown()),
			() -> assertFalse(partialDate3.isSecondsUnknown()),
			() -> assertEquals(PartialDate.UNKNOWN_FIELD_LITERAL, partialDate3.getYearLiteral()),
			() -> assertEquals(23, partialDate3.getHour().get().intValue()),
			() -> assertEquals(15, partialDate3.getMinute().get().intValue()),
			() -> assertEquals(42, partialDate3.getSecond().get().intValue())
			);
	}

	@Test
	@DisplayName("Test string constructor with date and time")
	public void testDateTimeString() {
		final var partialDate1 = PartialDate.of(String.format("04.05.2020 23:15:%s", PartialDate.UNKNOWN_FIELD_LITERAL));

		assertAll(
			() -> assertFalse(partialDate1.isDaysUnknown()),
			() -> assertFalse(partialDate1.isMonthsUnknown()),
			() -> assertFalse(partialDate1.isYearsUnknown()),
			() -> assertFalse(partialDate1.isHoursUnknown()),
			() -> assertFalse(partialDate1.isMinutesUnknown()),
			() -> assertTrue(partialDate1.isSecondsUnknown()),
			() -> assertEquals(4, partialDate1.getDay().get().intValue()),
			() -> assertEquals(5, partialDate1.getMonth().get().intValue()),
			() -> assertEquals(2020, partialDate1.getYear().get().intValue()),
			() -> assertEquals(23, partialDate1.getHour().get().intValue()),
			() -> assertEquals(15, partialDate1.getMinute().get().intValue()),
			() -> assertTrue(partialDate1.getSecond().isEmpty())
			);

		final var partialDate2 = PartialDate.of("");

		assertAll(
			() -> assertTrue(partialDate2.isDaysUnknown()),
			() -> assertTrue(partialDate2.isMonthsUnknown()),
			() -> assertTrue(partialDate2.isYearsUnknown()),
			() -> assertTrue(partialDate2.isHoursUnknown()),
			() -> assertTrue(partialDate2.isMinutesUnknown()),
			() -> assertTrue(partialDate2.isSecondsUnknown())
			);

	}

	@Test
	@DisplayName("Test string constructor with junk")
	public void testJunkString() {
		assertThrows(InvalidParameterException.class, () -> PartialDate.of("AAAA"));
		assertThrows(InvalidParameterException.class, () -> PartialDate.of("AAAA.BB"));
		assertThrows(InvalidParameterException.class, () -> PartialDate.of("AAAA:BB"));
		assertThrows(InvalidParameterException.class, () -> PartialDate.of("AAAA.BB CC:DDD"));
	}

	@Test
	@DisplayName("Test operations")
	public void testOperations() {
		final var date = PartialDate.of(2002, 10, 1, 23, 15, 42);
		var partialDate = date.plusYears(5);

		assertEquals(2007, partialDate.getYear().get().intValue());
		assertEquals(10, partialDate.getMonth().get().intValue());
		assertEquals(1, partialDate.getDay().get().intValue());
		assertEquals(23, partialDate.getHour().get().intValue());
		assertEquals(15, partialDate.getMinute().get().intValue());
		assertEquals(42, partialDate.getSecond().get().intValue());

		partialDate = date.minusMinutes(30);

		assertEquals(2002, partialDate.getYear().get().intValue());
		assertEquals(10, partialDate.getMonth().get().intValue());
		assertEquals(1, partialDate.getDay().get().intValue());
		assertEquals(22, partialDate.getHour().get().intValue());
		assertEquals(45, partialDate.getMinute().get().intValue());
		assertEquals(42, partialDate.getSecond().get().intValue());
	}

	@Test
	@DisplayName("Test comparison date")
	public void testComparisonDate() {
		//not so precise dates
		final var partialDate1 = PartialDate.of(2012, 8, 3, 0, 0, 0);
		final var partialDate2 = PartialDate.of(2012, 8, 2, 0, 0, 0);

		assertTrue(partialDate2.before(partialDate1));
		assertTrue(partialDate1.after(partialDate2));

		//precise dates
		final var partialDate3 = PartialDate.of(2012, 8, 3, 0, 0, 0);
		final var partialDate4 = PartialDate.of(2012, 8, 2, 10, 30, 15);

		assertTrue(partialDate4.before(partialDate3));
		assertTrue(partialDate3.after(partialDate4));

		//dates without time
		final var partialDate5 = PartialDate.of(2012, 10, 1);
		final var partialDate6 = PartialDate.of(2012, 10, 2);

		assertFalse(partialDate5.isAfter(partialDate6));
		assertTrue(partialDate5.isBefore(partialDate6));

		assertTrue(partialDate6.isAfter(partialDate5));
		assertFalse(partialDate6.isBefore(partialDate5));
	}

	@Test
	@DisplayName("Test partial date comparison")
	public void testComparisonPartialDate() {
		//fully unknown dates (both years unknown)
		final var partialDate1 = PartialDate.TOTALLY_UNKNOWN;
		final var partialDate2 = PartialDate.TOTALLY_UNKNOWN;

		assertAll(
			() -> assertFalse(partialDate1.isAfter(partialDate2)),
			() -> assertFalse(partialDate1.isBefore(partialDate2)),
			() -> assertEquals(0, partialDate1.compareTo(partialDate2)),
			() -> assertTrue(partialDate1.isAfterOrEquals(partialDate2)),
			() -> assertTrue(partialDate1.isBeforeOrEquals(partialDate2))
			);

		//one year unknown
		final var partialDate5 = PartialDate.of(2012, 10, 1);
		final var partialDate6 = PartialDate.TOTALLY_UNKNOWN;

		assertAll(
			() -> assertFalse(partialDate5.isAfter(partialDate6)),
			() -> assertFalse(partialDate5.isBefore(partialDate6)),
			() -> assertEquals(0, partialDate5.compareTo(partialDate6)),
			() -> assertTrue(partialDate5.isAfterOrEquals(partialDate6)),
			() -> assertTrue(partialDate5.isBeforeOrEquals(partialDate6))
			);

		//both months unknown
		final var partialDate7 = PartialDate.of(Optional.of(2012), Optional.empty(), Optional.empty());
		final var partialDate8 = PartialDate.of(Optional.of(2014), Optional.empty(), Optional.empty());

		assertTrue(partialDate7.isBefore(partialDate8));
		assertFalse(partialDate7.isAfter(partialDate6));

		//one month unknown
		final var partialDate9 = PartialDate.of(Optional.of(2012), Optional.empty(), Optional.empty());
		final var partialDate10 = PartialDate.of(2012, 10, 1);

		assertAll(
			() -> assertFalse(partialDate9.isBefore(partialDate10)),
			() -> assertFalse(partialDate9.isAfter(partialDate10)),
			() -> assertEquals(0, partialDate9.compareTo(partialDate10)),
			() -> assertTrue(partialDate9.isAfterOrEquals(partialDate10)),
			() -> assertTrue(partialDate9.isBeforeOrEquals(partialDate10))
			);
	}

	@Test
	@DisplayName("Test equality")
	public void testEquals() {
		//both good dates
		final var partialDate1 = PartialDate.of(2012, 8, 3, 10, 30, 15);
		final var partialDate2 = PartialDate.of(2012, 8, 3, 10, 30, 15);

		assertEquals(partialDate2, partialDate1);

		//both months unknown
		final var partialDate3 = PartialDate.of(Optional.of(2012), Optional.empty(), Optional.empty());
		final var partialDate4 = PartialDate.of(Optional.of(2012), Optional.empty(), Optional.empty());

		assertEquals(partialDate4, partialDate3);

		//full unknown date
		final var partialDate5 = PartialDate.TOTALLY_UNKNOWN;
		final var partialDate6 = PartialDate.TOTALLY_UNKNOWN;

		assertEquals(partialDate6, partialDate5);

		//one unknown date
		final var partialDate7 = PartialDate.TOTALLY_UNKNOWN;
		final var partialDate8 = PartialDate.of(2012, 10, 1);

		assertEquals(partialDate8, partialDate7);

		//months unknown for one date and not for the other
		final var partialDate9 = PartialDate.of(2012, 8, 3);
		final var partialDate10 = PartialDate.of(Optional.of(2012), Optional.empty(), Optional.empty());

		assertEquals(partialDate10, partialDate9);
	}
}

