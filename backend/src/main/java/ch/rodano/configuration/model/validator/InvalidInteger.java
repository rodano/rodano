package ch.rodano.configuration.model.validator;

import java.util.HashMap;
import java.util.Map;

import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.utils.DisplayableUtils;

public class InvalidInteger implements ValueError {

	public static final Map<String, String> NOT_AN_INTEGER = Map.of(
		LanguageStatic.en.getId(), "Invalid number format. Number must be an integer.",
		LanguageStatic.fr.getId(), "Format de nombre invalide. Le nombre doit être un entier.");

	public static final Map<String, String> TOO_MANY_DIGITS = Map.of(
		LanguageStatic.en.getId(), "Invalid number format. Number must have at most %d digit(s).",
		LanguageStatic.fr.getId(), "Format de nombre invalide. Le nombre doit avoir au plus %d chiffre(s).");

	private final Map<String, String> messages;

	/**
	 * Constructor
	 *
	 * @param messages
	 * @param args
	 */
	protected InvalidInteger(final Map<String, String> messages, final Object... args) {
		this.messages = new HashMap<>();
		if(messages != null) {
			messages.forEach((key, value) -> this.messages.put(key, String.format(value, args)));
		}
	}

	// ValueCheck factory methods

	public static ValueCheck wrongType() {
		return new ValueCheck(new InvalidInteger(NOT_AN_INTEGER));
	}

	public static ValueCheck tooManyDigits(final String value, final int maxDigitsExpected) {
		return new ValueCheck(new InvalidInteger(TOO_MANY_DIGITS, maxDigitsExpected), value);
	}

	@Override
	public String getLocalizedMessage(final String... languages) {
		return DisplayableUtils.getLocalizedMap(messages, languages);
	}

	@Override
	public String getDefaultLocalizedMessage() {
		return DisplayableUtils.getLocalizedMap(messages, LanguageStatic.en.getId());
	}
}
