package ch.rodano.configuration.utils;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import ch.rodano.configuration.model.language.Language;

public class DisplayableUtils {
	public static String getLocalizedMap(final Map<String, String> map, final String... languages) {
		return getLocalizedMapWithDefault(map, "", languages);
	}

	public static String getLocalizedMapWithDefault(final Map<String, String> map, final String defaultMessage, final String... languages) {
		if(map == null) {
			return defaultMessage;
		}
		return Arrays.stream(languages).map(map::get).filter(StringUtils::isNotBlank).findFirst().orElse(defaultMessage);
	}

	public static String getLocalizedMapWithDefault(final Map<String, String> map, final String defaultMessage, final Language... languages) {
		if(map == null) {
			return defaultMessage;
		}
		return Arrays.stream(languages).map(Language::getId).map(map::get).filter(StringUtils::isNotBlank).findFirst().orElse(defaultMessage);
	}
}
