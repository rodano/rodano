package ch.rodano.configuration.model.validator;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.utils.DisplayableUtils;

public class InvalidNumber implements ValueError {

	public static final Map<String, String> NOT_A_NUMBER = Map.of(
		LanguageStatic.en.getId(), "Invalid number format.",
		LanguageStatic.fr.getId(), "Format de nombre invalide.");

	public static final Map<String, String> TOO_SMALL = Map.of(
		LanguageStatic.en.getId(), "Number is too small. Number must be greater or equal to %s.",
		LanguageStatic.fr.getId(), "Le nombre est trop petit. Le nombre doit être supérieur ou égal à %s.");

	public static final Map<String, String> TOO_BIG = Map.of(
		LanguageStatic.en.getId(), "Number is too big. Number must be lower or equal to %s.",
		LanguageStatic.fr.getId(), "Le nombre est trop grand. Le nombre doit être inférieur ou égal à %s.");

	public static final Map<String, String> DOES_NOT_MATCH = Map.of(
		LanguageStatic.en.getId(), "Number does not match: %s.",
		LanguageStatic.fr.getId(), "Le nombre ne correspond pas au format attendu: %s.");

	private final Map<String, String> messages;

	/**
	 * Constructor
	 *
	 * @param messages
	 * @param args
	 */
	protected InvalidNumber(final Map<String, String> messages, final Object... args) {
		this.messages = new HashMap<>();
		if(messages != null) {
			messages.forEach((key, value) -> this.messages.put(key, String.format(value, args)));
		}
	}

	// Factory methods for ValueCheck
	public static ValueCheck wrongType() {
		return new ValueCheck(new InvalidNumber(NOT_A_NUMBER));
	}

	public static ValueCheck tooSmall(final DecimalFormat formatter, final double minValue) {
		return new ValueCheck(new InvalidNumber(TOO_SMALL, formatter.format(minValue)));
	}

	public static ValueCheck tooBig(final DecimalFormat formatter, final double maxValue) {
		return new ValueCheck(new InvalidNumber(TOO_BIG, formatter.format(maxValue)));
	}

	public static ValueCheck doesNotMatch(final Map<String, String> matcherMessage, final String matcher) {
		return new ValueCheck(new InvalidText(DOES_NOT_MATCH, matcherMessage, matcher));
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
