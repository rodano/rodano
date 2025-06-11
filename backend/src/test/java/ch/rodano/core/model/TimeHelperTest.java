package ch.rodano.core.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.rodano.core.configuration.core.Configurator;
import ch.rodano.core.helpers.time.TimeHelper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = { "spring.profiles.active=test" })
@ContextConfiguration(classes = { Configurator.class })
public class TimeHelperTest {
	//To get the util.Date fields we need a calendar
	private static final Calendar CALENDAR = Calendar.getInstance();
	private static final ObjectMapper MAPPER = new ObjectMapper();

	static {
		MAPPER.registerModule(new JavaTimeModule());
		MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		CALENDAR.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Test
	@DisplayName("Days between")
	public void daysBetween() {
		final var firstDay = ZonedDateTime.of(LocalDateTime.of(LocalDate.of(2016, 7, 1), LocalTime.of(0, 0, 0, 0)), ZoneId.of("UTC"));
		final var lastDay = ZonedDateTime.now();

		final var firstInterval = ChronoUnit.DAYS.between(firstDay, lastDay);

		final var firstDay1 = LocalDateTime.of(2016, 7, 1, 0, 0);
		final var lastDay1 = LocalDateTime.now();

		final var secondInterval = (int) ChronoUnit.DAYS.between(firstDay1, lastDay1);

		assertEquals(secondInterval, firstInterval);
	}

	@Test
	@DisplayName("Truncate to day")
	public void truncateToDay() {
		final var zonedDateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS);

		final var localDate = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);

		assertAll(
			"Check the truncation",
			() -> assertEquals(zonedDateTime.getSecond(), localDate.getSecond()),
			() -> assertEquals(zonedDateTime.getMinute(), localDate.getMinute()),
			() -> assertEquals(zonedDateTime.getHour(), localDate.getHour()),
			() -> assertEquals(zonedDateTime.getDayOfMonth(), localDate.getDayOfMonth()),
			() -> assertEquals(zonedDateTime.getMonth().getValue(), localDate.getMonthValue()),
			() -> assertEquals(zonedDateTime.getYear(), localDate.getYear())
		);
	}

	@Test
	@DisplayName("Date String Formatter")
	public void dateStringFormatter() {
		final var zonedDateTime = ZonedDateTime.now();
		final var zonedDateTimeString = zonedDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

		final var newDate = LocalDateTime.now();
		final var format = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		final var newString = newDate.format(format);

		assertEquals(newString, zonedDateTimeString);
	}

	@Test
	@DisplayName("Simple formatter")
	public void simpleFormatter() {
		final var date = new Date();
		final var sdf = new SimpleDateFormat("dd.MM.yyyy");

		final var ldate = LocalDateTime.now();
		final var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

		assertEquals(sdf.format(date), ldate.format(formatter));
	}

	@Test
	@DisplayName("Parser Test")
	public void parserTest() {
		final var zonedDateTime = ZonedDateTime.of(LocalDateTime.parse("2014-03-27T14:18:49"), ZoneId.of("UTC"));

		final var localDate = LocalDateTime.parse("2014-03-27T14:18:49");

		assertAll(
			"Check the parser",
			() -> assertEquals(zonedDateTime.getSecond(), localDate.getSecond()),
			() -> assertEquals(zonedDateTime.getMinute(), localDate.getMinute()),
			() -> assertEquals(zonedDateTime.getHour(), localDate.getHour()),
			() -> assertEquals(zonedDateTime.getDayOfMonth(), localDate.getDayOfMonth()),
			() -> assertEquals(zonedDateTime.getMonth().getValue(), localDate.getMonthValue()),
			() -> assertEquals(zonedDateTime.getYear(), localDate.getYear())
		);
	}

	@Test
	@DisplayName("Date to ZonedDateTime")
	public void dateToZonedatetime() {
		final var date = new Date();

		final var zdate = TimeHelper.asZonedDateTime(date);

		CALENDAR.setTime(date);

		assertAll(
			"Check the conversion",
			() -> assertEquals(CALENDAR.get(Calendar.SECOND), zdate.getSecond()),
			() -> assertEquals(CALENDAR.get(Calendar.MINUTE), zdate.getMinute()),
			() -> assertEquals(CALENDAR.get(Calendar.HOUR_OF_DAY), zdate.getHour()),
			() -> assertEquals(CALENDAR.get(Calendar.DAY_OF_MONTH), zdate.getDayOfMonth()),
			() -> assertEquals(CALENDAR.get(Calendar.MONTH) + 1, zdate.getMonthValue()),
			() -> assertEquals(CALENDAR.get(Calendar.YEAR), zdate.getYear())
		);
	}

	@Test
	@DisplayName("ZonedDateTime to Date")
	public void zonedatetimeToDate() {
		final var zdate = ZonedDateTime.now();

		final var date = TimeHelper.asDate(zdate);

		CALENDAR.setTime(date);

		assertAll(
			"Check the conversion",
			() -> assertEquals(CALENDAR.get(Calendar.SECOND), zdate.getSecond()),
			() -> assertEquals(CALENDAR.get(Calendar.MINUTE), zdate.getMinute()),
			() -> assertEquals(CALENDAR.get(Calendar.HOUR_OF_DAY), zdate.getHour()),
			() -> assertEquals(CALENDAR.get(Calendar.DAY_OF_MONTH), zdate.getDayOfMonth()),
			() -> assertEquals(CALENDAR.get(Calendar.MONTH) + 1, zdate.getMonthValue()),
			() -> assertEquals(CALENDAR.get(Calendar.YEAR), zdate.getYear())
		);
	}

	@Test
	@DisplayName("UNIX Origin Time")
	public void unixOriginTime() {
		final var date = new Date(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC")).toInstant().toEpochMilli());

		final var ldate = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);

		final var cal = Calendar.getInstance();

		cal.setTime(date);
		cal.setTimeZone(TimeZone.getTimeZone(ZoneId.of("UTC")));

		assertAll(
			"Check the convresion",
			() -> assertEquals(cal.get(Calendar.SECOND), ldate.getSecond()),
			() -> assertEquals(cal.get(Calendar.MINUTE), ldate.getMinute()),
			() -> assertEquals(cal.get(Calendar.HOUR_OF_DAY), ldate.getHour()),
			() -> assertEquals(cal.get(Calendar.DAY_OF_MONTH), ldate.getDayOfMonth()),
			() -> assertEquals(cal.get(Calendar.MONTH) + 1, ldate.getMonthValue()),
			() -> assertEquals(cal.get(Calendar.YEAR), ldate.getYear())
		);
	}

	@Test
	@DisplayName("Timezone Formatter")
	public void timezoneFormatter() {
		final DateFormat dformat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		dformat.setTimeZone(TimeZone.getTimeZone("CET"));
		final var date = new Date();

		final var dformatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
		final var ldate = LocalDateTime.now();

		assertEquals(dformat.format(date), ldate.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("CET")).format(dformatter));

	}
}
