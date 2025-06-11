package ch.rodano.core.services.dao.utils;

public class DatabaseUtils {
	/**
	 * Convert camel case to snake case
	 *
	 * @param camel The string to convert
	 * @param upper Whether to return an UPPER_SNAKE_CASE or a lower_snake_case string
	 * @return A snake cased string
	 */
	public static String camelToSnakeCase(final String camel, final Boolean upper) {
		if(camel == null || camel.isEmpty()) {
			return camel;
		}

		final var stringBuilder = new StringBuilder();
		final var charArray = camel.toCharArray();
		for(var i = 0; i < charArray.length; i++) {
			final var c = charArray[i];
			final var nc = upper ? Character.toUpperCase(c) : Character.toLowerCase(c);
			if(Character.isUpperCase(c) && i != 0) {
				stringBuilder.append('_').append(nc);
			}
			else {
				stringBuilder.append(nc);
			}
		}
		return stringBuilder.toString();
	}
}
