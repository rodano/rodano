package ch.rodano.core.helpers.time;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

public class TimeHelper {
	public static ZonedDateTime asZonedDateTime(final long date) {
		return new Date(date).toInstant().atZone(ZoneId.of("UTC"));
	}

	public static ZonedDateTime asZonedDateTime(final Date input) {
		if(input != null) {
			return ZonedDateTime.ofInstant(input.toInstant(), ZoneOffset.UTC);
		}
		return null;
	}

	public static Date asDate(final ZonedDateTime input) {
		if(input != null) {
			return Date.from(input.toInstant());
		}
		return null;
	}

	/**
	 * Convert a zoned date time to a SQL timestamp
	 *
	 * @param input The date to convert
	 * @return A timestamp corresponding to the given date
	 */
	public static Timestamp asSQLTimestamp(final ZonedDateTime input) {
		if(input != null) {
			return Timestamp.from(input.toInstant());
		}
		return null;
	}
}
