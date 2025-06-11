package ch.rodano.configuration.model.validator;

import java.util.HashMap;
import java.util.Map;

import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.utils.DisplayableUtils;

public class InvalidPossibleValue implements ValueError {

	public static final Map<String, String> IMPOSSIBLE_VALUE = Map.of(
		LanguageStatic.en.getId(), "%s is not a possible value.",
		LanguageStatic.fr.getId(), "%s n'est pas une valeur possible.");

	public static final Map<String, String> OTHER_OPTION_REQUESTED = Map.of(
		LanguageStatic.en.getId(), "%s is not a possible value and it is not allowed to set more than one other option.",
		LanguageStatic.fr.getId(), "%s n'est pas une valeur possible et il n'est pas autorisé d'enregistrer plus d'une autre option.");


	private final Map<String, String> messages;

	/**
	 * Constructor
	 *
	 * @param messages
	 * @param args
	 */
	protected InvalidPossibleValue(final Map<String, String> messages, final Object... args) {
		this.messages = new HashMap<>();
		if(messages != null) {
			messages.forEach((key, value) -> this.messages.put(key, String.format(value, args)));
		}
	}

	// ValueCheck factory methods
	public static ValueCheck impossibleValue(final String value) {
		return new ValueCheck(new InvalidPossibleValue(IMPOSSIBLE_VALUE, value));
	}

	public static ValueCheck otherOptionRequested(final String value) {
		return new ValueCheck(new InvalidDate(OTHER_OPTION_REQUESTED, value));
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
