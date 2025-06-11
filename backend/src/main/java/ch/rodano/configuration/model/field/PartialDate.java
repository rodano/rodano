package ch.rodano.configuration.model.field;

import java.security.InvalidParameterException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class PartialDate implements Comparable<PartialDate> {

	//default values are used to initialize the underlying zoned date time
	//when toZonedDateTime() is called, the resulting zoned date time will have these default values for unknown fields
	public static final Map<TemporalField, Integer> DEFAULT_FIELD_VALUES = Map.of(
		ChronoField.ERA, 1,
		ChronoField.YEAR_OF_ERA, 1,
		ChronoField.MONTH_OF_YEAR, 1,
		ChronoField.DAY_OF_MONTH, 1,
		ChronoField.HOUR_OF_DAY, 0,
		ChronoField.MINUTE_OF_HOUR, 0,
		ChronoField.SECOND_OF_MINUTE, 0,
		ChronoField.NANO_OF_SECOND, 0
		);

	//TODO this is wrong
	//base fields are used to compare two dates
	private static final List<TemporalField> BASE_FIELDS = Arrays.asList(
		ChronoField.YEAR_OF_ERA,
		ChronoField.MONTH_OF_YEAR,
		ChronoField.DAY_OF_MONTH,
		ChronoField.HOUR_OF_DAY,
		ChronoField.MINUTE_OF_HOUR,
		ChronoField.SECOND_OF_MINUTE
		);

	public static final PartialDate TOTALLY_UNKNOWN = PartialDate.of(Optional.empty());

	public static final String UNKNOWN_FIELD_LITERAL = "Unknown";
	public static final String TOTALLY_UNKNOWN_DATE_LITERAL = String.format("%1$s.%1$s.%1$s", UNKNOWN_FIELD_LITERAL);

	//underlying zoned date time, used to store the date in a reliable way
	//if any of the field is unknown, the content of the same field in the zoned date time must be disregarded (but will be set to the value from DEFAULT_FIELD_VALUES)
	//for example, that means that if years is unknown, the value of the years in the zoned date time is set to the default value
	private final ZonedDateTime datetime;

	//set of chrono units that are not known
	private final Set<TemporalUnit> unknownUnits;

	private PartialDate(final ZonedDateTime datetime, final Set<TemporalUnit> unknownUnits) {
		Objects.requireNonNull(datetime, "Datetime can not be null");
		Objects.requireNonNull(unknownUnits, "Unknown units can not be null");
		this.datetime = datetime;
		this.unknownUnits = Collections.unmodifiableSet(unknownUnits);
	}

	public static PartialDate of(final ZonedDateTime datetime) {
		return new PartialDate(datetime, Collections.emptySet());
	}

	public static PartialDate ofNullable(final ZonedDateTime datetime) {
		return datetime == null ? null : new PartialDate(datetime, Collections.emptySet());
	}

	public static PartialDate ofObject(final Object date) {
		if(date == null) {
			return null;
		}
		if(date instanceof PartialDate) {
			return (PartialDate) date;
		}
		else if(date instanceof ZonedDateTime) {
			return PartialDate.of((ZonedDateTime) date);
		}
		else if(date instanceof String) {
			return PartialDate.of((String) date);
		}
		throw new IllegalArgumentException(String.format("Class %s can not be transformed in partial date", date.getClass().getName()));
	}

	public static PartialDate now() {
		return PartialDate.of(ZonedDateTime.now());
	}

	public static PartialDate of(final Optional<Integer> year, final Optional<Integer> month, final Optional<Integer> day, final Optional<Integer> hour, final Optional<Integer> minute, final Optional<Integer> second) {
		final var datetime = ZonedDateTime.of(
			year.orElse(DEFAULT_FIELD_VALUES.get(ChronoField.YEAR_OF_ERA)),
			month.orElse(DEFAULT_FIELD_VALUES.get(ChronoField.MONTH_OF_YEAR)),
			day.orElse(DEFAULT_FIELD_VALUES.get(ChronoField.DAY_OF_MONTH)),
			hour.orElse(DEFAULT_FIELD_VALUES.get(ChronoField.HOUR_OF_DAY)),
			minute.orElse(DEFAULT_FIELD_VALUES.get(ChronoField.MINUTE_OF_HOUR)),
			second.orElse(DEFAULT_FIELD_VALUES.get(ChronoField.SECOND_OF_MINUTE)),
			DEFAULT_FIELD_VALUES.get(ChronoField.NANO_OF_SECOND),
			ZoneOffset.UTC);
		final var units = new HashSet<TemporalUnit>();
		if(second.isEmpty()) {
			units.add(ChronoUnit.SECONDS);
		}
		if(minute.isEmpty()) {
			units.add(ChronoUnit.MINUTES);
		}
		if(hour.isEmpty()) {
			units.add(ChronoUnit.HOURS);
		}
		if(day.isEmpty()) {
			units.add(ChronoUnit.DAYS);
		}
		if(month.isEmpty()) {
			units.add(ChronoUnit.MONTHS);
		}
		if(year.isEmpty()) {
			units.add(ChronoUnit.YEARS);
		}
		return new PartialDate(datetime, units);
	}

	public static PartialDate of(final int year, final int month, final int day, final int hour, final int minute, final int second) {
		return of(Optional.of(year), Optional.of(month), Optional.of(day), Optional.of(hour), Optional.of(minute), Optional.of(second));
	}

	public static PartialDate of(final Optional<Integer> year, final Optional<Integer> month, final Optional<Integer> day) {
		return of(year, month, day, Optional.empty(), Optional.empty(), Optional.empty());
	}

	public static PartialDate of(final int year, final int month, final int day) {
		return of(Optional.of(year), Optional.of(month), Optional.of(day));
	}

	public static PartialDate of(final Optional<Integer> year, final Optional<Integer> month) {
		return of(year, month, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
	}

	public static PartialDate of(final int year, final int month) {
		return of(Optional.of(year), Optional.of(month));
	}

	public static PartialDate of(final Optional<Integer> year) {
		return of(year, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
	}

	public static PartialDate of(final int year) {
		return of(Optional.of(year));
	}

	private static Optional<Integer> parsePart(final String part) {
		return PartialDate.UNKNOWN_FIELD_LITERAL.equals(part) ? Optional.empty() : Optional.of(Integer.parseInt(part));
	}

	private static Map<ChronoField, Optional<Integer>> parseDate(final String date) {
		final var parts = StringUtils.splitPreserveAllTokens(date, ".");
		Optional<Integer> year = Optional.empty(), month = Optional.empty(), day = Optional.empty();
		String part;
		var i = parts.length - 1;
		if(i >= 0) {
			part = parts[i--];
			year = parsePart(part);
			if(i >= 0) {
				part = parts[i--];
				month = parsePart(part);
				if(i >= 0) {
					part = parts[i];
					day = parsePart(part);
				}
			}
		}
		return Map.of(
			ChronoField.YEAR_OF_ERA, year,
			ChronoField.MONTH_OF_YEAR, month,
			ChronoField.DAY_OF_MONTH, day
			);
	}

	private static Map<ChronoField, Optional<Integer>> parseTime(final String time) {
		final var parts = StringUtils.splitPreserveAllTokens(time, ":");
		Optional<Integer> hour = Optional.empty(), minute = Optional.empty(), second = Optional.empty();
		String part;
		var i = 0;
		if(i < parts.length) {
			part = parts[i++];
			hour = parsePart(part);
			if(i < parts.length) {
				part = parts[i++];
				minute = parsePart(part);
				if(i < parts.length) {
					part = parts[i];
					second = parsePart(part);
				}
			}
		}
		return Map.of(
			ChronoField.HOUR_OF_DAY, hour,
			ChronoField.MINUTE_OF_HOUR, minute,
			ChronoField.SECOND_OF_MINUTE, second
			);
	}

	public static PartialDate of(final String date) {
		final var parts = StringUtils.split(date, " ");
		//fully unknown date
		if(parts.length == 0) {
			return new PartialDate(
				ZonedDateTime.now(),
				Set.of(ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS)
				);
		}
		try {
			final Map<TemporalField, Optional<Integer>> fields = BASE_FIELDS.stream().collect(Collectors.toMap(Function.identity(), _ -> Optional.empty()));
			//date and time
			if(parts.length == 2) {
				fields.putAll(parseDate(parts[0]));
				fields.putAll(parseTime(parts[1]));
			}
			//only date
			else if(parts.length == 1) {
				if(parts[0].contains(".")) {
					fields.putAll(parseDate(parts[0]));
				}
				//only time
				else if(parts[0].contains(":")) {
					fields.putAll(parseTime(parts[0]));
				}
				//only one integer, considered as a year
				else {
					fields.put(ChronoField.YEAR_OF_ERA, parsePart(parts[0]));
				}
			}

			//unknown units are the one with an empty optional
			final var unknownUnits = fields.entrySet().stream()
				.filter(e -> e.getValue().isEmpty())
				.map(Entry::getKey)
				.map(TemporalField::getBaseUnit)
				.collect(Collectors.toSet());

			//fill map with default values when required
			final var fieldsValues = fields
				.entrySet()
				.stream()
				.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().orElse(DEFAULT_FIELD_VALUES.get(e.getKey()))));

			final var datetime = ZonedDateTime.of(
				fieldsValues.get(ChronoField.YEAR_OF_ERA),
				fieldsValues.get(ChronoField.MONTH_OF_YEAR),
				fieldsValues.get(ChronoField.DAY_OF_MONTH),
				fieldsValues.get(ChronoField.HOUR_OF_DAY),
				fieldsValues.get(ChronoField.MINUTE_OF_HOUR),
				fieldsValues.get(ChronoField.SECOND_OF_MINUTE),
				DEFAULT_FIELD_VALUES.get(ChronoField.NANO_OF_SECOND),
				ZoneOffset.UTC);

			return new PartialDate(datetime, unknownUnits);
		}
		catch(final NumberFormatException e) {
			throw new InvalidParameterException(date);
		}
	}

	public boolean isUnitUnknown(final ChronoUnit unit) {
		return unknownUnits.contains(unit);
	}

	public final Optional<Integer> get(final TemporalField field) {
		if(unknownUnits.contains(field.getBaseUnit())) {
			return Optional.empty();
		}
		return Optional.of(datetime.get(field));
	}

	public final Optional<Integer> getYear() {
		return isYearsUnknown() ? Optional.empty() : Optional.of(datetime.getYear());
	}

	public final Optional<Integer> getMonth() {
		return isMonthsUnknown() ? Optional.empty() : Optional.of(datetime.getMonthValue());
	}

	public final Optional<Integer> getDay() {
		return isDaysUnknown() ? Optional.empty() : Optional.of(datetime.getDayOfMonth());
	}

	public final Optional<Integer> getHour() {
		return isHoursUnknown() ? Optional.empty() : Optional.of(datetime.getHour());
	}

	public final Optional<Integer> getMinute() {
		return isMinutesUnknown() ? Optional.empty() : Optional.of(datetime.getMinute());
	}

	public final Optional<Integer> getSecond() {
		return isSecondsUnknown() ? Optional.empty() : Optional.of(datetime.getSecond());
	}

	public final String getYearLiteral() {
		return getYear().map(y -> Integer.toString(y)).orElse(PartialDate.UNKNOWN_FIELD_LITERAL);
	}

	public final String getMonthLiteral() {
		return getMonth().map(m -> String.format("%02d", m)).orElse(PartialDate.UNKNOWN_FIELD_LITERAL);
	}

	public final String getDayLiteral() {
		return getDay().map(d -> String.format("%02d", d)).orElse(PartialDate.UNKNOWN_FIELD_LITERAL);
	}

	public final String getHourLiteral() {
		return getHour().map(h -> String.format("%02d", h)).orElse(PartialDate.UNKNOWN_FIELD_LITERAL);
	}

	public final String getMinuteLiteral() {
		return getMinute().map(m -> String.format("%02d", m)).orElse(PartialDate.UNKNOWN_FIELD_LITERAL);
	}

	public final String getSecondLiteral() {
		return getSecond().map(s -> String.format("%02d", s)).orElse(PartialDate.UNKNOWN_FIELD_LITERAL);
	}

	public boolean isUnitUnknown(final TemporalUnit unit) {
		return unknownUnits.contains(unit);
	}

	public boolean isSecondsUnknown() {
		return unknownUnits.contains(ChronoUnit.SECONDS);
	}

	public boolean isMinutesUnknown() {
		return unknownUnits.contains(ChronoUnit.MINUTES);
	}

	public boolean isHoursUnknown() {
		return unknownUnits.contains(ChronoUnit.HOURS);
	}

	public boolean isDaysUnknown() {
		return unknownUnits.contains(ChronoUnit.DAYS);
	}

	public boolean isMonthsUnknown() {
		return unknownUnits.contains(ChronoUnit.MONTHS);
	}

	public boolean isYearsUnknown() {
		return unknownUnits.contains(ChronoUnit.YEARS);
	}

	public boolean isComplete() {
		return unknownUnits.isEmpty();
	}

	public boolean isCompletelyUnknown() {
		return isYearsUnknown() &&
			isMonthsUnknown() &&
			isDaysUnknown() &&
			isHoursUnknown() &&
			isMinutesUnknown() &&
			isSecondsUnknown();
	}

	public boolean isAnchoredInTime() {
		return !isYearsUnknown();
	}

	/**
	 *
	 * @return returns the underlying zoned date time if it's possible to anchor the date in time (i.e. the field year is known)
	 * It is considered that the year is sufficient to anchor the date in time but strictly speaking we should have the ERA
	 * If the year is not known (for example, it there is only day and month, or minutes and seconds) it's not possible to place the date in time and not possible to return a zoned date time
	 */
	public Optional<ZonedDateTime> toZonedDateTime() {
		if(isAnchoredInTime()) {
			return Optional.of(datetime);
		}
		return Optional.empty();
	}

	@Override
	public int compareTo(final PartialDate date) {
		for(final var field : BASE_FIELDS) {
			final var value = get(field);
			final var otherValue = date.get(field);
			if(value.isEmpty() || otherValue.isEmpty()) {
				return 0;
			}
			if(!value.equals(otherValue)) {
				return value.get() - otherValue.get();
			}
		}
		return 0;
	}

	public boolean equals(final PartialDate date) {
		return compareTo(date) == 0;
	}

	@Override
	public boolean equals(final Object date) {
		if(date instanceof PartialDate) {
			return equals((PartialDate) date);
		}

		throw new UnsupportedOperationException();
	}

	public boolean after(final PartialDate date) {
		return compareTo(date) > 0;
	}

	public boolean before(final PartialDate date) {
		return compareTo(date) < 0;
	}

	public boolean afterOrEquals(final PartialDate date) {
		return compareTo(date) >= 0;
	}

	public boolean beforeOrEquals(final PartialDate date) {
		return compareTo(date) <= 0;
	}

	public boolean isAfter(final PartialDate date) {
		return after(date);
	}

	public boolean isBefore(final PartialDate date) {
		return before(date);
	}

	public boolean isAfterOrEquals(final PartialDate date) {
		return afterOrEquals(date);
	}

	public boolean isBeforeOrEquals(final PartialDate date) {
		return beforeOrEquals(date);
	}

	public PartialDate plus(final int amount, final TemporalUnit unit) {
		if(isUnitUnknown(unit)) {
			throw new UnsupportedOperationException(String.format("Unable to add %d %s when this field is unknown", amount, unit));
		}
		final var newDatetime = datetime.plus(amount, unit);
		return new PartialDate(newDatetime, unknownUnits);
	}

	public PartialDate plusYears(final int years) {
		return plus(years, ChronoUnit.YEARS);
	}

	public PartialDate plusMonths(final int months) {
		return plus(months, ChronoUnit.MONTHS);
	}

	public PartialDate plusDays(final int days) {
		return plus(days, ChronoUnit.DAYS);
	}

	public PartialDate plusHours(final int hours) {
		return plus(hours, ChronoUnit.HOURS);
	}

	public PartialDate plusMinutes(final int minutes) {
		return plus(minutes, ChronoUnit.MINUTES);
	}

	public PartialDate plusSeconds(final int seconds) {
		return plus(seconds, ChronoUnit.SECONDS);
	}

	public PartialDate minusYears(final int years) {
		return plus(-years, ChronoUnit.YEARS);
	}

	public PartialDate minusMonths(final int months) {
		return plus(-months, ChronoUnit.MONTHS);
	}

	public PartialDate minusDays(final int days) {
		return plus(-days, ChronoUnit.DAYS);
	}

	public PartialDate minusHours(final int hours) {
		return plus(-hours, ChronoUnit.HOURS);
	}

	public PartialDate minusMinutes(final int minutes) {
		return plus(-minutes, ChronoUnit.MINUTES);
	}

	public PartialDate minusSeconds(final int seconds) {
		return plus(-seconds, ChronoUnit.SECONDS);
	}

	public PartialDate with(final TemporalField field, final int newValue) {
		if(isUnitUnknown(field.getBaseUnit())) {
			throw new UnsupportedOperationException(String.format("Unable to adjust %s when this field is unknown", field));
		}
		final var newDatetime = datetime.with(field, newValue);
		return new PartialDate(newDatetime, unknownUnits);
	}

	public PartialDate withYear(final int year) {
		return with(ChronoField.YEAR_OF_ERA, year);
	}

	public PartialDate withMonth(final int month) {
		return with(ChronoField.MONTH_OF_YEAR, month);
	}

	public PartialDate withDayOfMonth(final int day) {
		return with(ChronoField.DAY_OF_MONTH, day);
	}

	public PartialDate withHour(final int hour) {
		return with(ChronoField.HOUR_OF_DAY, hour);
	}

	public PartialDate withMinute(final int minute) {
		return with(ChronoField.MINUTE_OF_DAY, minute);
	}

	public PartialDate withSecond(final int second) {
		return with(ChronoField.SECOND_OF_MINUTE, second);
	}

	@Override
	public String toString() {
		return String.format("%s %s", getDateLiteral(), getTimeLiteral());
	}

	public String getDateLiteral() {
		return String.format("%s.%s.%s", getDayLiteral(), getMonthLiteral(), getYearLiteral());
	}

	public String getTimeLiteral() {
		return String.format("%s:%s:%s", getHourLiteral(), getMinuteLiteral(), getSecondLiteral());
	}

	public String format(final boolean displayYears, final boolean displayMonths, final boolean displayDays) {
		final var value = new StringBuilder();
		if(displayDays) {
			value.append(getDayLiteral());
		}
		if(displayMonths) {
			if(value.length() > 0) {
				value.append(".");
			}
			value.append(getMonthLiteral());
		}
		if(displayYears) {
			if(value.length() > 0) {
				value.append(".");
			}
			value.append(getYearLiteral());
		}
		return value.toString();
	}
}
