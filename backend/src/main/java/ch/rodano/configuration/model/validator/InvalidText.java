package ch.rodano.configuration.model.validator;

import java.util.HashMap;
import java.util.Map;

import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.utils.DisplayableUtils;

public class InvalidText implements ValueError {

	public static final Map<String, String> TOO_LONG = Map.of(
		LanguageStatic.en.getId(), "Text must have at most %d characters.",
		LanguageStatic.fr.getId(), "Le texte ne doit pas contenir plus de %d charactères.");

	public static final Map<String, String> DOES_NOT_MATCH = Map.of(
		LanguageStatic.en.getId(), "Text does not match: %s.",
		LanguageStatic.fr.getId(), "Le texte ne correspond pas au format attendu: %s.");

	private final Map<String, String> messages;

	/**
	 * Constructor
	 *
	 * @param messages
	 * @param maxLength
	 */
	protected InvalidText(final Map<String, String> messages, final int maxLength) {
		this.messages = new HashMap<>();
		if(messages != null) {
			messages.forEach((key, value) -> this.messages.put(key, String.format(value, maxLength)));
		}
	}

	/**
	 * Constructor
	 *
	 * @param messages
	 * @param matcherMessage
	 * @param matcher
	 */
	protected InvalidText(final Map<String, String> messages, final Map<String, String> matcherMessage, final String matcher) {
		this.messages = new HashMap<>();
		if(messages != null) {
			messages.forEach((key, value) -> {
				final String mm;
				if(matcherMessage != null && matcherMessage.containsKey(key)) {
					mm = matcherMessage.get(key);
				}
				else {
					mm = matcher;
				}

				this.messages.put(key, String.format(value, mm));
			});
		}
	}

	// Factory methods for ValueCheck
	public static ValueCheck tooLong(final int maxLength) {
		return new ValueCheck(new InvalidText(TOO_LONG, maxLength));
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
