package ch.rodano.core.utils;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public final class Utils {

	private static final Pattern REGEX = Pattern.compile("\\$\\{(\\w+)}");

	public static int getDifferencesInDayBetweenDate(final ZonedDateTime date1, final ZonedDateTime date2) {
		final double differenceInMs = Math.abs(date1.toInstant().toEpochMilli() - date2.toInstant().toEpochMilli());
		return (int) Math.round(differenceInMs / 86400000D);
	}

	public static String implodeForSQL(final Collection<?> items) {
		final var sql = new StringBuilder();
		for(final Object item : items) {
			if(sql.length() > 0) {
				sql.append(",");
			}
			final var value = item.toString();
			final var isNumeric = StringUtils.isNumeric(value);
			if(!isNumeric) {
				sql.append("'");
			}
			sql.append(value);
			if(!isNumeric) {
				sql.append("'");
			}
		}
		return sql.toString();
	}

	public static String replaceText(final String text, final Map<String, String> replacements) {
		if(text == null || text.trim().length() == 0) {
			return "";
		}
		final var replacedText = new StringBuffer();
		final var regexMatcher = REGEX.matcher(text);
		while(regexMatcher.find()) {
			regexMatcher.appendReplacement(replacedText, replacements.getOrDefault(regexMatcher.group(1), "NA"));
		}
		regexMatcher.appendTail(replacedText);
		return replacedText.toString();
	}
}
